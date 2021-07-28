/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.entities.valuesupplier;

import com.devexperts.switchboard.entities.AttributedEntity;
import com.devexperts.switchboard.entities.Attributes;
import com.devexperts.switchboard.entities.Test;
import com.devexperts.switchboard.entities.TestRun;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This implementation of {@link ValuesExtractor} can be initiated by both {@link Test} and {@link TestRun}
 * for extracting from {@link Attributes} a TestAttribute key matching specified {@link attributeKeyRegex}
 */
public class AttributeKeyValuesExtractor implements TestRunValuesExtractor, TestValuesExtractor {
    @JsonProperty(required = true)
    private String attributeKeyRegex;
    @JsonProperty(defaultValue = ",")
    private String valuesSeparator = ",";
    @JsonProperty
    private String defaultValue = null;

    private AttributeKeyValuesExtractor() {}

    public AttributeKeyValuesExtractor(String attributeKeyRegex, String valuesSeparator) {
        this.attributeKeyRegex = attributeKeyRegex;
        this.valuesSeparator = valuesSeparator;
    }

    @Override
    public Set<String> getRunValues(TestRun t) {
        return getValues(t);
    }

    @Override
    public String getRunValue(TestRun t) {
        return getValue(t);
    }

    @Override
    public Set<String> getTestValues(Test t) {
        return getValues(t);
    }

    @Override
    public String getTestValue(Test t) {
        return getValue(t);
    }

    private Set<String> getValues(AttributedEntity t) {
        return t.getAttributes().getAttributes().keySet().stream()
                .filter(k -> k.matches(attributeKeyRegex))
                .collect(Collectors.toSet());
    }

    private String getValue(AttributedEntity t) {
        String value = String.join(valuesSeparator, getValues(t));
        return value.isEmpty() ? defaultValue : value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttributeKeyValuesExtractor that = (AttributeKeyValuesExtractor) o;
        return Objects.equals(attributeKeyRegex, that.attributeKeyRegex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributeKeyRegex);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "attributeKeyRegex='" + attributeKeyRegex + '\'' +
                '}';
    }
}
