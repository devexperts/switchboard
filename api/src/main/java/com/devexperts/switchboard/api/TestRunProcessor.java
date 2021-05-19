/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.api;

import com.devexperts.switchboard.entities.TestRun;

import java.util.List;

/**
 * A component processing (complementing, decoding, splitting etc) specified tests runs
 */
public interface TestRunProcessor<F extends IntegrationFeatures> extends IntegrationComponent<F> {

    /**
     * Process the specified test runs and return the result
     *
     * @param testRuns a list of test runs to process
     * @return a list of processed test runs
     */
    List<TestRun> processRuns(List<TestRun> testRuns);
}
