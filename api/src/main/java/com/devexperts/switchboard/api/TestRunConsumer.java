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
 * A component consuming the prepared TestRun (e.g. test runner or some report collector). These entities are the final ones in Integration run.
 */
public interface TestRunConsumer<F extends IntegrationFeatures> extends IntegrationComponent<F> {

    /**
     * Consume specified testRuns.
     *
     * @param testRuns test runs to consume
     * @return nothing
     */
    Void accept(List<TestRun> testRuns);
}
