/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.api;

import com.devexperts.switchboard.entities.Test;
import com.devexperts.switchboard.entities.TestRun;

import java.util.Collection;
import java.util.List;

/**
 * A component splitting specified tests into TestRuns by specified attributes
 */
public interface TestSplitter<F extends IntegrationFeatures> extends IntegrationComponent<F> {

    /**
     * Splits the specified list of tests into a list of test runs
     *
     * @param tests a list of tests to split
     * @return a list of test runs containing the tests
     */
    List<TestRun> split(Collection<Test> tests);
}
