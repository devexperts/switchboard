/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.integrations.cukes;

import com.devexperts.switchboard.api.TestProcessor;
import com.devexperts.switchboard.entities.Attributes;
import com.devexperts.switchboard.entities.Test;
import com.devexperts.switchboard.utils.Arguments;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * An implementation of {@link TestProcessor} providing ability to split single word Cucumber tags
 * into a `key - empty_value_key - single_value` {@link Attributes} values
 * splitting them by a specified {@link #escapedValueSeparator}
 */
public class CukesTagTestProcessor implements TestProcessor<CukesIntegrationFeatures> {
    private static final String DEFAULT_VALUE_SEPARATOR = "#";

    @JsonProperty(required = true)
    private String identifier;
    @JsonProperty(defaultValue = DEFAULT_VALUE_SEPARATOR)
    private String escapedValueSeparator = DEFAULT_VALUE_SEPARATOR;

    @JsonIgnore
    private Pattern valueExtractor;

    private CukesTagTestProcessor() {
        super();
    }

    public CukesTagTestProcessor(String identifier, String escapedValueSeparator) {
        this.identifier = identifier;
        this.escapedValueSeparator = escapedValueSeparator;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void init(CukesIntegrationFeatures integrationFeatures) {
        valueExtractor = Pattern.compile("(.+)" + Arguments.checkNotBlank(escapedValueSeparator, "escapedValueSeparator") + "(.+)");
    }

    @Override
    public List<Test> processTests(List<Test> tests) {
        List<Test> result = new ArrayList<>();
        for (Test test : tests) {
            List<Matcher> updated = test.getAttributes().getAttributes().keySet().stream()
                    .map(valueExtractor::matcher)
                    .filter(Matcher::matches)
                    .collect(Collectors.toList());
            if (updated.isEmpty()) {
                result.add(test);
            } else {
                Attributes.Builder b = test.getAttributes().toBuilder();
                for (Matcher m : updated) {
                    b.remove(m.group(0));
                    b.putAttribute(m.group(1), "", m.group(2));
                }
                result.add(new Test(test.getIdentifier(), b.build(), CukesIntegrationFeatures.TEST_TO_RUNNABLE_STRING));
            }
        }
        return result;
    }

    @Override
    public void close() {/*do nothing*/}
}