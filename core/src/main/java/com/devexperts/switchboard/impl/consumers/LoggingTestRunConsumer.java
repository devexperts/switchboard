/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.impl.consumers;

import com.devexperts.switchboard.api.IntegrationFeatures;
import com.devexperts.switchboard.api.TestRunConsumer;
import com.devexperts.switchboard.entities.Test;
import com.devexperts.switchboard.entities.TestRun;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A simplest consumer logging a comma-separated {@link Test} identifiers from TestRun
 */
public final class LoggingTestRunConsumer<F extends IntegrationFeatures> implements TestRunConsumer<F> {
    private static final Logger log = LoggerFactory.getLogger(LoggingTestRunConsumer.class);

    @JsonProperty(required = true)
    private String identifier;

    private LoggingTestRunConsumer() {}

    public LoggingTestRunConsumer(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public void init(IntegrationFeatures integrationFeatures) { /*do nothing*/ }

    @Override
    public Void accept(List<TestRun> testRuns) {
        String testRunsString = testRuns.stream()
                .map(tr -> tr.getTests().stream()
                        .map(Test::getIdentifier)
                        .collect(Collectors.joining(",")))
                .map(tr -> String.format("[%s]", tr))
                .collect(Collectors.joining(",\n"));
        log.info("Test runs: {}", testRunsString);
        return null;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void close() {/*do nothing*/}
}