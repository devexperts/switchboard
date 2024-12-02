/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.impl.extractors;

import com.devexperts.switchboard.api.IntegrationFeatures;
import com.devexperts.switchboard.api.TestExtractor;
import com.devexperts.switchboard.entities.Test;
import com.devexperts.switchboard.utils.Arguments;
import com.devexperts.switchboard.utils.FileUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A file based implementation of TestExtractor
 *
 * @param <F> implementation class of {@link IntegrationFeatures}
 */
public abstract class FileTestExtractor<F extends IntegrationFeatures> implements TestExtractor<F> {
    private static final Logger log = LoggerFactory.getLogger(FileTestExtractor.class);

    @JsonProperty(required = true)
    private String identifier;
    @JsonProperty(required = true)
    private List<String> testLocations;


    protected FileTestExtractor() {}

    protected FileTestExtractor(String identifier, List<String> testLocations) {
        this.identifier = identifier;
        this.testLocations = testLocations;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns the path pattern used to evaluate if file with specified path should be used for text extraction
     *
     * @return the regex pattern of path to file to extract
     */
    protected abstract String getFilePattern();

    /**
     * Extracts {@link Test}s in specific way from specified file
     *
     * @param file file to extract Tests from
     * @return list of extracted tests
     */
    protected abstract List<Test> extractTests(File file);

    @Override
    public List<Test> get() {
        long start = System.currentTimeMillis();
        List<Path> paths = FileUtils.listFilePathsRecursively(getFilePattern(), Arguments.checkNotEmpty(testLocations, "testLocations"));
        log.info("Collected {} file paths from specified locations {} by pattern '{}'", paths.size(), getFilePattern(), testLocations);
        if (log.isTraceEnabled()) {
            log.trace("Collected paths:\n\t{}", paths.stream().map(Path::toString).collect(Collectors.joining("\n\t")));
        }

        List<Test> tests = paths
                .stream()
                .map(Path::toFile)
                .map(f -> {
                    List<Test> fileTests = extractTests(f);
                    log.debug("Extracted {} tests from {}", fileTests.size(), f);
                    return fileTests;
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        log.info("Extracted {} tests from {} files in {} millis", tests.size(), paths.size(), System.currentTimeMillis() - start);
        log.trace("Extracted tests: {}\n\t", tests.stream().map(Test::getIdentifier).collect(Collectors.joining("\n\t")));
        return tests;
    }
}
