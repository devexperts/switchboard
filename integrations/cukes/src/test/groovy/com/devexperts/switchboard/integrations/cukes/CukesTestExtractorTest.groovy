/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.integrations.cukes

import com.devexperts.switchboard.entities.Attributes
import org.junit.Test

class CukesTestExtractorTest {
    private static final String PATH = "src${File.separator}test${File.separator}resources${File.separator}test_classes"

    private static final CukesIntegrationFeatures features = new CukesIntegrationFeatures()

    private static final List<com.devexperts.switchboard.entities.Test> EXTRACTED_TESTS = Arrays.asList(
            createExtracted("Single scenario feature : Scenario 1 [11]", "test_classes${File.separator}Single_feature_1.feature", ["11"], ["Bat", "Regression", "Another_tag1", "USER#user1"]),

            createExtracted("Multi scenario feature : Scenario 1 [11]", "test_classes${File.separator}Multi_feature_1.feature", ["11"], ["Regression", "Sanity", "Another_tag1", "USER#user1"]),
            createExtracted("Multi scenario feature : Scenario 2 [17]", "test_classes${File.separator}Multi_feature_1.feature", ["17"], ["Regression", "Sanity", "Bat", "USER#user1"]),

            createExtracted("Outline scenario feature : Scenario 1 [19, 20]", "test_classes${File.separator}Outline_feature_1.feature", ["19", "20"], ["Regression", "Sanity", "Bat", "Another_tag1", "USER#user2"]),

            createExtracted("Mixed feature : Scenario 1 [9]", "test_classes${File.separator}Mixed_feature_1.feature", ["9"], ["Regression", "USER#user2"]),
            createExtracted("Mixed feature : Scenario outline 1 [22, 23]", "test_classes${File.separator}Mixed_feature_1.feature", ["22", "23"], ["Sanity", "USER#user2"]),
            createExtracted("Mixed feature : Scenario 2 [26]", "test_classes${File.separator}Mixed_feature_1.feature", ["26"], ["Bat", "USER#user2"]),

            createExtracted("Single scenario feature 2 : Scenario 1 [11]", "test_classes${File.separator}pckg${File.separator}Single_feature_2.feature", ["11"], ["Bat", "Regression", "Another_tag2", "USER#user2"]),

            createExtracted("Multi scenario feature 2 : Scenario 1 [11]", "test_classes${File.separator}pckg${File.separator}Multi_feature_2.feature", ["11"], ["Regression", "Sanity", "Another_tag3", "USER#user2"]),
            createExtracted("Multi scenario feature 2 : Scenario 2 [17]", "test_classes${File.separator}pckg${File.separator}Multi_feature_2.feature", ["17"], ["Regression", "Sanity", "Bat", "USER#user2"]),

            createExtracted("Outline scenario feature 2 : Scenario 1 [19, 20]", "test_classes${File.separator}pckg${File.separator}Outline_feature_2.feature", ["19", "20"], ["Regression", "Sanity", "Bat", "Another_tag2", "USER#user3"]),

            createExtracted("Mixed feature 2 : Scenario 1 [9]", "test_classes${File.separator}pckg${File.separator}Mixed_feature_2.feature", ["9"], ["Regression", "USER#user2"]),
            createExtracted("Mixed feature 2 : Scenario outline 1 [22, 23]", "test_classes${File.separator}pckg${File.separator}Mixed_feature_2.feature", ["22", "23"], ["Sanity", "USER#user2"]),
            createExtracted("Mixed feature 2 : Scenario 2 [26]", "test_classes${File.separator}pckg${File.separator}Mixed_feature_2.feature", ["26"], ["Bat", "USER#user2"])
    )

    @Test
    void extractCukesTests() {
        def extractor = new CukesTestExtractor("cukesExtractor", [PATH], "regex:.*.feature", "src${File.separator}test${File.separator}resources", "")
        extractor.init(features)
        List<com.devexperts.switchboard.entities.Test> tests = extractor.get()

        assert tests.size() == EXTRACTED_TESTS.size()
        assert tests.containsAll(EXTRACTED_TESTS) && EXTRACTED_TESTS.containsAll(tests)
    }

    private static final List<com.devexperts.switchboard.entities.Test> PROCESSED_TESTS = Arrays.asList(
            createProcessed("Single scenario feature : Scenario 1 [11]", "test_classes${File.separator}Single_feature_1.feature", ["11"], ["Bat", "Regression", "Another_tag1", "USER#user1"]),

            createProcessed("Multi scenario feature : Scenario 1 [11]", "test_classes${File.separator}Multi_feature_1.feature", ["11"], ["Regression", "Sanity", "Another_tag1", "USER#user1"]),
            createProcessed("Multi scenario feature : Scenario 2 [17]", "test_classes${File.separator}Multi_feature_1.feature", ["17"], ["Regression", "Sanity", "Bat", "USER#user1"]),

            createProcessed("Outline scenario feature : Scenario 1 [19, 20]", "test_classes${File.separator}Outline_feature_1.feature", ["19", "20"], ["Regression", "Sanity", "Bat", "Another_tag1", "USER#user2"]),

            createProcessed("Mixed feature : Scenario 1 [9]", "test_classes${File.separator}Mixed_feature_1.feature", ["9"], ["Regression", "USER#user2"]),
            createProcessed("Mixed feature : Scenario outline 1 [22, 23]", "test_classes${File.separator}Mixed_feature_1.feature", ["22", "23"], ["Sanity", "USER#user2"]),
            createProcessed("Mixed feature : Scenario 2 [26]", "test_classes${File.separator}Mixed_feature_1.feature", ["26"], ["Bat", "USER#user2"]),

            createProcessed("Single scenario feature 2 : Scenario 1 [11]", "test_classes${File.separator}pckg${File.separator}Single_feature_2.feature", ["11"], ["Bat", "Regression", "Another_tag2", "USER#user2"]),

            createProcessed("Multi scenario feature 2 : Scenario 1 [11]", "test_classes${File.separator}pckg${File.separator}Multi_feature_2.feature", ["11"], ["Regression", "Sanity", "Another_tag3", "USER#user2"]),
            createProcessed("Multi scenario feature 2 : Scenario 2 [17]", "test_classes${File.separator}pckg${File.separator}Multi_feature_2.feature", ["17"], ["Regression", "Sanity", "Bat", "USER#user2"]),

            createProcessed("Outline scenario feature 2 : Scenario 1 [19, 20]", "test_classes${File.separator}pckg${File.separator}Outline_feature_2.feature", ["19", "20"], ["Regression", "Sanity", "Bat", "Another_tag2", "USER#user3"]),

            createProcessed("Mixed feature 2 : Scenario 1 [9]", "test_classes${File.separator}pckg${File.separator}Mixed_feature_2.feature", ["9"], ["Regression", "USER#user2"]),
            createProcessed("Mixed feature 2 : Scenario outline 1 [22, 23]", "test_classes${File.separator}pckg${File.separator}Mixed_feature_2.feature", ["22", "23"], ["Sanity", "USER#user2"]),
            createProcessed("Mixed feature 2 : Scenario 2 [26]", "test_classes${File.separator}pckg${File.separator}Mixed_feature_2.feature", ["26"], ["Bat", "USER#user2"])
    )


    @Test
    void processCukesTests() {
        def extractor = new CukesTestExtractor("cukesExtractor", [PATH], "regex:.*.feature", "src${File.separator}test${File.separator}resources", "")
        extractor.init(features)
        List<com.devexperts.switchboard.entities.Test> extracted = extractor.get()

        def processor = new CukesTagTestProcessor("cukesTagProcessor", "#")
        processor.init(features)
        List<com.devexperts.switchboard.entities.Test> processed = processor.processTests(extracted)

        assert processed.size() == PROCESSED_TESTS.size()
        assert processed.containsAll(PROCESSED_TESTS) && PROCESSED_TESTS.containsAll(processed)
    }

    private static com.devexperts.switchboard.entities.Test createExtracted(String id, String path, Collection<String> lines, Collection<String> tags) {
        Attributes.Builder builder = Attributes.newBuilder()
                .putAttribute("location", ["path": [path].toSet(), "lines": lines.toSet()])
                .mergeAttribute("location", "scenario_name", id.substring(id.indexOf(":") + 2, id.lastIndexOf("[") - 1))
                .mergeAttribute("location", "dir_path", path.substring(0, path.lastIndexOf("${File.separator}")))
        tags.forEach { builder.putAttribute(it, [:]) }
        return new com.devexperts.switchboard.entities.Test(id, builder.build(), CukesIntegrationFeatures.TEST_TO_RUNNABLE_STRING)
    }

    private static com.devexperts.switchboard.entities.Test createProcessed(String id, String path, Collection<String> lines, Collection<String> tags) {
        Attributes.Builder builder = Attributes.newBuilder()
                .putAttribute("location", ["path": [path].toSet(), "lines": lines.toSet()])
                .mergeAttribute("location", "scenario_name", id.substring(id.indexOf(":") + 2, id.lastIndexOf("[") - 1))
                .mergeAttribute("location", "dir_path", path.substring(0, path.lastIndexOf("${File.separator}")))
        tags.forEach {
            if (it.matches(".+#.+")) {
                String[] split = it.split("#")
                builder.putAttribute(split[0], "", split[1])
            } else {
                builder.putAttribute(it, [:])
            }
        }
        return new com.devexperts.switchboard.entities.Test(id, builder.build(), CukesIntegrationFeatures.TEST_TO_RUNNABLE_STRING)
    }
}