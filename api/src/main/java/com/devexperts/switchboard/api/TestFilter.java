/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.api;

import com.devexperts.switchboard.entities.Test;

import java.util.List;

/**
 * A component filtering tests by some specified attributes
 */
public interface TestFilter<F extends IntegrationFeatures> extends IntegrationComponent<F> {

    /**
     * Checks a list of tests by specified attributes and returns a list of matching tests
     *
     * @param tests a list of tests to filter
     * @return a list of tests matching the filter
     */
    List<Test> filter(List<Test> tests);
}
