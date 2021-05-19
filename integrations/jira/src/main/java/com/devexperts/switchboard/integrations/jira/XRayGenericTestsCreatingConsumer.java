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
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static final Logger log = LoggerFactory.getLogger(XRayGenericTestsCreatingConsumer.class);
    private static final String GENERIC_TEST_DEFINITION_FIELD = "Generic Test Definition";
    private static final String JIRA_ISSUE_KEY_NAME = "key";

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
    @JsonProperty(defaultValue = "true")
    private boolean updateExisting = true;
    @JsonProperty
    private Pair<String, TestValuesExtractor> xrayFieldLinkToTestAttribute = null;

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
                                            boolean updateExisting, Pair<String, TestValuesExtractor> xrayFieldLinkToTestAttribute)
    {
        this.identifier = identifier;
        this.project = project;
        this.issueTypeName = issueTypeName;
        this.summarySupplier = summarySupplier;
        this.fieldValuesExtractors = fieldValuesExtractors;
        this.defaultFieldValues = defaultFieldValues;
        this.updateExisting = updateExisting;
        this.xrayFieldLinkToTestAttribute = xrayFieldLinkToTestAttribute;
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
        Map<IssueInput, Boolean> result = doAccept(testRuns);
        log.info("Created/updated {} XRay issues", result.size());
        return null;
    }

    private Map<IssueInput, Boolean> doAccept(List<TestRun> testRuns) {
        Map<IssueInput, Boolean> result = new HashMap<>();
        for (TestRun testRun : testRuns) {
            for (Test test : testRun.getTests()) {
                String testMappingVal = testXrayMappingValueExtractor.apply(test);
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
                if (linked.isEmpty()) {
                    BasicIssue created = features.jiraClient.getIssueClient().createIssue(input).claim();
                    log.info("Created test linked to {}: {}", testDefinition, created);
                    result.put(input, true);
                } else if (updateExisting) {
                    if (linked.size() == 1) {
                        Issue singleFound = linked.get(0);
                        log.info("Found existing test linked to {}: {} {}", testDefinition, singleFound.getKey(), singleFound.getSummary());
                        features.jiraClient.getIssueClient().updateIssue(singleFound.getKey(), input).claim();
                        result.put(input, true);
                    } else {
                        log.warn("Cannot update existing issues: multiple match. " +
                                "Found {} issues linked to {} : {}", linked.size(), testDefinition, linked.stream());
                        result.put(input, false);
                    }
                } else {
                    log.warn("Found {} issues linked to {} : {}", linked.size(), testDefinition, linked.stream()
                            .map(BasicIssue::getId)
                            .map(String::valueOf)
                            .collect(Collectors.joining(", ")));
                    result.put(input, false);
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

        if (fields.get("Time Tracking").isRequired() && !filledFields.contains("Time Tracking")) {
            builder.setFieldValue(fields.get("Time Tracking").getId(), new TimeTracking(1, null, null));
        }
        return builder.build();
    }

    private static IssueInputBuilder setField(Test test, IssueInputBuilder builder, String fieldName, TestValuesExtractor valuesExtractor,
                                              Map<String, CimFieldInfo> fields)
    {
        String value = valuesExtractor.getTestValue(test);
        if (value == null) {
            return builder;
        }

        // fill the values suitable for direct builder setting:
        if (Objects.equals("Component/s", fieldName)) {
            return builder.setComponentsNames(valuesExtractor.getTestValues(test));
        }
        if (Objects.equals("Affects Version/s", fieldName)) {
            return builder.setAffectedVersionsNames(valuesExtractor.getTestValues(test));
        }
        if (Objects.equals("Fix Version/s", fieldName)) {
            return builder.setFixVersionsNames(valuesExtractor.getTestValues(test));
        }
        if (Objects.equals("Description", fieldName)) {
            return builder.setDescription(value);
        }
        if (Objects.equals("Due Date", fieldName)) {
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

    private static Object transformValue(String fieldName, String fieldType, Iterable<Object> allowedValuesIter, String value)
    {
        List<Object> allowedValues = allowedValuesIter == null ? null :
                StreamSupport.stream(allowedValuesIter.spliterator(), false).collect(Collectors.toList());
        if (Arrays.asList("string", "any").contains(fieldType)) {
            return value;
        } else if (Objects.equals("number", fieldType)) {
            if (value.matches("\\d+")) {
                return Integer.valueOf(value);
            }
            if (value.matches("(\\d+\\.\\d*)|(\\d*\\.\\d+)")) {
                return Double.valueOf(value);
            }
            throw new IllegalStateException(String.format("Unexpected value for a number field '%s': %s", fieldName, value));
        } else if (Objects.equals("date", fieldType)) {
            try {
                return ISODateTimeFormat.date().parseDateTime(value);
            } catch (Exception e) {
                throw new IllegalStateException(String.format("Failed to parse date value '%s' for '%s' field",
                        value, fieldName), e);
            }
        } else if (Objects.equals("timetracking", fieldType)) {
            if (value.matches("\\d+")) {
                return new TimeTracking(Integer.valueOf(value), null, null);
            } else {
                throw new IllegalArgumentException("Expected originalEstimateMinutes value passed to timetracking as an integer, found: " + value);
            }
        } else if (Objects.equals("priority", fieldType)) {
            if (allowedValues == null || allowedValues.isEmpty()) {
                throw new IllegalStateException("Failed to get allowed values for priority field " + fieldName);
            }
            return allowedValues.stream()
                    .map(v -> (BasicPriority) v)
                    .filter(p -> p.getName().equals(value) || p.getName().matches(value + " \\(\\d+\\)"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(String.format(
                            "Failed to get an allowed priority matching specified value '%s'. Allowed values: %s", value,
                            allowedValues.stream().map(v -> ((CustomFieldOption) v).getValue()).collect(Collectors.joining(", ")))));
        } else if (Objects.equals("option", fieldType)) {
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
        throw new IllegalStateException("Unexpected field type for setting by ValuesExtractor: " + fieldType);
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
                        children, j.has("child") ? parseOption(j.get("child"), fieldName + ", available value child " + j.get("child")) : null);
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