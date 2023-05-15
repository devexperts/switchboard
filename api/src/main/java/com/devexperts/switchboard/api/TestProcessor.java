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
 * A component processing (complementing, decoding, splitting etc.) specified tests
 */
public interface TestProcessor<F extends IntegrationFeatures> extends IntegrationComponent<F> {

    /**
     * Process the specified tests and return the result
     *
     * @param tests a list of  tests to process
     * @return a list of processed tests
     */
    List<Test> processTests(List<Test> tests);
}
