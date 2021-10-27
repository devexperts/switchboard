/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.sandbox.examples.junit;

import com.devexperts.switchboard.impl.consumers.LoggingTestRunConsumer;
import com.devexperts.switchboard.integrations.javaparser.JavaParserIntegration;
import com.devexperts.switchboard.integrations.javaparser.JavaTestExtractor;
import com.devexperts.switchboard.sandbox.utils.SandboxUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class JunitExtract {
    // Path to test project
    private static final File BASE_PATH = new File("sandbox/src/main/resources/junit");
    // The exact directories to collect tests can be specified or just 'Collections.singletonList(BASE_PATH)' can be used to get all tests
    private static final List<String> PATHS_TO_TESTS = Stream.of(new File(BASE_PATH, "pckg1"), new File(BASE_PATH, "pckg2"))
            .map(File::getAbsolutePath)
            .collect(Collectors.toList());

    private JunitExtract() {}

    public static void main(String[] args) throws Exception {
        // build integration
        JavaParserIntegration integration = buildIntegration(true);
        // serialize and store configuration
        String config = SandboxUtils.writeIntegrationConfig(integration);
        // init and run integration on sample tests
        SandboxUtils.prepareAndRunIntegration(config);
    }

    public static JavaParserIntegration buildIntegration(boolean runnable) {
        JavaTestExtractor extractor = new JavaTestExtractor(
                "JunitExtractor",                                   // identifier
                PATHS_TO_TESTS,                                     // paths to tests
                "regex:.*Test.java",                                // Test file pattern
                "(Parameterized)?Test",                             // Test annotation pattern matching @Test and @ParameterizedTest for JUnit5
                false,                                              // Store qualified annotation in test Attributes
                true,                                               // Collect comments to test Attributes
                Arrays.asList("BeforeAll", "BeforeEach"),           // Patterns for precondition methods
                Arrays.asList("AfterEach", "AfterAll"));            // Patterns for postcondition methods

        return JavaParserIntegration.newBuilder()
                // Set Integration identifier
                .identifier("ExampleJavaParserIntegration")
                // Mark Integration as runnable
                .isRunnable(runnable)
                // Add the created TestExtractor
                .testExtractors(Collections.singletonList(extractor))
                // Use LoggingTestRunConsumer which just writes the collected TestRun list to log:
                .testRunConsumers(Collections.singletonList(new LoggingTestRunConsumer("ResultLogger")))
                .build();
    }
}
