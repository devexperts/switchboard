/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.impl.filters;

import com.devexperts.switchboard.api.IntegrationFeatures;
import com.devexperts.switchboard.api.TestFilter;
import com.devexperts.switchboard.entities.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A default implementation of TestFilter with logging every single entity matching check
 */
public abstract class DefaultTestFilter<F extends IntegrationFeatures> implements TestFilter<F> {
    private static final Logger log = LoggerFactory.getLogger(DefaultTestFilter.class);

    @Override
    public List<Test> filter(List<Test> tests) {
        return tests.stream()
                .filter(this::matchesVerbose)
                .collect(Collectors.toList());
    }

    /**
     * Evaluates this filter on the specified test.
     *
     * @param test        test to check
     * @return true if test matches specified filter attributes
     */
    protected abstract boolean matches(Test test);

    private boolean matchesVerbose(Test test) {
        if (matches(test)) {
            log.trace("Accepted: {}", test.getIdentifier());
            return true;
        }
        log.trace("Dropped: {}", test.getIdentifier());
        return false;
    }
}