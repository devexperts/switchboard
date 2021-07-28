/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.entities.valuesupplier;

import com.devexperts.switchboard.entities.Test;
import com.devexperts.switchboard.entities.TestRun;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This implementation of {@link ValuesExtractor} can be initiated by both {@link Test} and {@link TestRun}
 * returning #getIdentifier() value of initiating entity
 */
public class IdentifierValuesExtractor implements TestRunValuesExtractor, TestValuesExtractor {

    @JsonIgnore
    private String value;

    public IdentifierValuesExtractor() {}

    @Override
    public Set<String> getTestValues(Test test) {
        return new HashSet<>(Collections.singletonList(getTestValue(test)));
    }

    @Override
    public String getTestValue(Test test) {
        return test.getIdentifier();
    }

    @Override
    public Set<String> getRunValues(TestRun testRun) {
        return new HashSet<>(Collections.singletonList(getRunValue(testRun)));
    }

    @Override
    public String getRunValue(TestRun testRun) {
        return testRun.getIdentifier();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{}";
    }
}