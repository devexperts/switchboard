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
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This implementation of {@link TestRunValuesExtractor} and {@link TestValuesExtractor}
 * creating a formatted String (see {@link String#format(String, Object...)})  based on {@link #formatString}
 * and a list of other {@link TestRunValuesExtractor} and {@link TestValuesExtractor} initiated with the same Test/TestRun as arguments
 */
public class FormattingValuesExtractor implements TestRunValuesExtractor, TestValuesExtractor {
    @JsonProperty(required = true)
    private String formatString;
    @JsonProperty(required = true)
    private List<ValuesExtractor> formatValues;

    private FormattingValuesExtractor() {}

    public FormattingValuesExtractor(String formatString, List<ValuesExtractor> formatValues) {
        this.formatString = formatString;
        this.formatValues = formatValues;
    }

    @Override
    public Set<String> getRunValues(TestRun t) {
        return new HashSet<>(Collections.singletonList(getRunValue(t)));
    }

    @Override
    public String getRunValue(TestRun t) {
        return String.format(formatString, getTypedSuppliers(TestRunValuesExtractor.class).stream()
                .map(v -> v.getRunValue(t))
                .toArray());
    }

    @Override
    public Set<String> getTestValues(Test t) {
        return new HashSet<>(Collections.singletonList(getTestValue(t)));
    }

    @Override
    public String getTestValue(Test t) {
        return String.format(formatString, getTypedSuppliers(TestValuesExtractor.class).stream()
                .map(v -> v.getTestValue(t))
                .toArray());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormattingValuesExtractor that = (FormattingValuesExtractor) o;
        return Objects.equals(formatString, that.formatString) &&
                Objects.equals(formatValues, that.formatValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formatString, formatValues);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "formatString='" + formatString + '\'' +
                ", formatValues=" + formatValues +
                '}';
    }

    @SuppressWarnings("unchecked")
    private <T extends ValuesExtractor> List<T> getTypedSuppliers(Class<T> clazz) {
        return formatValues.stream()
                .filter(v -> clazz.isAssignableFrom(v.getClass()))
                .map(v -> (T) v)
                .collect(Collectors.toList());
    }
}
