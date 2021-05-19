/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.integrations.teamcity


import com.devexperts.switchboard.entities.Attributes
import com.devexperts.switchboard.entities.TestRun
import com.devexperts.switchboard.entities.valuesupplier.AttributeKeyValuesExtractor
import com.devexperts.switchboard.entities.valuesupplier.AttributeValuesExtractor
import com.devexperts.switchboard.entities.valuesupplier.ConstantValuesExtractor
import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.model.AgentPool
import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.model.Build
import com.devexperts.switchboard.utils.JacksonUtils
import com.devexperts.switchboard.utils.TestUtils
import org.junit.Test
import org.mockito.Mockito

import java.util.function.Function

@SuppressWarnings("GroovyAccessibility")
class TeamCityIntegrationTest {
    private static final Function<com.devexperts.switchboard.entities.Test, String> TO_RUNNER_STRING_STUB = new Function<com.devexperts.switchboard.entities.Test, String>() {
        @Override
        String apply(com.devexperts.switchboard.entities.Test test) {
            return "id:" + test.identifier
        }
    }
    private static final TestRun TEST_RUN_1 = getTestRun("Test Run #1",
            ["test-1-1", "test-1-2", "test-1-3"], ["tag-1", "tag-2", "tag-3"], ["key1": "key1val1"])
    private static final TestRun TEST_RUN_2 = getTestRun("Test Run #2",
            ["test-2-1", "test-2-2", "test-2-3"], ["tag-1", "tag-3", "tag-3"], ["key1": "key1val2"])
    private static final TestRun TEST_RUN_3 = getTestRun("Test Run #3",
            ["test-3-1", "test-3-2", "test-3-3"], ["tag-2", "tag-3", "tag-3"], ["key1": "key1val3"])
    private static final TestRun TEST_RUN_4 = getTestRun("Test Run #4",
            ["test-4-1", "test-4-2", "test-4-3"], ["tag-1", "tag-2"], ["key2": "key2val1", "compatibleAgent": "Agent1"])
    private static final TestRun TEST_RUN_5 = getTestRun("Test Run #5",
            ["test-5-1", "test-5-2", "test-5-3"], ["tag-1", "tag-3"], ["key2": "key2val1"])
    private static final TestRun TEST_RUN_6 = getTestRun("Test Run #6",
            ["test-6-1", "test-6-2", "test-6-3"], ["tag-2", "tag-3"], ["key2": "key2val1"])
    private static final TestRun TEST_RUN_7 = getTestRun("Test Run #7",
            ["test-7-1", "test-7-2", "test-7-3"], ["tag-1"], ["key1": "key1val1", "key2": "key2val2"])
    private static final TestRun TEST_RUN_8 = getTestRun("Test Run #8",
            ["test-8-1", "test-8-2", "test-8-3"], ["tag-2"], ["key1": "key1val2", "key2": "key2val2", "compatibleAgent": "Agent2"])
    private static final TestRun TEST_RUN_9 = getTestRun("Test Run #9",
            ["test-9-1", "test-9-2", "test-9-3"], ["tag-3"], ["key1": "key1val3", "key2": "key2val2"])

    private static final List<TestRun> TEST_RUNS = [TEST_RUN_1, TEST_RUN_2, TEST_RUN_3, TEST_RUN_4, TEST_RUN_5,
                                                    TEST_RUN_6, TEST_RUN_7, TEST_RUN_8, TEST_RUN_9]

    @Test
    void testTcIntegrationLoad() {
        TeamCityIntegration integration = JacksonUtils.getMapper().readValue(
                Thread.currentThread().getContextClassLoader().getResource("TeamCityIntegration.json"),
                TeamCityIntegration.class)
        assert integration.getIdentifier() == "TeamcityIntegration-1"
        assert TestUtils.getField(integration, "login") == "user"
        assert TestUtils.getField(integration, "password") == "p@ssw0rd"
        assert TestUtils.getField(integration, "basePath") == "https://teamcity.somewhere.com"
        assert TestUtils.getIntegrationComponents(integration, "testExtractors").isEmpty()
        assert TestUtils.getIntegrationComponents(integration, "testProcessors").isEmpty()
        assert TestUtils.getIntegrationComponents(integration, "testFilters").isEmpty()
        assert TestUtils.getIntegrationComponents(integration, "testSplitters").isEmpty()
        assert TestUtils.getIntegrationComponents(integration, "testRunConsumers").size() == 1
        def consumer = TestUtils.getIntegrationComponents(integration, "testRunConsumers")
                .find { it.identifier == "TcBuildTestRunConsumer-1" }
        assert consumer instanceof TcBuildTestRunConsumer
        assert consumer.identifier == "TcBuildTestRunConsumer-1"
        assert consumer.buildTypeId != null
        assert consumer.branchName != null
        assert consumer.defaultBranch
        assert consumer.agentStackingCriterion != null
        assert consumer.agentRequirements.size() == 0
        assert consumer.buildProperties.size() == 4
        assert consumer.buildTags.size() == 2
        assert consumer.moveToTop
    }

    @Test
    void testAgentRequirementsLocator() {
        TcBuildTestRunConsumer consumer = TcBuildTestRunConsumer.newBuilder()
                .buildTypeId(new ConstantValuesExtractor("buildTypeId"))
                .agentRequirements([
                        new AgentRequirement(new ConstantValuesExtractor("Agent-\\d+"), "system.agent.name", AgentParamMatchType.MATCHES),
                        new AgentRequirement(new AttributeKeyValuesExtractor("tag-1", ","), "other.agent.prop", AgentParamMatchType.STARTS_WITH),
                        new AgentRequirement(new AttributeValuesExtractor("key1", "", ","), "some.agent.prop", AgentParamMatchType.EQUALS),
                ])
                .build()
        for (TestRun run : TEST_RUNS) {
            StringBuilder expected = new StringBuilder("compatible:(buildType:(id:buildTypeId))")
                    .append(",parameter:(name:system.agent.name,value:Agent-\\d+,matchType:matches)")
            if (run.attributes.attributes.containsKey("tag-1"))
                expected.append(",parameter:(name:other.agent.prop,value:tag-1,matchType:starts-with)")
            if (run.attributes.attributes.containsKey("key1"))
                expected.append(",parameter:(name:some.agent.prop,value:${run.attributes.getSingleAttributeValue("key1", "").get()},matchType:equals)")

            assert consumer.getAgentLocator(run) == expected.toString()
        }
    }

    @Test
    void testTcIntegrationConsumerRun() {
        TeamCityIntegration integration = JacksonUtils.getMapper().readValue(
                Thread.currentThread().getContextClassLoader().getResource("TeamCityIntegration.json"),
                TeamCityIntegration.class)
        def mockIntegration = Mockito.spy(integration)
        def mockFeatures = getMockIntegrationFeatures()
        Mockito.doReturn(mockFeatures).when(mockIntegration).getIntegrationFeatures()
        mockIntegration.init()

        def consumer = TestUtils.getIntegrationComponents(mockIntegration, "testRunConsumers")
                .find { it.identifier == "TcBuildTestRunConsumer-1" }
        assert consumer instanceof TcBuildTestRunConsumer
        def builds = consumer.doAccept(TEST_RUNS) as Map<TestRun, Build>

        assert builds.size() == TEST_RUNS.size()
        for (TestRun run : TEST_RUNS) {
            Build build = builds.get(run)
            assert build.buildTypeId == "DXAutoQA_SwitchboardTest"
            assert build.branchName == run.identifier
            assert build.defaultBranch

            assert build.getProperties().property.find { it.name == "buildProp1" }.value == "buildPropVal1"
            assert build.getProperties().property.find { it.name == "testString" }.value.split(", ").sort().toList()
                    == run.tests.collect { it.toRunnerString() }.sort()
            assert build.getProperties().property.find { it.name == "verbosy" }.value
                    == ("The quick brown ${run.identifier} jumps over the lazy DXAutoQA_SwitchboardTest.")
            if (run.attributes.attributes.containsKey("key1")) {
                assert build.getProperties().property.size() == 4
                assert build.getProperties().property.find { it.name == "buildProp2" }.value
                        == run.attributes.getSingleAttributeValue("key1", "").get()
            } else {
                assert build.getProperties().property.size() == 3
            }

            int count = 0
            if (run.attributes.attributes.containsKey("tag-1")) {
                assert build.tags.tag.find { it.name == "tag-1" } != null
                count++
            }
            if (run.attributes.attributes.containsKey("tag-2")) {
                assert build.tags.tag.find { it.name == "tag-2" } != null
                count++
            }
            assert count > 0 ? build.tags.tag.size() == count : build.tags.tag == null

            assert build.agent != null
        }

        // .agentStackingCriterion(new AttributeValueHolder("key2", ""))
        // e.g. runs with same key2 value have same agent:
        assert builds.get(TEST_RUN_4).agent == builds.get(TEST_RUN_5).agent
        assert builds.get(TEST_RUN_4).agent == builds.get(TEST_RUN_6).agent
        assert builds.get(TEST_RUN_7).agent == builds.get(TEST_RUN_8).agent
        assert builds.get(TEST_RUN_8).agent == builds.get(TEST_RUN_9).agent
    }

    private static TeamCityIntegrationFeatures getMockIntegrationFeatures() {
        TeamCityIntegrationFeatures featuresMock = Mockito.mock(TeamCityIntegrationFeatures.class)
        Mockito.when(featuresMock.queueNewBuild(Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(0))

        URL res = Thread.currentThread().getContextClassLoader().getResource("MockAgentsPool.json")
        AgentPool mockPool = JacksonUtils.getMapper().readValue(res, AgentPool.class)
        Mockito.when(featuresMock.getAgentPool(Mockito.any(), Mockito.any())).thenReturn(mockPool)
        return featuresMock
    }

    private static TestRun getTestRun(String identifier, List<String> testIdentifiers,
                                      List<String> singleAttributes, Map<String, String> valuedAttributes) {
        return TestRun.newBuilder()
                .identifier(identifier)
                .addTests(getTests(testIdentifiers))
                .mergeAttributes(getKeyAttributes(singleAttributes))
                .mergeAttributes(getKeyValueAttributes(valuedAttributes))
                .build()
    }

    private static List<com.devexperts.switchboard.entities.Test> getTests(List<String> testIdentifiers) {
        return testIdentifiers.collect { new com.devexperts.switchboard.entities.Test(it, Attributes.newBuilder().build(), TO_RUNNER_STRING_STUB) }
    }

    private static Attributes getKeyAttributes(List<String> singleAttributes) {
        def builder = Attributes.newBuilder()
        singleAttributes.each { builder.mergeAttribute(it, [:]) }
        return builder.build()
    }

    private static Attributes getKeyValueAttributes(Map<String, String> valuedAttributes) {
        def builder = Attributes.newBuilder()
        valuedAttributes.each { builder.mergeAttribute(it.key, "", it.value) }
        return builder.build()
    }
}