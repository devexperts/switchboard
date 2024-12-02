/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.integrations.jira

import com.atlassian.jira.rest.client.api.domain.CimFieldInfo
import com.atlassian.jira.rest.client.api.domain.CimIssueType
import com.atlassian.jira.rest.client.api.domain.CustomFieldOption
import com.atlassian.jira.rest.client.api.domain.FieldSchema
import com.atlassian.jira.rest.client.api.domain.StandardOperation
import com.atlassian.jira.rest.client.api.domain.input.IssueInput
import com.devexperts.switchboard.api.Integration
import com.devexperts.switchboard.entities.Attributes
import com.devexperts.switchboard.entities.valuesupplier.AttributeValuesExtractor
import com.devexperts.switchboard.entities.valuesupplier.ConstantValuesExtractor
import com.devexperts.switchboard.entities.valuesupplier.FormattingValuesExtractor
import com.devexperts.switchboard.utils.JacksonUtils
import com.devexperts.switchboard.utils.TestUtils
import com.fasterxml.jackson.databind.ObjectMapper
import org.codehaus.jettison.json.JSONObject
import org.junit.Test
import org.mockito.Mockito

class JiraIntegrationTest {
    private static final String QUERY_1 = "query1"
    private static final List<String> OUTPUT_1 = ["ABC-1", "ABC-2", "ABC-3"]
    private static final String QUERY_2 = "query2"
    private static final List<String> OUTPUT_2 = ["ABC-3", "ABC-4", "ABC-5"]
    private static final String QUERY_3 = "query3"
    private static final List<String> OUTPUT_3 = []
    private static final String ATTRIBUTE_KEY = "jiraId"
    private static final String ATTRIBUTE_VALUE_KEY = ""
    private static final List<com.devexperts.switchboard.entities.Test> TEST_LIST1 = asJiraIdTests(["ABC-1", "ABC-2", "ABC-3", "ABC-4", "ABC-5", "ABC-6"])
    private static final List<com.devexperts.switchboard.entities.Test> TEST_LIST2 = asJiraIdTests(["ABC-1", "ABC-3", "ABC-4", "ABC-5", "ABC-6"])

    @Test
    void testJqlQueryTestFilter() {
        JqlQueryTestFilter filter1 = new JqlQueryTestFilter("TestJqlQueryTestFilter_1", QUERY_1, ATTRIBUTE_KEY, ATTRIBUTE_VALUE_KEY)
        JqlQueryTestFilter filter2 = new JqlQueryTestFilter("TestJqlQueryTestFilter_2", QUERY_2, ATTRIBUTE_KEY, ATTRIBUTE_VALUE_KEY)
        JqlQueryTestFilter filter3 = new JqlQueryTestFilter("TestJqlQueryTestFilter_3", QUERY_3, ATTRIBUTE_KEY, ATTRIBUTE_VALUE_KEY)
        JiraIntegrationFeatures mock = getMockIntegrationFeatures()
        [filter1, filter2, filter3].each { it.init(mock) }

        assert filter1.filter(TEST_LIST1) == asJiraIdTests(OUTPUT_1).findAll { TEST_LIST1.contains(it) }
        assert filter1.filter(TEST_LIST2) == asJiraIdTests(OUTPUT_1).findAll { TEST_LIST2.contains(it) }
        assert filter2.filter(TEST_LIST1) == asJiraIdTests(OUTPUT_2).findAll { TEST_LIST1.contains(it) }
        assert filter2.filter(TEST_LIST2) == asJiraIdTests(OUTPUT_2).findAll { TEST_LIST2.contains(it) }
        assert filter3.filter(TEST_LIST1) == asJiraIdTests([])
        assert filter3.filter(TEST_LIST1) == asJiraIdTests([])
    }

    @Test
    void testIntegrationLoad() {
        URL res = Thread.currentThread().getContextClassLoader().getResource("JiraIntegration.json")
        def integration = JacksonUtils.getMapper().readValue(res, Integration.class)
        assert integration.getIdentifier() == "Jira_integration_1"
        assert TestUtils.getField(integration, "login") == "user"
        assert TestUtils.getField(integration, "password") == "password"
        assert TestUtils.getField(integration, "uri") == "https://jira.somwhere.elsewhere.com/"
        assert TestUtils.getIntegrationComponents(integration, "testExtractors").isEmpty()
        assert TestUtils.getIntegrationComponents(integration, "testProcessors").isEmpty()
        assert TestUtils.getIntegrationComponents(integration, "testFilters").size() == 2
        assert TestUtils.getIntegrationComponents(integration, "testSplitters").isEmpty()
        assert TestUtils.getIntegrationComponents(integration, "testRunConsumers").isEmpty()
    }

    private static List<com.devexperts.switchboard.entities.Test> asJiraIdTests(Collection<String> ids) {
        return ids.collect { asJiraIdTest(it) }
    }

    private static com.devexperts.switchboard.entities.Test asJiraIdTest(String id) {
        return new com.devexperts.switchboard.entities.Test(id.replace("-", "_"),
                Attributes.newBuilder().putAttribute(ATTRIBUTE_KEY, ATTRIBUTE_VALUE_KEY, id).build(),
                com.devexperts.switchboard.entities.Test::getIdentifier
        )
    }

    @Test
    void xRayGenericTestsCreatingConsumerTest() {
        String lorem = TestUtils.getTextSample(4)
        XRayGenericTestsCreatingConsumer consumer = new XRayGenericTestsCreatingConsumer("test-consumer-test", "QEXQA", "Test",
                new AttributeValuesExtractor("TestCase", "summary", ","), [
                "Component/s"                      : new AttributeValuesExtractor("TestCase", "components", ","),
                "Trading Type"                     : new AttributeValuesExtractor("TestCase", "tradingType", ","),
                "Severity"                         : new AttributeValuesExtractor("TestCase", "severity", ","),
                "Description"                      : new AttributeValuesExtractor("comments", "", "\n"),
                "Labels"                           : new AttributeValuesExtractor("TestCase", "labels", ","),
                "Test Sets association with a Test": new AttributeValuesExtractor("TestSet", "", ","),
                "Generic Test Definition"          : new FormattingValuesExtractor("%s.%s.%s", [
                        new AttributeValuesExtractor(Attributes.LOCATION_PROP, "package", ","),
                        new AttributeValuesExtractor(Attributes.LOCATION_PROP, "class", ","),
                        new AttributeValuesExtractor(Attributes.LOCATION_PROP, "method", ",")])
        ] as Map, [
                "Component/s" : new ConstantValuesExtractor(["Component1", "Component2"] as Set, ","),
                "Trading Type": new ConstantValuesExtractor("None"),
                "Severity"    : new ConstantValuesExtractor("Functional"),
                "Description" : new ConstantValuesExtractor(lorem),
                "Test Type"   : new ConstantValuesExtractor("Automated")
        ] as Map, null, false, true)
        consumer.init(getMockIntegrationFeatures())

        ObjectMapper mapper = JacksonUtils.getMapper()
        def tests = mapper.readValue(Thread.currentThread().getContextClassLoader().getResource("SampleTests.json"),
                mapper.getTypeFactory().constructCollectionType(List.class, com.devexperts.switchboard.entities.Test.class)) as List
        List<IssueInput> list = new ArrayList<>()
        for (com.devexperts.switchboard.entities.Test test : (tests as List<com.devexperts.switchboard.entities.Test>)) {
            //noinspection GroovyAccessibility
            list.add(consumer.createInput(test))
        }
        assertInput(list.get(0), MAP0)
        assertInput(list.get(1), MAP1)
        assertInput(list.get(2), MAP2)
    }

    private static JiraIntegrationFeatures getMockIntegrationFeatures() {
        JiraIntegrationFeatures features = new JiraIntegrationFeatures(new URI("https://jira.somwhere.elsewhere.com/"),
                "login", "password", 300, 100, 10, 10)
        JiraIntegrationFeatures featuresMock = Mockito.spy(features)
        Mockito.doReturn(OUTPUT_1).when(featuresMock).searchForIssueKeys(QUERY_1)
        Mockito.doReturn(OUTPUT_2).when(featuresMock).searchForIssueKeys(QUERY_2)
        Mockito.doReturn(OUTPUT_3).when(featuresMock).searchForIssueKeys(QUERY_3)
        Mockito.doReturn(getMockType()).when(featuresMock).getIssueType(Mockito.any(), Mockito.any())
        Mockito.doReturn([]).when(featuresMock).searchForIssues(Mockito.any())

        return featuresMock
    }

    private static assertInput(IssueInput generated, Map<String, String> expected) {
        assert generated.getProperties().isEmpty()
        assert generated.getFields().size() == expected.size()
        for (String key : generated.fields.keySet()) {
            assert generated.getField(key).toString() == expected.get(key)
        }
    }
    private static final Map<String, String> MAP0 = [
            "summary"          : "FieldInput{id=summary, value=This is some test summary}",
            "issuetype"        : "FieldInput{id=issuetype, value=ComplexIssueInputFieldValue{valuesMap={id=10900}}}",
            "components"       : "FieldInput{id=components, value=[ComplexIssueInputFieldValue{valuesMap={name=Component1}}]}",
            "customfield_17970": "FieldInput{id=customfield_17970, value=ComplexIssueInputFieldValue{valuesMap={id=25665, value=Automated}}}",
            "project"          : "FieldInput{id=project, value=ComplexIssueInputFieldValue{valuesMap={key=QEXQA}}}",
            "description"      : "FieldInput{id=description, value= STEP: do something 1\n STEP: do something else 1\n STEP: and another thing 1}",
            "customfield_18071": "FieldInput{id=customfield_18071, value=ComplexIssueInputFieldValue{valuesMap={id=28972, value=None}}}",
            "customfield_17973": "FieldInput{id=customfield_17973, value=com.devexperts.switchboard.integrations.test.example.JUnitJiraTests.test1}",
            "customfield_10000": "FieldInput{id=customfield_10000, value=ComplexIssueInputFieldValue{valuesMap={id=10517, value=Functional}}}",
            "labels"           : "FieldInput{id=labels, value=[One, Two, Three]}",
            "timetracking"     : "FieldInput{id=timetracking, value=ComplexIssueInputFieldValue{valuesMap={originalEstimate=1m}}}"
    ]
    private static final Map<String, String> MAP1 = [
            "summary"          : "FieldInput{id=summary, value=This is another test summary}",
            "issuetype"        : "FieldInput{id=issuetype, value=ComplexIssueInputFieldValue{valuesMap={id=10900}}}",
            "components"       : "FieldInput{id=components, value=[ComplexIssueInputFieldValue{valuesMap={name=Component2}}]}",
            "customfield_17970": "FieldInput{id=customfield_17970, value=ComplexIssueInputFieldValue{valuesMap={id=25665, value=Automated}}}",
            "project"          : "FieldInput{id=project, value=ComplexIssueInputFieldValue{valuesMap={key=QEXQA}}}",
            "description"      : "FieldInput{id=description, value= STEP: do something else 2\n STEP: do something 2\n STEP: and another thing 2}",
            "customfield_18071": "FieldInput{id=customfield_18071, value=ComplexIssueInputFieldValue{valuesMap={id=28972, value=None}}}",
            "customfield_17973": "FieldInput{id=customfield_17973, value=com.devexperts.switchboard.integrations.test.example.JUnitJiraTests.test2}",
            "customfield_10000": "FieldInput{id=customfield_10000, value=ComplexIssueInputFieldValue{valuesMap={id=10518, value=Showstopper}}}",
            "labels"           : "FieldInput{id=labels, value=[Four]}",
            "timetracking"     : "FieldInput{id=timetracking, value=ComplexIssueInputFieldValue{valuesMap={originalEstimate=1m}}}"
    ]
    private static final Map<String, String> MAP2 = [
            "summary"          : "FieldInput{id=summary, value=Guess what? Another test summary!}",
            "issuetype"        : "FieldInput{id=issuetype, value=ComplexIssueInputFieldValue{valuesMap={id=10900}}}",
            "components"       : "FieldInput{id=components, value=[ComplexIssueInputFieldValue{valuesMap={name=Component1}}, ComplexIssueInputFieldValue{valuesMap={name=Component2}}]}",
            "customfield_17970": "FieldInput{id=customfield_17970, value=ComplexIssueInputFieldValue{valuesMap={id=25665, value=Automated}}}",
            "project"          : "FieldInput{id=project, value=ComplexIssueInputFieldValue{valuesMap={key=QEXQA}}}",
            "customfield_18071": "FieldInput{id=customfield_18071, value=ComplexIssueInputFieldValue{valuesMap={id=28972, value=None}}}",
            "description"      : "FieldInput{id=description, value=${TestUtils.getTextSample(4)}}",
            "customfield_17973": "FieldInput{id=customfield_17973, value=com.devexperts.switchboard.integrations.test.example.JUnitJiraTests.test3}",
            "customfield_10000": "FieldInput{id=customfield_10000, value=ComplexIssueInputFieldValue{valuesMap={id=10519, value=Minor functional}}}",
            "labels"           : "FieldInput{id=labels, value=[Five, Six]}",
            "timetracking"     : "FieldInput{id=timetracking, value=ComplexIssueInputFieldValue{valuesMap={originalEstimate=1m}}}"
    ]

    private static CimIssueType getMockType() {
        Long id = 10900
        URI uri = new URI("https://jira.somwhere.elsewhere.com/rest/api/latest/issuetype/" + id)
        String name = "Test"
        boolean isSubtask = false
        String description = "Mock Test issue type"
        Map<String, CimFieldInfo> fields = [
                "Test Type"              : new CimFieldInfo("customfield_17970", false, "Test Type",
                        new FieldSchema("option", null, null,
                                "com.xpandit.plugins.xray:test-type-custom-field", 17970), [StandardOperation.SET] as Set,
                        [
                                new JSONObject(["self" : "https://jira.in.devexperts.com/rest/api/2/customFieldOption/25574",
                                                "value": "Manual", "id": "25574"]),
                                new JSONObject(["self" : "https://jira.in.devexperts.com/rest/api/2/customFieldOption/25575",
                                                "value": "Cucumber", "id": "25575"]),
                                new JSONObject(["self" : "https://jira.in.devexperts.com/rest/api/2/customFieldOption/25576",
                                                "value": "Generic", "id": "25576"]),
                                new JSONObject(["self" : "https://jira.in.devexperts.com/rest/api/2/customFieldOption/25665",
                                                "value": "Automated", "id": "25665"]),
                        ], null),
                "Trading Type"           : new CimFieldInfo("customfield_18071", true, "Trading Type",
                        new FieldSchema("option", null, null,
                                "com.atlassian.jira.plugin.system.customfieldtypes:select", 18071),
                        [StandardOperation.SET] as Set, [new CustomFieldOption(28972,
                        new URI("https://jira.in.devexperts.com/rest/api/2/customFieldOption/28972"),
                        "None", [], null)],
                        null),
                "Generic Test Definition": new CimFieldInfo("customfield_17973", false, "Generic Test Definition",
                        new FieldSchema("string", null, null,
                                "com.xpandit.plugins.xray:path-editor-custom-field", 17973),
                        [StandardOperation.SET] as Set, null,
                        null),
                "Severity"               : new CimFieldInfo("customfield_10000", true, "Severity",
                        new FieldSchema("option", null, null,
                                "com.atlassian.jira.plugin.system.customfieldtypes:select", 10000),
                        [StandardOperation.SET] as Set,
                        [
                                new CustomFieldOption(10518,
                                        new URI("https://jira.in.devexperts.com/rest/api/2/customFieldOption/10518"),
                                        "Showstopper", [], null),
                                new CustomFieldOption(10517,
                                        new URI("https://jira.in.devexperts.com/rest/api/2/customFieldOption/10517"),
                                        "Functional", [], null),
                                new CustomFieldOption(10519,
                                        new URI("https://jira.in.devexperts.com/rest/api/2/customFieldOption/10519"),
                                        "Minor functional", [], null),
                                new CustomFieldOption(10520,
                                        new URI("https://jira.in.devexperts.com/rest/api/2/customFieldOption/10520"),
                                        "Usability", [], null),
                                new CustomFieldOption(10521,
                                        new URI("https://jira.in.devexperts.com/rest/api/2/customFieldOption/10521"),
                                        "Glitch", [], null),
                                new CustomFieldOption(25584,
                                        new URI("https://jira.in.devexperts.com/rest/api/2/customFieldOption/25584"),
                                        "Performance", [], null)
                        ], null),
                "Labels"                 : new CimFieldInfo("labels", false, "Labels",
                        new FieldSchema("array", "string", "labels", null, null),
                        [StandardOperation.SET, StandardOperation.ADD, StandardOperation.REMOVE] as Set, null,
                        new URI("https://jira.in.devexperts.com/rest/api/1.0/labels/suggest?query=")),
                "Time Tracking"          : new CimFieldInfo("timetracking", true, "Time Tracking",
                        new FieldSchema("timetracking", null, null, null, null),
                        [StandardOperation.SET] as Set, null, null)

        ]
        return new CimIssueType(uri, id, name, isSubtask, description, uri, fields)
    }
}