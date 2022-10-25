/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.sandbox.examples.cukes;

import com.devexperts.switchboard.api.TestSplitter;
import com.devexperts.switchboard.entities.Attributes;
import com.devexperts.switchboard.entities.attributes.AttributeHasSingleValue;
import com.devexperts.switchboard.entities.attributes.AttributeIsPresent;
import com.devexperts.switchboard.entities.attributes.AttributePredicate;
import com.devexperts.switchboard.impl.consumers.LoggingTestRunConsumer;
import com.devexperts.switchboard.impl.filters.AttributesFilter;
import com.devexperts.switchboard.impl.splitters.AttributeSingleValueSplitter;
import com.devexperts.switchboard.impl.splitters.CompositeSplitter;
import com.devexperts.switchboard.impl.splitters.TestCountSplitter;
import com.devexperts.switchboard.integrations.cukes.CukesIntegration;
import com.devexperts.switchboard.integrations.cukes.CukesTagTestProcessor;
import com.devexperts.switchboard.integrations.cukes.CukesTestExtractor;
import com.devexperts.switchboard.sandbox.utils.SandboxUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class CukesExtract {

    // Path to test project
    private static final File BASE_PATH = new File("sandbox/src/main/resources/cukes");
    // The exact directories to collect tests can be specified or just 'Collections.singletonList(BASE_PATH)' can be used to get all tests
    private static final List<String> PATHS_TO_TESTS = Stream.of(new File(BASE_PATH, "pckg1"), new File(BASE_PATH, "pckg3"))
            .map(File::getAbsolutePath)
            .collect(Collectors.toList());

    private CukesExtract() {}

    public static void main(String[] args) throws Exception {
        // build integration
        CukesIntegration integration = buildIntegration();
        // serialize and store configuration
        String config = SandboxUtils.writeIntegrationConfig(integration);
        // init and run integration on sample tests
        SandboxUtils.prepareAndRunIntegration(config);
    }

    public static CukesIntegration buildIntegration() {
        CukesTestExtractor extractor = new CukesTestExtractor("cukesExtractor", PATHS_TO_TESTS, "regex:.*.feature", BASE_PATH.getAbsolutePath(), "");

        // TestProcessor which separates annotations containing '#' to key-value pair (e.g. "@UserRole#user1" => "UserRole" : "User1")
        CukesTagTestProcessor processor = new CukesTagTestProcessor("cukesTagProcessor", "#");
        // Split tests by location
        AttributeSingleValueSplitter dirSplitter = new AttributeSingleValueSplitter("DirSplitter", Attributes.LOCATION_PROP, "dir_path");

        // Split tests by value of @operatingSystem tag (separated by '#')
        AttributeSingleValueSplitter osSplitter = new AttributeSingleValueSplitter("OSSplitter", "operatingSystem", "");

        // A composite splitter example. First create atomic splitters:
        // Split tests by value of @UserRole tag (separated by '#')
        AttributeSingleValueSplitter userRoleSplitter = new AttributeSingleValueSplitter("UserRoleSplitter", "UserRole", "");
        // Split tests by value of @UserName tag (separated by '#')
        AttributeSingleValueSplitter userNameSplitter = new AttributeSingleValueSplitter("UserNameSplitter", "UserName", "");
        // Split tests by maximum count of tests in each run (3)
        TestCountSplitter countSplitter = new TestCountSplitter("SeparateRunSplitter", 3);

        Map<AttributePredicate, TestSplitter> splitterMap = new HashMap<>();
        splitterMap.put(new AttributeHasSingleValue("UserRole", ""), userRoleSplitter);
        splitterMap.put(new AttributeHasSingleValue("UserName", ""), userNameSplitter);

        // Split tests by @UserRole tag value (if applicable), else by @UserName tag value (if applicable), else by test count in each test run
        CompositeSplitter compositeSplitter = new CompositeSplitter("UserSplitter", splitterMap, countSplitter);

        // Create filter dropping tests annotated as Negative
        AttributesFilter filter = new AttributesFilter("DropRegressionFilter",new AttributeIsPresent("Negative").negate());

        return CukesIntegration.newBuilder()
                // Set Integration identifier
                .identifier("ExampleCukesIntegration")
                // Mark Integration as runnable
                .isRunnable(true)
                // Add the created TestExtractor
                .testExtractors(Collections.singletonList(extractor))
                // Add the created TestProcessor
                .testProcessors(Collections.singletonList(processor))
                // Add the created TestFilter
                .testFilters(Collections.singletonList(filter))
                // Add all splitters in order as they are expected to be applied
                .testSplitters(Arrays.asList(dirSplitter, osSplitter, compositeSplitter))
                // Use LoggingTestRunConsumer which just writes the collected TestRun list to log:
                .testRunConsumers(Collections.singletonList(new LoggingTestRunConsumer("ResultLogger")))
                .build();
    }
}
