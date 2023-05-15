/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.sandbox.examples.jira;

import com.devexperts.switchboard.api.ComponentReference;
import com.devexperts.switchboard.api.Integration;
import com.devexperts.switchboard.api.TestRunConsumer;
import com.devexperts.switchboard.entities.Attributes;
import com.devexperts.switchboard.entities.Pair;
import com.devexperts.switchboard.entities.attributes.AttributeIsPresent;
import com.devexperts.switchboard.entities.attributes.AttributeValueMatches;
import com.devexperts.switchboard.entities.valuesupplier.AttributeValuesExtractor;
import com.devexperts.switchboard.entities.valuesupplier.BlockFormattingValuesExtractor;
import com.devexperts.switchboard.entities.valuesupplier.ConditionalValueExtractor;
import com.devexperts.switchboard.entities.valuesupplier.ConstantValuesExtractor;
import com.devexperts.switchboard.entities.valuesupplier.EnumeratingTestDescriptionFormatter;
import com.devexperts.switchboard.entities.valuesupplier.FormattingValuesExtractor;
import com.devexperts.switchboard.entities.valuesupplier.TestValuesExtractor;
import com.devexperts.switchboard.entities.valuesupplier.ValuesExtractor;
import com.devexperts.switchboard.integrations.javaparser.JavaParserIntegration;
import com.devexperts.switchboard.integrations.jira.JiraIntegration;
import com.devexperts.switchboard.integrations.jira.JiraIntegrationFeatures;
import com.devexperts.switchboard.integrations.jira.XRayGenericTestsCreatingConsumer;
import com.devexperts.switchboard.sandbox.examples.junit.JunitExtract;
import com.devexperts.switchboard.sandbox.utils.SandboxUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class TestCreatingConsumerExample {

    private static final String TEST_DETAILS = "TestDetails";
    private static final String SEVERITY = "severity";
    private static final String PRIORITY = "priority";

    private TestCreatingConsumerExample() {}

    public static void main(String[] args) throws Exception {
        // build integration
        JavaParserIntegration javaIntegration = JunitExtract.buildIntegration(false);
        JiraIntegration integration = buildIntegration(true);
        List<Integration<?>> integrations = Arrays.asList(integration, javaIntegration);
        // serialize and store configuration
        String config = SandboxUtils.writeIntegrationConfig(integrations);
        // init and run integration on sample tests
        SandboxUtils.prepareAndRunIntegration(config);
    }

    private static JiraIntegration buildIntegration(boolean runnable) {
        ComponentReference extractorReference = new ComponentReference<>("JunitExtractor", "ExampleJavaParserIntegration");
        return JiraIntegration.newBuilder()
                .identifier("JiraIntegration")
                .isRunnable(runnable)
                .testExtractors(Collections.singletonList(extractorReference))
                .testRunConsumers(Collections.singletonList(buildConsumer()))
                .uri("https://jira.somewhere.com")                          // base jira url
                .login("%testImportLogin%")                                 // this is a placeholder format. Add env variable 'testImportLogin=<your_login>'
                .password("%testImportPassword%")                           // this is a placeholder format. Add env variable 'testImportPassword=<your_password>'
                .socketTimeout(30)                                          // single request timeout, seconds
                .searchQueryBatch(100)                                      // maximum issues batch returned per request
                .build();
    }

    private static TestRunConsumer<JiraIntegrationFeatures> buildConsumer() {
        // we get summary as value of "DisplayName" annotation
        TestValuesExtractor summaryExtractor = new AttributeValuesExtractor("DisplayName", "", ",");

        // Next we specify what autotest values go to which Jira XRay fields:
        Map<String, TestValuesExtractor> fieldValuesExtractors = new HashMap<>();

        // Description consists of:
        List<Pair<String, ValuesExtractor>> blockExtractors = new ArrayList<>();
        // - Overview which we get from 'javadoc' section of 'comments' Attribute
        blockExtractors.add(Pair.of("*Overview:*", new AttributeValuesExtractor(Attributes.COMMENTS_PROP, Attributes.JAVADOC_PROP, "\n")));
        // - Preconditions which we get from 'preconditions_comments' section of 'comments' Attribute
        blockExtractors.add(Pair.of("*Preconditions:*",
            new AttributeValuesExtractor(Attributes.PRECONDITIONS_COMMENTS_PROP, Attributes.JAVADOC_PROP, "\n\n")));
        // - main section of Actions and Results which are formatted using a special EnumeratingTestDescriptionFormatter
        // Notice that section has no special header and thus has no key. Actions and result are parsed from formatted comments inside test ('preconditions_'comments' section of 'comments' Attribute).
        blockExtractors.add(Pair.of("",
            new EnumeratingTestDescriptionFormatter(new AttributeValuesExtractor(Attributes.COMMENTS_PROP, Attributes.COMMENTS_PROP, "\n"),
                "^Action: *(.+)", "^Result: *(.+)",            // specify format to parse for Actions and Results:
                "*Actions:*", "*Results:*",                    // specify headers for Actions and Results:
                "# ",                                          // a placeholder for Action/Result number
                "Check result (%s)",                           // format for a marker step linking action with result number
                "After step (%s) - ")));                       // format for result beginning
        // - Postconditions which we get from 'postconditions_comments' section of 'comments' Attribute
        blockExtractors.add(Pair.of("*Postconditions:*",
            new AttributeValuesExtractor(Attributes.POSTCONDITIONS_COMMENTS_PROP, Attributes.JAVADOC_PROP, "\n\n")));
        // Joining the whole Description into one block:
        fieldValuesExtractors.put("Description", new BlockFormattingValuesExtractor(blockExtractors, "\n", "\n\n", false));

        // Generic Test Definition is required for test run reporting:
        fieldValuesExtractors.put("Generic Test Definition", new FormattingValuesExtractor("%s.%s.%s", Arrays.asList(
            new AttributeValuesExtractor(Attributes.LOCATION_PROP, "package", ","),
            new AttributeValuesExtractor(Attributes.LOCATION_PROP, "class", ","),
            new AttributeValuesExtractor(Attributes.LOCATION_PROP, "method", ","))));

        // test set definition:
        fieldValuesExtractors.put("Test Sets association with a Test", new AttributeValuesExtractor("TestSet", "", ","));

        // Severity requires mapping between enums in tests and constants of Jira enum fields:
        fieldValuesExtractors.put("Severity", new ConditionalValueExtractor(Arrays.asList(
            Pair.of(new AttributeValueMatches(TEST_DETAILS, SEVERITY, "Severity.SHOWSTOPPER"),
                new ConstantValuesExtractor("Showstopper")),
            Pair.of(new AttributeValueMatches(TEST_DETAILS, SEVERITY, "Severity.MINOR_FUNCTIONAL"),
                new ConstantValuesExtractor("Minor functional")),
            Pair.of(new AttributeValueMatches(TEST_DETAILS, SEVERITY, "Severity.FUNCTIONAL"), new ConstantValuesExtractor("Functional")),
            Pair.of(new AttributeValueMatches(TEST_DETAILS, SEVERITY, "Severity.USABILITY"), new ConstantValuesExtractor("Usability")),
            Pair.of(new AttributeValueMatches(TEST_DETAILS, SEVERITY, "Severity.GLITCH"), new ConstantValuesExtractor("Glitch")),
            Pair.of(new AttributeValueMatches(TEST_DETAILS, SEVERITY, "Severity.PERFORMANCE"),
                new ConstantValuesExtractor("Performance"))),
            // the default value:
            new ConstantValuesExtractor("Functional")));

        // Priority requires mapping between enums in tests and constants of Jira enum fields:
        fieldValuesExtractors.put("Priority", new ConditionalValueExtractor(Arrays.asList(
            Pair.of(new AttributeValueMatches(TEST_DETAILS, PRIORITY, "Priority.ASAP"), new ConstantValuesExtractor("ASAP")),
            Pair.of(new AttributeValueMatches(TEST_DETAILS, PRIORITY, "Priority.CRITICAL"), new ConstantValuesExtractor("Critical")),
            Pair.of(new AttributeValueMatches(TEST_DETAILS, PRIORITY, "Priority.URGENT"), new ConstantValuesExtractor("Urgent")),
            Pair.of(new AttributeValueMatches(TEST_DETAILS, PRIORITY, "Priority.NORMAL"), new ConstantValuesExtractor("Normal")),
            Pair.of(new AttributeValueMatches(TEST_DETAILS, PRIORITY, "Priority.LOW_PRIORITY"),
                new ConstantValuesExtractor("Low priority"))),
            // the default value:
            new ConstantValuesExtractor("Normal")));

        fieldValuesExtractors.put("Test Type", new ConditionalValueExtractor(
            Collections.singletonList(Pair.of(new AttributeIsPresent("ManualTest"), new ConstantValuesExtractor("Manual"))),
            // the default value:
            new ConstantValuesExtractor("Automated")));

        fieldValuesExtractors.put("Unique ID", new AttributeValuesExtractor(TEST_DETAILS, "uniqueID", ","));
        fieldValuesExtractors.put("Time Tracking", new AttributeValuesExtractor(TEST_DETAILS, "manualEstimate", ","));
        fieldValuesExtractors.put("Test Repository Path", new AttributeValuesExtractor("RepositoryPath", "", ","));

        fieldValuesExtractors.put("Component/s", new ConditionalValueExtractor(Arrays.asList(
            Pair.of(new AttributeValueMatches(TEST_DETAILS, "Component", "Components.FRONTEND"), new ConstantValuesExtractor("Frontend")),
            Pair.of(new AttributeValueMatches(TEST_DETAILS, "Component", "Components.BACKEND"), new ConstantValuesExtractor("Backend"))),
            new ConstantValuesExtractor("dxCore")));

        Map<String, TestValuesExtractor> defaultFieldValues = new HashMap<>();
        defaultFieldValues.put("Test Type", new ConstantValuesExtractor("Automated"));
        defaultFieldValues.put("Time Tracking", new ConstantValuesExtractor("20"));

        defaultFieldValues.put("Assignee", new ConstantValuesExtractor("%testImportLogin%"));

        Pair<String, TestValuesExtractor> xrayFieldLinkToTestAttribute =
            Pair.of("Unique ID", new AttributeValuesExtractor(TEST_DETAILS, "uniqueID", ","));

        return new XRayGenericTestsCreatingConsumer(
                "Jira-Test-Creator",          // identifier
                "QWERTY",                     // Jira project id
                "Test",                       // Jira issue type for Test Case
                summaryExtractor,             // A complex extractor to construct summary
                fieldValuesExtractors,        // Field values extractors
                defaultFieldValues,           // Default field values if not found in tests
                true,                         // update existing XRay tests content or ignore changes
                xrayFieldLinkToTestAttribute
        );
    }
}
