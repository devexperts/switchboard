/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.entities.valuesupplier;

import com.devexperts.switchboard.entities.Pair;
import com.devexperts.switchboard.entities.Test;
import com.devexperts.switchboard.entities.TestRun;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This implementation of {@link TestRunValuesExtractor} and {@link TestValuesExtractor}
 * creating a String consisted of specified blocks. Each block is specified by it's String header and ValuesExtractor providing block body
 * initiated with the same Test/TestRun as arguments.
 */
public class BlockFormattingValuesExtractor implements TestRunValuesExtractor, TestValuesExtractor {
    @JsonProperty(required = true)
    private List<Pair<String, ValuesExtractor>> blockExtractors;
    @JsonProperty(defaultValue = "\n")
    private String headerSeparator = "\n";
    @JsonProperty(defaultValue = "\n\n")
    private String blockSeparator = "\n\n";
    @JsonProperty(defaultValue = "false")
    private boolean showEmptyBlocks = false;

    private BlockFormattingValuesExtractor() {}

    public BlockFormattingValuesExtractor(List<Pair<String, ValuesExtractor>> blockExtractors, String headerSeparator, String blockSeparator,
                                          boolean showEmptyBlocks)
    {
        this.blockExtractors = blockExtractors;
        this.headerSeparator = headerSeparator;
        this.blockSeparator = blockSeparator;
        this.showEmptyBlocks = showEmptyBlocks;
    }

    @Override
    public Set<String> getRunValues(TestRun t) {
        return new HashSet<>(Collections.singletonList(getRunValue(t)));
    }

    @Override
    public String getRunValue(TestRun t) {
        return getValue(t, TestRunValuesExtractor.class, TestRunValuesExtractor::getRunValue);
    }

    @Override
    public Set<String> getTestValues(Test t) {
        return new HashSet<>(Collections.singletonList(getTestValue(t)));
    }

    @Override
    public String getTestValue(Test t) {
        return getValue(t, TestValuesExtractor.class, TestValuesExtractor::getTestValue);
    }

    private <T, C extends ValuesExtractor> String getValue(T t, Class<C> extractorClass, BiFunction<C, T, String> extractoeFunction) {
        return blockExtractors.stream()
                .map(p -> Pair.of(p.getKey(), getTypedSupplier(p.getValue(), extractorClass)))
                .filter(e -> e.getValue() != null)
                .map(e -> Pair.of(e.getKey(), extractoeFunction.apply(e.getValue(), t)))
                .filter(e -> showEmptyBlocks || !isEffectivelyBlank(e.getValue()))
                .map(e -> getJoiner(headerSeparator).apply(e))
                .collect(Collectors.joining(blockSeparator));
    }

    private static Function<Pair<String, String>, String> getJoiner(String separator) {
        return p -> Stream.of(p.getKey(), p.getValue())
                .filter(Objects::nonNull)
                .filter(v -> !v.isEmpty())
                .collect(Collectors.joining(separator));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockFormattingValuesExtractor that = (BlockFormattingValuesExtractor) o;
        return showEmptyBlocks == that.showEmptyBlocks && Objects.equals(blockExtractors, that.blockExtractors) && Objects.equals(headerSeparator, that.headerSeparator) && Objects.equals(blockSeparator, that.blockSeparator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockExtractors, headerSeparator, blockSeparator, showEmptyBlocks);
    }


    @SuppressWarnings("unchecked")
    private <T extends ValuesExtractor> T getTypedSupplier(ValuesExtractor extractor, Class<T> clazz) {
        return clazz.isAssignableFrom(extractor.getClass()) ? (T) extractor : null;
    }

    private static boolean isEffectivelyBlank(String arg) {
        return arg == null || arg.trim().isEmpty();
    }
}
