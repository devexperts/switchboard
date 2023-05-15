/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.impl.splitters;

import com.devexperts.switchboard.api.IntegrationFeatures;
import com.devexperts.switchboard.api.TestSplitter;
import com.devexperts.switchboard.entities.Attributes;
import com.devexperts.switchboard.entities.Test;
import com.devexperts.switchboard.entities.TestRun;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of {@link TestSplitter} based on a maximum test count in TestRun specified in {@link #count}
 * If {@link #count} is set < 1 no splitting is performed and a TestRun with a copy of initial Test list is returned
 */
public class TestCountSplitter<F extends IntegrationFeatures> implements TestSplitter<F> {
    @JsonProperty(required = true)
    private String identifier;
    @JsonProperty(defaultValue = "0")
    private Integer count = 0;

    private TestCountSplitter() {}

    public TestCountSplitter(String identifier, Integer count) {
        this.identifier = identifier;
        this.count = count;
    }

    @Override
    public void init(IntegrationFeatures integrationFeatures) {/*do nothing*/}

    @Override
    public List<TestRun> split(Collection<Test> tests) {
        if (count > 0) {
            AtomicInteger counter = new AtomicInteger(0);
            List<Set<Test>> split = doSplit(new ArrayList<>(tests));
            List<TestRun> results = new ArrayList<>();
            for (Set<Test> testSet : split) {
                results.add(TestRun.newBuilder()
                        .identifier("#" + counter.getAndIncrement())
                        .addTests(testSet)
                        .putAttributes(Attributes.newBuilder()
                                .putAttribute("splitters", identifier, "true")
                                .build())
                        .build());
            }
            return results;
        }
        return Collections.singletonList(TestRun.newBuilder()
                .identifier("")
                .addTests(tests)
                .putAttributes(Attributes.newBuilder()
                        .putAttribute("splitters", identifier, "false")
                        .build())
                .build());
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    private <T> List<Set<T>> doSplit(List<T> tests) {
        List<Set<T>> result = new ArrayList<>();

        for (int i = 0; i < tests.size(); i = i + count) {
            int end = Math.min(i + count, tests.size());
            result.add(new HashSet<>(tests.subList(i, end)));
        }
        return result;
    }

    @Override
    public void close() {/*do nothing*/}
}