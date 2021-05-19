/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.api;

import com.devexperts.switchboard.entities.Test;
import com.devexperts.switchboard.entities.TestRun;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;


/**
 * This entity represents a configured composition of subcomponents prepared for extracting, processing, filtering, splitting and formatting tests
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface Integration<F extends IntegrationFeatures> extends Identifiable, AutoCloseable {

    /**
     * This method initializes the integration instance (e.g. setup IntegrationFeatures from parsed POJO fields) and its components
     */
    void init();

    /**
     * Returns the features of this integration which can be passed to each integration component
     *
     * @return the {@link IntegrationFeatures} of this Integration instance
     */
    F getIntegrationFeatures();

    /**
     * Returns {@code true} if this configuration is supposed to be run, false for the ones which only provide its components to runnable integrations.
     *
     * @return true if configuration is runnable
     */
    boolean isRunnable();

    /**
     * Run this configuration implementation if it is runnable
     */
    void run();

    /**
     * @return components runner for {@link TestExtractor} instances assigned to this configuration
     */
    ComponentsRunner<List<Test>, Void> getExtractorRunner();

    /**
     * @return components runner for {@link TestProcessor} instances assigned to this configuration
     */
    ComponentsRunner<List<Test>, List<Test>> getTestProcessorRunner();

    /**
     * @return components runner for {@link TestFilter} instances assigned to this configuration
     */
    ComponentsRunner<List<Test>, List<Test>> getFilterRunner();

    /**
     * @return components runner for {@link TestSplitter} instances assigned to this configuration
     */
    ComponentsRunner<List<TestRun>, List<Test>> getSplitterRunner();

    /**
     * @return components runner for {@link TestRunProcessor} instances assigned to this configuration
     */
    ComponentsRunner<List<TestRun>, List<TestRun>> getTestRunProcessorRunner();

    /**
     * @return components runner for {@link TestRunConsumer} instances assigned to this configuration
     */
    ComponentsRunner<Void, List<TestRun>> getConsumerRunner();
}