/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.integrations.jira;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.BasicPriority;
import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.api.domain.CimIssueType;
import com.atlassian.jira.rest.client.api.domain.CustomFieldOption;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.TimeTracking;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.devexperts.switchboard.api.TestRunConsumer;
import com.devexperts.switchboard.entities.Attributes;
import com.devexperts.switchboard.entities.Pair;
import com.devexperts.switchboard.entities.Test;
import com.devexperts.switchboard.entities.TestRun;
import com.devexperts.switchboard.entities.valuesupplier.TestValuesExtractor;
import com.devexperts.switchboard.utils.Arguments;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Implementation of {@link TestRunConsumer} checking if an issue of specified type linked by Xray `Generic Test Definition` field
 * exists for each test in TestSet and creating a new one if it does not exist
 */
public class XRayGenericTestsCreatingConsumer implements TestRunConsumer<JiraIntegrationFeatures> {
    private enum ProcessingStatus {CREATED, UPDATED, INPUT_DUPLICATE, JIRA_DUPLICATE, UPDATE_PROHIBITED, CREATE_FAILURE, UPDATE_FAILURE}

    private static final Logger log = LoggerFactory.getLogger(XRayGenericTestsCreatingConsumer.class);
    private static final String GENERIC_TEST_DEFINITION_FIELD = "Generic Test Definition";
    private static final String JIRA_ISSUE_KEY_NAME = "key";
    private static final String TIME_TRACKING_FIELD = "Time Tracking";
    private static final String CHILD = "child";

    @JsonProperty(required = true)
    private String identifier;
    @JsonProperty(required = true)
    private String project;
    @JsonProperty(required = true)
    private String issueTypeName;
    @JsonProperty(required = true)
    private TestValuesExtractor summarySupplier;
    @JsonProperty(required = true)
    private Map<String, TestValuesExtractor> fieldValuesExtractors;
    @JsonProperty(defaultValue = "[:]")
    private Map<String, TestValuesExtractor> defaultFieldValues = new HashMap<>();
    @JsonProperty
    private Pair<String, TestValuesExtractor> xrayFieldLinkToTestAttribute = null;
    @JsonProperty(defaultValue = "true")
    private boolean updateExisting = true;
    @JsonProperty(defaultValue = "true")
    private boolean failSafeStrategy = true;

    @JsonIgnore
    private final List<Issue> existing = new ArrayList<>();
    @JsonIgnore
    private JiraIntegrationFeatures features;

    @JsonIgnore
    private String xrayMappingKey = GENERIC_TEST_DEFINITION_FIELD;
    @JsonIgnore
    private boolean isMappedByJiraKey = false;
    @JsonIgnore
    private Function<Test, String> testXrayMappingValueExtractor = XRayGenericTestsCreatingConsumer::getTestDefinition;

    private XRayGenericTestsCreatingConsumer() {}

    public XRayGenericTestsCreatingConsumer(String identifier, String project, String issueTypeName, TestValuesExtractor summarySupplier,
                                            Map<String, TestValuesExtractor> fieldValuesExtractors, Map<String, TestValuesExtractor> defaultFieldValues,
                                            Pair<String, TestValuesExtractor> xrayFieldLinkToTestAttribute, boolean updateExisting, boolean failSafeStrategy)
    {
        this.identifier = identifier;
        this.project = project;
        this.issueTypeName = issueTypeName;
        this.summarySupplier = summarySupplier;
        this.fieldValuesExtractors = fieldValuesExtractors;
        this.defaultFieldValues = defaultFieldValues;
        this.xrayFieldLinkToTestAttribute = xrayFieldLinkToTestAttribute;
        this.updateExisting = updateExisting;
        this.failSafeStrategy = failSafeStrategy;
    }

    @Override
    public void init(JiraIntegrationFeatures integrationFeatures) {
        this.features = integrationFeatures;

        existing.addAll(integrationFeatures.searchForIssues(String.format("project = %s AND issuetype = %s", project, issueTypeName)));
        if (existing.isEmpty()) {
            log.info("{} '{}' found no XRay tests in project '{}'", getClass().getSimpleName(), getIdentifier(), project);
        } else {
            log.info("{} '{}' found {} XRay tests in project '{}'", getClass().getSimpleName(), getIdentifier(), existing.size(), project);
        }

        if (xrayFieldLinkToTestAttribute != null) {
            xrayMappingKey = Arguments.checkNotBlank(xrayFieldLinkToTestAttribute.getKey(),
                    "Jira field for test matching is not specified");
            isMappedByJiraKey = xrayMappingKey.equals(JIRA_ISSUE_KEY_NAME);
            TestValuesExtractor extractor = Arguments.checkNotNull(xrayFieldLinkToTestAttribute.getValue(),
                    "TestValuesExtractor for XRay matching is not specified");
            testXrayMappingValueExtractor = extractor::getTestValue;
            if (!isMappedByJiraKey) {
                fieldValuesExtractors.put(xrayMappingKey, extractor);
            }
        }
    }

    @Override
    public Void accept(List<TestRun> testRuns) {
        Map<ProcessingStatus, List<IssueInput>> result = doAccept(testRuns);
        log.info(describeResult(result));
        return null;
    }

    private Map<ProcessingStatus, List<IssueInput>> doAccept(List<TestRun> testRuns) {
        Map<ProcessingStatus, List<IssueInput>> result = new EnumMap<>(ProcessingStatus.class);
        for (TestRun testRun : testRuns) {
            Map<String, Set<Test>> testsByKey = testRun.getTests().stream()
                    .collect(Collectors.groupingBy(t -> testXrayMappingValueExtractor.apply(t), Collectors.toSet()));
            List<Map.Entry<String, Set<Test>>> duplicates = testsByKey.entrySet().stream().filter(t -> t.getValue().size() > 1).collect(Collectors.toList());
            for (Map.Entry<String, Set<Test>> duplicate : duplicates) {
                log.warn("Found tests in test run '{}' duplicated by '{}'='{}': {}",
                        testRun.getIdentifier(), xrayMappingKey, duplicate.getKey(),
                        duplicate.getValue().stream().map(XRayGenericTestsCreatingConsumer::getTestDefinition).collect(Collectors.joining(", ")));
                for (Test test : duplicate.getValue()) {
                    result.computeIfAbsent(ProcessingStatus.INPUT_DUPLICATE, k -> new ArrayList<>()).add(createInput(test));
                }
                testsByKey.remove(duplicate.getKey());
            }

            for (Map.Entry<String, Set<Test>> testWithKey : testsByKey.entrySet()) {
                String testMappingVal = testWithKey.getKey();
                Test test = testWithKey.getValue().iterator().next();
                String testDefinition = getTestDefinition(test);
                List<Issue> linked = (isMappedByJiraKey ?
                        existing.stream().filter(i -> i.getKey().equalsIgnoreCase(testMappingVal)) :
                        existing.stream()
                                .filter(i -> {
                                    IssueField f = i.getFieldByName(xrayMappingKey);
                                    return f != null && f.getValue() != null
                                            && Objects.equals(f.getValue().toString(), testMappingVal);
                                }))
                        .collect(Collectors.toList());
                IssueInput input = createInput(test);
                String testDesc = describeTestLink(testDefinition, testMappingVal);
                if (linked.isEmpty()) {
                    try {
                        BasicIssue created = features.createIssue(input);
                        log.debug("Created test {}: {}", testDesc, created.getKey());
                        result.computeIfAbsent(ProcessingStatus.CREATED, k -> new ArrayList<>()).add(input);
                    } catch (Exception e) {
                        String failMsg = String.format("Failed to create test %s", testDesc);
                        if (failSafeStrategy) {
                            log.error("{}{}", failMsg, collectCauseMessages(e));
                            log.debug(failMsg, e);
                            result.computeIfAbsent(ProcessingStatus.CREATE_FAILURE, k -> new ArrayList<>()).add(input);
                        } else {
                            throw new RuntimeException(failMsg, e);
                        }
                    }
                } else if (updateExisting) {
                    if (linked.size() == 1) {
                        Issue singleFound = linked.get(0);
                        try {
                            features.updateIssue(singleFound.getKey(), input);
                            log.debug("Updated existing test {}: {}", testDesc, singleFound.getKey());
                            result.computeIfAbsent(ProcessingStatus.UPDATED, k -> new ArrayList<>()).add(input);
                        } catch (Exception e) {
                            String failMsg = String.format("Failed to update issue %s %s", singleFound.getKey(), testDesc);
                            if (failSafeStrategy) {
                                log.error("{}{}", failMsg, collectCauseMessages(e));
                                log.debug(failMsg, e);
                                result.computeIfAbsent(ProcessingStatus.UPDATE_FAILURE, k -> new ArrayList<>()).add(input);
                            } else {
                                throw new RuntimeException(failMsg, e);
                            }
                        }
                    } else {
                        log.warn("Cannot update existing Jira issues: multiple match. Found {} issues {}: {}",
                                linked.size(), testDesc, linked.stream().map(BasicIssue::getKey).collect(Collectors.joining(", ")));
                        result.computeIfAbsent(ProcessingStatus.JIRA_DUPLICATE, k -> new ArrayList<>()).add(input);
                    }
                } else {
                    log.warn("Found {} issues {}: {}", linked.size(), testDesc,
                            linked.stream().map(BasicIssue::getKey).collect(Collectors.joining(", ")));
                    result.computeIfAbsent(ProcessingStatus.UPDATE_PROHIBITED, k -> new ArrayList<>()).add(input);
                }
            }
        }
        return result;
    }

    private static String getTestDefinition(Test test) {
        return String.format("%s.%s.%s",
                test.getAttributes().getSingleAttributeValue(Attributes.LOCATION_PROP, "package").orElse(""),
                test.getAttributes().getSingleAttributeValue(Attributes.LOCATION_PROP, "class").orElse(""),
                test.getAttributes().getSingleAttributeValue(Attributes.LOCATION_PROP, "method").orElse(""));
    }

    private String describeTestLink(String testDefinition, String testMappingVal) {
        return String.format("linked to %s by '%s'='%s'", testDefinition, xrayMappingKey, testMappingVal);
    }

    private static String describeResult(Map<ProcessingStatus, List<IssueInput>> result) {
        StringBuilder sb = new StringBuilder(String.format("Processed %s tests. Execution result:",
                result.values().stream().mapToInt(List::size).sum()))
                .append(String.format("\r\n\t\t- %s tests created successfully",
                        result.getOrDefault(ProcessingStatus.CREATED, new ArrayList<>()).size()))
                .append(String.format("\r\n\t\t- %s tests updated successfully",
                        result.getOrDefault(ProcessingStatus.UPDATED, new ArrayList<>()).size()));
        if (result.containsKey(ProcessingStatus.INPUT_DUPLICATE)) {
            sb.append(String.format("\r\n\t\t- %s tests not processed due to key duplication between supplied tests",
                    result.get(ProcessingStatus.INPUT_DUPLICATE).size()));
        }
        if (result.containsKey(ProcessingStatus.JIRA_DUPLICATE)) {
            sb.append(String.format("\r\n\t\t- %s tests not processed due to key duplication in Jira",
                    result.get(ProcessingStatus.JIRA_DUPLICATE).size()));
        }
        if (result.containsKey(ProcessingStatus.UPDATE_PROHIBITED)) {
            sb.append(String.format("\r\n\t\t- %s tests not processed due update not allowed",
                    result.get(ProcessingStatus.UPDATE_PROHIBITED).size()));
        }
        if (result.containsKey(ProcessingStatus.CREATE_FAILURE)) {
            sb.append(String.format("\r\n\t\t- %s tests creation failed",
                    result.get(ProcessingStatus.CREATE_FAILURE).size()));
        }
        if (result.containsKey(ProcessingStatus.UPDATE_FAILURE)) {
            sb.append(String.format("\r\n\t\t- %s tests update failed",
                    result.get(ProcessingStatus.UPDATE_FAILURE).size()));
        }

        return sb.toString();
    }

    private static String collectCauseMessages(Exception e) {
        StringBuilder sb = new StringBuilder();
        Throwable cause = e;
        int depth = 1;
        String prevCauseMsg = "";
        while (cause != null) {
            String causeMsg = cause.getMessage();
            if (!prevCauseMsg.equals(causeMsg)) {
                sb.append(":\n");
                for (int i = 0; i < depth; i++) {
                    sb.append("\t");
                }
                sb.append(causeMsg);
                depth++;
            }
            cause = cause.getCause();
            prevCauseMsg = causeMsg;
        }
        return sb.toString();
    }

    private IssueInput createInput(Test test) {
        CimIssueType type = features.getIssueType(project, issueTypeName);
        IssueInputBuilder builder = new IssueInputBuilder(project, type.getId(),
                Arguments.checkNotNull(summarySupplier, "summary value suppliers").getTestValue(test));

        Map<String, CimFieldInfo> fields = type.getFields().values().stream().collect(Collectors.toMap(CimFieldInfo::getName, f -> f));

        Set<String> filledFields = new HashSet<>();
        for (Map.Entry<String, TestValuesExtractor> entry : fieldValuesExtractors.entrySet()) {
            if (entry.getValue().getTestValue(test) != null) {
                filledFields.add(entry.getKey());
                setField(test, builder, entry.getKey(), entry.getValue(), fields);
            }
        }
        for (Map.Entry<String, TestValuesExtractor> entry : defaultFieldValues.entrySet()) {
            if (!filledFields.contains(entry.getKey()) && entry.getValue().getTestValue(test) != null) {
                filledFields.add(entry.getKey());
                setField(test, builder, entry.getKey(), entry.getValue(), fields);
            }
        }

        final CimFieldInfo timeTrackingField = fields.get(TIME_TRACKING_FIELD);
        if (timeTrackingField.isRequired() && !filledFields.contains(TIME_TRACKING_FIELD)) {
            builder.setFieldValue(timeTrackingField.getId(), new TimeTracking(1, null, null));
        }
        return builder.build();
    }

    private IssueInputBuilder setField(Test test, IssueInputBuilder builder, String fieldName, TestValuesExtractor valuesExtractor,
                                       Map<String, CimFieldInfo> fields)
    {
        String value = valuesExtractor.getTestValue(test);
        if (value == null) {
            return builder;
        }

        // fill the values suitable for direct builder setting:
        if ("Component/s".equals(fieldName)) {
            return builder.setComponentsNames(valuesExtractor.getTestValues(test));
        }
        if ("Affects Version/s".equals(fieldName)) {
            return builder.setAffectedVersionsNames(valuesExtractor.getTestValues(test));
        }
        if ("Fix Version/s".equals(fieldName)) {
            return builder.setFixVersionsNames(valuesExtractor.getTestValues(test));
        }
        if ("Description".equals(fieldName)) {
            return builder.setDescription(value);
        }
        if ("Due Date".equals(fieldName)) {
            return builder.setDueDate(ISODateTimeFormat.date().parseDateTime(value));
        }

        // fill custom fields
        CimFieldInfo field = fields.get(fieldName);
        if (field == null) {
            throw new IllegalStateException(String.format("Field with name '%s' not found among project fields", fieldName));
        }

        // process array
        if (Objects.equals("array", field.getSchema().getType())) {
            List<Object> values = new ArrayList<>();
            if (field.getSchema().getItems() != null) {
                valuesExtractor.getTestValues(test).forEach(v ->
                        values.add(transformValue(fieldName, field.getSchema().getItems(), field.getAllowedValues(), v)));
            } else {
                values.addAll(valuesExtractor.getTestValues(test));
            }
            return builder.setFieldValue(field.getId(), values);
        }
        // process as simple field
        return builder.setFieldValue(field.getId(), transformValue(fieldName, field.getSchema().getType(),
                field.getAllowedValues(), value));
    }

    private Object transformValue(String fieldName, String fieldType, Iterable<Object> allowedValuesIter, String value) {
        List<Object> allowedValues = allowedValuesIter == null ? null :
                StreamSupport.stream(allowedValuesIter.spliterator(), false).collect(Collectors.toList());
        if (Arrays.asList("string", "any").contains(fieldType)) {
            return value;
        }
        if ("number".equals(fieldType)) {
            if (value.matches("\\d+")) {
                return Integer.valueOf(value);
            }
            if (value.matches("(\\d+\\.\\d*)|(\\d*\\.\\d+)")) {
                return Double.valueOf(value);
            }
            throw new IllegalStateException(String.format("Unexpected value for a number field '%s': %s", fieldName, value));
        }
        if ("date".equals(fieldType)) {
            return getDate(fieldName, value);
        }
        if ("timetracking".equals(fieldType)) {
            return getTimeTracking(value);
        }
        if ("priority".equals(fieldType)) {
            return getPriority(fieldName, value, allowedValues);
        }
        if ("option".equals(fieldType)) {
            return getOption(fieldName, value, allowedValues);
        }
        if ("user".equals(fieldType)) {
            return features.findUser(value);
        }
        throw new IllegalStateException("Unexpected field type for setting by ValuesExtractor: " + fieldType);
    }

    private CustomFieldOption getOption(String fieldName, String value, List<Object> allowedValues) {
        if (allowedValues == null || allowedValues.isEmpty()) {
            throw new IllegalStateException("Failed to get allowed values for options field " + fieldName);
        }
        return allowedValues.stream()
                .map(v -> parseOption(v, fieldName))
                .filter(v -> Objects.equals(value, v.getValue()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "Failed to get an allowed option for field '%s' matching specified value '%s'. Allowed values: %s", fieldName, value,
                        allowedValues.stream().map(v -> ((CustomFieldOption) v).getValue()).collect(Collectors.joining(", ")))));
    }

    private BasicPriority getPriority(String fieldName, String value, List<Object> allowedValues) {
        if (allowedValues == null || allowedValues.isEmpty()) {
            throw new IllegalStateException("Failed to get allowed values for priority field " + fieldName);
        }
        return allowedValues.stream()
                .map(BasicPriority.class::cast)
                .filter(p -> p.getName().equals(value) || p.getName().matches(value + " \\(\\d+\\)"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "Failed to get an allowed priority matching specified value '%s'. Allowed values: %s", value,
                        allowedValues.stream().map(v -> ((CustomFieldOption) v).getValue()).collect(Collectors.joining(", ")))));
    }

    private TimeTracking getTimeTracking(String value) {
        if (value.matches("\\d+")) {
            return new TimeTracking(Integer.valueOf(value), null, null);
        }
        throw new IllegalArgumentException("Expected originalEstimateMinutes value passed to timetracking as an integer, found: " + value);
    }

    private DateTime getDate(String fieldName, String value) {
        try {
            return ISODateTimeFormat.date().parseDateTime(value);
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Failed to parse date value '%s' for '%s' field",
                    value, fieldName), e);
        }
    }

    private static CustomFieldOption parseOption(Object o, String fieldName) {
        if (o instanceof CustomFieldOption) {
            return (CustomFieldOption) o;
        }
        if (o instanceof JSONObject) {
            try {
                JSONObject j = (JSONObject) o;
                JSONArray jChildren = j.has("children") ? j.getJSONArray("children") : new JSONArray();
                List<CustomFieldOption> children = new ArrayList<>();
                for (int i = 0; i < jChildren.length(); i++) {
                    JSONObject jChild = jChildren.getJSONObject(i);
                    children.add(parseOption(jChild, fieldName + ", available value child " + jChild));
                }
                return new CustomFieldOption(j.getLong("id"), new URI(j.getString("self")), j.getString("value"),
                        children, j.has(CHILD) ? parseOption(j.get(CHILD), fieldName + ", available value child " + j.get(CHILD)) : null);
            } catch (JSONException | URISyntaxException je) {
                throw new RuntimeException("Failed to parse available value for field " + fieldName, je);
            }
        }
        throw new IllegalStateException("Unexpected allowed value type: " + o.getClass().getName());
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void close() {/*do nothing*/}
}
