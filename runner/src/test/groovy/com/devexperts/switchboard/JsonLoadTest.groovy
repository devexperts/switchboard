/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard

import com.devexperts.switchboard.api.Integration
import com.devexperts.switchboard.api.TestSplitter
import com.devexperts.switchboard.entities.TestRun
import com.devexperts.switchboard.utils.ClassLoaderUtils
import com.devexperts.switchboard.utils.FileUtils
import com.devexperts.switchboard.utils.JacksonUtils
import com.devexperts.switchboard.utils.TestUtils
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException
import org.junit.Test

@SuppressWarnings("GroovyAccessibility")
class JsonLoadTest {
    private static final URL javaparserUrl = TestUtils.findLibUrl("../integrations/javaparser/build/libs/")
    private static final URL bytecodeParserUrl = TestUtils.findLibUrl("../integrations/bytecodeparser/build/libs/")
    private static final URL cukesUrl = TestUtils.findLibUrl("../integrations/cukes/build/libs/")
    private static final ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader()
    private static final ClassLoader javaparserClassLoader = ClassLoaderUtils.getClassloader("javaparserClassLoader", false, javaparserUrl)
    private static final ClassLoader bytecodeParserClassLoader = ClassLoaderUtils.getClassloader("bytecodeParserClassLoader", false, bytecodeParserUrl)
    private static final ClassLoader cukesClassLoader = ClassLoaderUtils.getClassloader("cukesClassLoader", false, cukesUrl)

    @Test
    void singleEntityLoadTest() {
        TestSplitter countSplitter = JacksonUtils.getMapper().readValue(threadClassLoader.getResource("SimpleSplitter.json"), TestSplitter.class)
        assert countSplitter.getIdentifier() == "TestCountSplitter-1"
        assert countSplitter.count == 5

    }

    @Test
    void entityListLoadTest() {
        def mapper = JacksonUtils.getMapper()
        List<TestSplitter> splitters = mapper.readValue(threadClassLoader.getResource("ThreeSplitters.json"),
                mapper.getTypeFactory().constructCollectionType(List.class, TestSplitter.class))
        assert splitters.size() == 3
        assert splitters.get(0).getIdentifier() == "TestCountSplitter-1"
        assert splitters.get(0).count == 2
        assert splitters.get(1).getIdentifier() == "TestCountSplitter-2"
        assert splitters.get(1).count == 4
        assert splitters.get(2).getIdentifier() == "TestCountSplitter-3"
        assert splitters.get(2).count == 6
    }

    @Test
    void featurelessIntegrationLoadTest() {
        URL res = threadClassLoader.getResource("FeaturelessIntegration.json")
        def integration = JacksonUtils.getMapper().readValue(res, Integration.class)
        assert integration.getIdentifier() == "FeaturelessIntegration-1"
        assert TestUtils.getIntegrationComponents(integration, "testExtractors").isEmpty()
        assert TestUtils.getIntegrationComponents(integration, "testProcessors").isEmpty()
        assert TestUtils.getIntegrationComponents(integration, "testFilters").size() == 1
        assert TestUtils.getIntegrationComponents(integration, "testFilters").find { it.identifier == "AttributesFilter-1" } != null
        assert TestUtils.getIntegrationComponents(integration, "testSplitters").size() == 2
        assert TestUtils.getIntegrationComponents(integration, "testSplitters").find { it.identifier == "AttributeSingleValueSplitter-1" } != null
        assert TestUtils.getIntegrationComponents(integration, "testSplitters").find { it.identifier == "AttributeSingleValueSplitter-1" } != null
        assert TestUtils.getIntegrationComponents(integration, "testRunConsumers").isEmpty()
    }

    @Test
    void javaparserIntegrationLoadTest() {
        URL res = threadClassLoader.getResource("JavaParserIntegration.json")
        def integration = JacksonUtils.getMapper(javaparserClassLoader).readValue(res, Integration.class)
        assert integration.getIdentifier() == "JavaParserIntegration-1"
        assert TestUtils.getIntegrationComponents(integration, "testExtractors").size() == 1
        assert TestUtils.getIntegrationComponents(integration, "testExtractors").find { it.identifier == "JunitExtractor-1" } != null
        assert TestUtils.getIntegrationComponents(integration, "testProcessors").isEmpty()
        assert TestUtils.getIntegrationComponents(integration, "testFilters").size() == 1
        assert TestUtils.getIntegrationComponents(integration, "testFilters").find { it.identifier == "AttributesFilter-1" } != null
        assert TestUtils.getIntegrationComponents(integration, "testSplitters").isEmpty()
        assert TestUtils.getIntegrationComponents(integration, "testRunConsumers").isEmpty()
    }

    @Test
    void bytecodeParserIntegrationLoadTest() {
        URL res = threadClassLoader.getResource("BytecodeParserIntegration.json")
        def integration = JacksonUtils.getMapper(bytecodeParserClassLoader).readValue(res, Integration.class)
        assert integration.getIdentifier() == "BytecodeParserIntegration-1"
        assert TestUtils.getIntegrationComponents(integration, "testExtractors").size() == 1
        assert TestUtils.getIntegrationComponents(integration, "testExtractors").find { it.identifier == "JunitExtractor-2" } != null
        assert TestUtils.getIntegrationComponents(integration, "testProcessors").isEmpty()
        assert TestUtils.getIntegrationComponents(integration, "testFilters").size() == 1
        assert TestUtils.getIntegrationComponents(integration, "testFilters").find { it.identifier == "AttributesFilter-1" } != null
        assert TestUtils.getIntegrationComponents(integration, "testSplitters").isEmpty()
        assert TestUtils.getIntegrationComponents(integration, "testRunConsumers").isEmpty()
    }

    @Test
    void cukesIntegrationLoadTest() {
        URL res = threadClassLoader.getResource("CukesIntegration.json")
        def integration = JacksonUtils.getMapper(cukesClassLoader).readValue(res, Integration.class)
        assert integration.getIdentifier() == "CukesIntegration-1"
        assert TestUtils.getIntegrationComponents(integration, "testExtractors").size() == 1
        assert TestUtils.getIntegrationComponents(integration, "testExtractors").find { it.identifier == "CukesTestExtractor-1" } != null
        assert TestUtils.getIntegrationComponents(integration, "testProcessors").isEmpty()
        assert TestUtils.getIntegrationComponents(integration, "testFilters").size() == 1
        assert TestUtils.getIntegrationComponents(integration, "testFilters").find { it.identifier == "AttributesFilter-1" } != null
        assert TestUtils.getIntegrationComponents(integration, "testSplitters").isEmpty()
        assert TestUtils.getIntegrationComponents(integration, "testRunConsumers").isEmpty()
    }

    @Test
    void integrationLoadTest1Neg() {
        try {
            JacksonUtils.getMapper(javaparserClassLoader)
                    .readValue(threadClassLoader.getResource("CukesIntegration.json"), Integration.class)
        } catch (Exception e) {
            assert e instanceof InvalidTypeIdException
        }
    }

    @Test
    void integrationLoadTest2Neg() {
        try {
            JacksonUtils.getMapper(cukesClassLoader)
                    .readValue(threadClassLoader.getResource("JavaParserIntegration.json"), Integration.class)
        } catch (Exception e) {
            assert e instanceof InvalidTypeIdException
        }
    }

    @Test
    void testLibUrlsList() {
        List<URL> urls = FileUtils.listFileURLsRecursively("**/libs/*-all.jar",
                ["../integrations/javaparser/",
                 "../integrations/cukes/"])
        assert urls.size() == 2
        assert urls.contains(cukesUrl)
        assert urls.contains(javaparserUrl)
    }

    @Test
    void integrationConfigTest() {
        URL res = threadClassLoader.getResource("IntegrationConfig.json")
        List<URL> urls = FileUtils.listFileURLsRecursively("**/libs/*-all.jar",
                ["../integrations/javaparser/", "../integrations/cukes/"])
        Map<Integration, ClassLoader> integrations = JacksonUtils.parseIntegrations(res, urls)
        assert integrations.size() == 3
        Integration integration1 = integrations.keySet().find { it.identifier == "FeaturelessIntegration-1" }

        assert TestUtils.getIntegrationComponents(integration1, "testExtractors").size() == 2
        assert TestUtils.getIntegrationComponents(integration1, "testExtractors").find { it.identifier == "CukesTestExtractor-1" } != null
        assert TestUtils.getIntegrationComponents(integration1, "testExtractors").find { it.identifier == "JunitExtractor-1" } != null
        assert TestUtils.getIntegrationComponents(integration1, "testProcessors").isEmpty()
        assert TestUtils.getIntegrationComponents(integration1, "testFilters").size() == 1
        assert TestUtils.getIntegrationComponents(integration1, "testFilters").find { it.identifier == "AttributesFilter-1" } != null
        assert TestUtils.getIntegrationComponents(integration1, "testSplitters").size() == 2
        assert TestUtils.getIntegrationComponents(integration1, "testSplitters").find { it.identifier == "AttributeKeySplitter-1" } != null
        assert TestUtils.getIntegrationComponents(integration1, "testSplitters").find { it.identifier == "AttributeSingleValueSplitter-1" } != null
        assert TestUtils.getIntegrationComponents(integration1, "testRunConsumers").size() == 1
        assert TestUtils.getIntegrationComponents(integration1, "testRunConsumers").find { it.identifier == "SystemPropsTestConsumer-1" } != null

        Integration integration2 = integrations.keySet().find { it.identifier == "CukesIntegration-1" }
        assert TestUtils.getIntegrationComponents(integration2, "testExtractors").size() == 1
        assert TestUtils.getIntegrationComponents(integration2, "testExtractors").find { it.identifier == "CukesTestExtractor-1" } != null
        assert TestUtils.getIntegrationComponents(integration2, "testProcessors").isEmpty()
        assert TestUtils.getIntegrationComponents(integration2, "testFilters").size() == 1
        assert TestUtils.getIntegrationComponents(integration2, "testFilters").find { it.identifier == "AttributesFilter-2" } != null
        assert TestUtils.getIntegrationComponents(integration2, "testSplitters").isEmpty()
        assert TestUtils.getIntegrationComponents(integration2, "testRunConsumers").isEmpty()

        Integration integration3 = integrations.keySet().find { it.identifier == "JavaParserIntegration-1" }
        assert TestUtils.getIntegrationComponents(integration3, "testExtractors").size() == 1
        assert TestUtils.getIntegrationComponents(integration3, "testExtractors").find { it.identifier == "JunitExtractor-1" } != null
        assert TestUtils.getIntegrationComponents(integration3, "testProcessors").isEmpty()
        assert TestUtils.getIntegrationComponents(integration3, "testFilters").size() == 1
        assert TestUtils.getIntegrationComponents(integration3, "testFilters").find { it.identifier == "AttributesFilter-3" } != null
        assert TestUtils.getIntegrationComponents(integration3, "testSplitters").isEmpty()
        assert TestUtils.getIntegrationComponents(integration3, "testRunConsumers").isEmpty()
    }

    @Test
    void placeholdersConfigTest() {
        URL res = threadClassLoader.getResource("JiraIntegrationConfig.json")
        List<URL> urls = FileUtils.listFileURLsRecursively("**/libs/*-all.jar", ["../integrations/jira/"])

        System.setProperty("some.jira_url-2", "https://jira.elsewhere.com")
        System.setProperty("login-2", "hiddenLogin2")
        System.setProperty("password-2", "hiddenPassword2")
        System.setProperty("login-3", "hiddenLogin3")

        Map<Integration, ClassLoader> integrations = JacksonUtils.parseIntegrations(res, urls)
        assert integrations.size() == 3
        Integration integration1 = integrations.keySet().find { it.identifier == "Plain config" }
        assert TestUtils.getField(integration1, "uri") == "https://jira.somewhere.com"
        assert TestUtils.getField(integration1, "login") == "explicitLogin"
        assert TestUtils.getField(integration1, "password") == "explicitPassword"

        Integration integration2 = integrations.keySet().find { it.identifier == "Placeholders" }
        assert TestUtils.getField(integration2, "uri") == "https://jira.elsewhere.com"
        assert TestUtils.getField(integration2, "login") == "hiddenLogin2"
        assert TestUtils.getField(integration2, "password") == "hiddenPassword2"

        Integration integration3 = integrations.keySet().find { it.identifier == "Escaped placeholders" }
        assert TestUtils.getField(integration3, "uri") == "%some.jira_url-2%"
        assert TestUtils.getField(integration3, "login") == "%login-3%"
        assert TestUtils.getField(integration3, "password") == "%password-3%"
    }

    @Test
    void integrationRunTest() {
        URL res = threadClassLoader.getResource("IntegrationConfig.json")
        new IntegrationsRunner().run("-c", new File(res.file).absolutePath,
                "-i", "../integrations/javaparser/", "../integrations/cukes/")
        String resultPropVal = System.getProperty("runResult")
        List<TestRun> result = JacksonUtils.getMapper().readValue(resultPropVal, List.class)
        assert result.size() == 5
        assert result[0].tests.size() == 8
        assert result[1].tests.size() == 3
        assert result[2].tests.size() == 1
        assert result[3].tests.size() == 3
        assert result[4].tests.size() == 2
    }
}