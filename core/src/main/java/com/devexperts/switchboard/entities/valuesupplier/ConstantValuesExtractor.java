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

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This implementation of {@link ValuesExtractor} stores and returns a constant value independent from initialization
 */
public class ConstantValuesExtractor implements TestRunValuesExtractor, TestValuesExtractor {
    @JsonProperty(required = true)
    private Set<String> values;
    @JsonProperty(defaultValue = ",")
    private String valuesSeparator = ",";

    private ConstantValuesExtractor() {}

    public ConstantValuesExtractor(Set<String> values, String valuesSeparator) {
        this.values = values;
        this.valuesSeparator = valuesSeparator;
    }

    public ConstantValuesExtractor(String value) {
        this(new HashSet<>(Collections.singletonList(value)), ",");
    }

    @Override
    public Set<String> getRunValues(TestRun t) {
        return values;
    }

    @Override
    public String getRunValue(TestRun t) {
        return String.join(valuesSeparator, values);
    }

    @Override
    public Set<String> getTestValues(Test t) {
        return values;
    }

    @Override
    public String getTestValue(Test t) {
        return String.join(valuesSeparator, values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstantValuesExtractor that = (ConstantValuesExtractor) o;
        return Objects.equals(values, that.values) &&
                Objects.equals(valuesSeparator, that.valuesSeparator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values, valuesSeparator);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "values=" + values +
                ", valuesSeparator='" + valuesSeparator + '\'' +
                '}';
    }
}
