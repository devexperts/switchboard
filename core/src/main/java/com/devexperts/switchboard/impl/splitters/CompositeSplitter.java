/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.impl.splitters;

import com.devexperts.switchboard.api.IntegrationFeatures;
import com.devexperts.switchboard.api.TestSplitter;
import com.devexperts.switchboard.entities.Test;
import com.devexperts.switchboard.entities.TestRun;
import com.devexperts.switchboard.entities.attributes.AttributePredicate;
import com.devexperts.switchboard.utils.Arguments;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link TestSplitter} based on a number of TestSplitters each matched with AttributePredicate specified as {@link conditionalSplitters}.
 * Each test is checked against these predicates (in order of ones specified) until first successful of successful predicate test,
 * then a respective TestSplitter is applied. If none match the {@link defaultSplitter} is applied.
 */
public class CompositeSplitter<F extends IntegrationFeatures> implements TestSplitter<F> {
    @JsonProperty(required = true)
    private String identifier;
    @JsonProperty(required = true)
    private Map<AttributePredicate, TestSplitter<F>> conditionalSplitters;
    @JsonProperty
    private TestSplitter<F> defaultSplitter = new TestCountSplitter<>("stub", 0);

    private CompositeSplitter() {}

    public CompositeSplitter(String identifier, Map<AttributePredicate, TestSplitter<F>> conditionalSplitters,
                             TestSplitter<F> defaultSplitter)
    {
        this.identifier = identifier;
        this.conditionalSplitters = conditionalSplitters;
        this.defaultSplitter = defaultSplitter;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void init(F integrationFeatures) {
        Arguments.checkNotEmpty(conditionalSplitters, "conditionalSplitters").values().forEach(s -> s.init(integrationFeatures));
        Arguments.checkNotNull(defaultSplitter, "defaultSplitter").init(integrationFeatures);
    }

    @Override
    public List<TestRun> split(Collection<Test> tests) {
        List<TestRun> result = new ArrayList<>();
        List<Test> unsplit = new ArrayList<>(tests);
        for (Map.Entry<AttributePredicate, TestSplitter<F>> conditionalSplitter : conditionalSplitters.entrySet()) {
            List<Test> matching = unsplit.stream()
                    .filter(t -> conditionalSplitter.getKey().test(t.getAttributes()))
                    .collect(Collectors.toList());
            unsplit.removeAll(matching);
            result.addAll(conditionalSplitter.getValue().split(matching));
        }
        result.addAll(defaultSplitter.split(unsplit));
        return result;
    }

    @Override
    public void close() throws Exception {
        conditionalSplitters.values().forEach(s -> {
            try {
                s.close();
            } catch (Exception e) {
                throw new RuntimeException("Failed to close splitter " + s.getIdentifier(), e);
            }
        });
        defaultSplitter.close();
    }
}