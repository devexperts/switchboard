/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.api;

import com.devexperts.switchboard.entities.Test;
import com.devexperts.switchboard.entities.TestRun;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class represents a placeholder for using a component of one integration in another integration
 */
public class ComponentReference<F extends IntegrationFeatures>
        implements TestExtractor<F>, TestProcessor<F>, TestFilter<F>, TestSplitter<F>, TestRunProcessor<F>, TestRunConsumer<F>
{
    @JsonProperty(required = true)
    private String identifier;
    @JsonProperty(required = true)
    private String integrationIdentifier;

    private ComponentReference() {}

    public ComponentReference(String identifier, String integrationIdentifier) {
        this.identifier = identifier;
        this.integrationIdentifier = integrationIdentifier;
    }

    @Override
    public void init(F integrationFeatures) {/*do nothing: referenced component will be inited by its own integration*/}

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public List<Test> get() {
        return getIntegration().getExtractorRunner().run(identifier, null);
    }

    @Override
    public List<Test> processTests(List<Test> tests) {
        return getIntegration().getTestProcessorRunner().run(identifier, tests);
    }

    @Override
    public List<TestRun> processRuns(List<TestRun> testsRuns) {
        return getIntegration().getTestRunProcessorRunner().run(identifier, testsRuns);
    }

    @Override
    public List<Test> filter(List<Test> tests) {
        return getIntegration().getFilterRunner().run(identifier, tests);
    }

    @Override
    public List<TestRun> split(Collection<Test> tests) {
        return getIntegration().getSplitterRunner().run(identifier, new ArrayList<>(tests));
    }

    @Override
    public Void accept(List<TestRun> testRuns) {
        getIntegration().getConsumerRunner().run(identifier, testRuns);
        return null;
    }

    private Integration<? extends IntegrationFeatures> getIntegration() {
        return IntegrationContexts.getIntegration(integrationIdentifier);
    }

    @Override
    public void close() {/*do nothing: referenced component will be closed by its own integration*/}
}