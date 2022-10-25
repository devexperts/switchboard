/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.entities.valuesupplier;

import com.devexperts.switchboard.entities.Test;
import com.devexperts.switchboard.entities.TestRun;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This implementation of {@link TestRunValuesExtractor} for creating a value consisting of {@link Test#toRunnerString()}
 * values of tests in TestRun joined by {@link #valuesSeparator}
 */
public class TestsToStringValuesExtractor implements TestRunValuesExtractor {
    @JsonProperty(required = true)
    private String valuesSeparator;

    private TestsToStringValuesExtractor() {}

    public TestsToStringValuesExtractor(String valuesSeparator) {
        this.valuesSeparator = valuesSeparator;
    }

    @Override
    public Set<String> getRunValues(TestRun testRun) {
        return testRun.getTests().stream()
                .map(Test::toRunnerString)
                .collect(Collectors.toSet());
    }

    @Override
    public String getRunValue(TestRun testRun) {
        Set<String> values = getRunValues(testRun);
        return values.isEmpty() ? null : String.join(valuesSeparator, values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestsToStringValuesExtractor that = (TestsToStringValuesExtractor) o;
        return Objects.equals(valuesSeparator, that.valuesSeparator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valuesSeparator);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "valuesSeparator='" + valuesSeparator + '\'' +
                '}';
    }
}