/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.entities.valuesupplier;

import com.devexperts.switchboard.entities.AttributedEntity;
import com.devexperts.switchboard.entities.Pair;
import com.devexperts.switchboard.entities.Test;
import com.devexperts.switchboard.entities.TestRun;
import com.devexperts.switchboard.entities.attributes.AttributePredicate;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Set;

public class ConditionalValueExtractor implements TestRunValuesExtractor, TestValuesExtractor {
    @JsonProperty(required = true)
    private List<Pair<AttributePredicate, ValuesExtractor>> conditions;
    @JsonProperty(required = true)
    private ValuesExtractor defaultValueExtractor;

    private ConditionalValueExtractor() {}

    public ConditionalValueExtractor(List<Pair<AttributePredicate, ValuesExtractor>> conditions, ValuesExtractor defaultValueExtractor) {
        this.conditions = conditions;
        this.defaultValueExtractor = defaultValueExtractor;
    }

    @Override
    public Set<String> getRunValues(TestRun t) {
        return getExtractor(t, TestRunValuesExtractor.class).getRunValues(t);
    }

    @Override
    public String getRunValue(TestRun t) {
        return getExtractor(t, TestRunValuesExtractor.class).getRunValue(t);
    }

    @Override
    public Set<String> getTestValues(Test t) {
        return getExtractor(t, TestValuesExtractor.class).getTestValues(t);
    }

    @Override
    public String getTestValue(Test t) {
        return getExtractor(t, TestValuesExtractor.class).getTestValue(t);
    }

    private <T extends ValuesExtractor> T getExtractor(AttributedEntity t, Class<T> extractorClass) {
        ValuesExtractor ex = conditions.stream()
                .filter(k -> k.getKey().test(t.getAttributes()))
                .map(Pair::getValue)
                .findFirst()
                .orElse(defaultValueExtractor);
        if (extractorClass.isAssignableFrom(ex.getClass())) {
            return (T) ex;
        }
        throw new IllegalStateException("Expected ValuesExtractor of type " + extractorClass.getSimpleName()
                + ", found: " + ex.getClass().getSimpleName());
    }
}
