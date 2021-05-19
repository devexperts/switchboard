/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.impl.splitters;

import com.devexperts.switchboard.api.IntegrationFeatures;
import com.devexperts.switchboard.api.TestSplitter;
import com.devexperts.switchboard.entities.Attributes;
import com.devexperts.switchboard.entities.Test;
import com.devexperts.switchboard.entities.TestRun;
import com.devexperts.switchboard.entities.attributes.AttributeHasSingleValue;
import com.devexperts.switchboard.utils.Arguments;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link TestSplitter} based on a single attribute value under specified key and attribute key
 */
public class AttributeSingleValueSplitter<F extends IntegrationFeatures> implements TestSplitter<F> {
    @JsonProperty(required = true)
    private String identifier;
    @JsonProperty(required = true)
    private String attributeKey;
    @JsonProperty
    private String attributeValueKey = "";

    @JsonIgnore
    private AttributeHasSingleValue predicate;

    private AttributeSingleValueSplitter() {}

    public AttributeSingleValueSplitter(String identifier, String attributeKey, String attributeValueKey) {
        this.identifier = identifier;
        this.attributeKey = attributeKey;
        this.attributeValueKey = attributeValueKey;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void init(IntegrationFeatures integrationFeatures) {
        predicate = new AttributeHasSingleValue(
                Arguments.checkNotBlank(attributeKey, "attributeKey"),
                Arguments.checkNotNull(attributeValueKey, "attributeValueKey"));
    }

    @Override
    public List<TestRun> split(Collection<Test> tests) {
        Map<String, List<Test>> split = new HashMap<>();
        List<Test> unsplit = new ArrayList<>(tests);
        Iterator<Test> iterator = unsplit.iterator();
        while (iterator.hasNext()) {
            Test test = iterator.next();
            if (predicate.test(test.getAttributes())) {
                //noinspection OptionalGetWithoutIsPresent : checked by predicate
                split.computeIfAbsent(test.getAttributes().getSingleAttributeValue(attributeKey, attributeValueKey).get(),
                        k -> new ArrayList<>()).add(test);
                iterator.remove();
            }
        }
        List<TestRun> result = new ArrayList<>();
        for (Map.Entry<String, List<Test>> s : split.entrySet()) {
            result.add(TestRun.newBuilder()
                    .identifier(String.format("'%s':'%s'='%s'", attributeKey, attributeValueKey, s.getKey()))
                    .addTests(s.getValue())
                    .putAttributes(Attributes.newBuilder()
                            .putAttribute("splitters", identifier, "true")
                            .putAttribute(attributeKey, attributeValueKey, s.getKey())
                            .build())
                    .build());
        }
        if (!unsplit.isEmpty()) {
            result.add(TestRun.newBuilder()
                    .identifier("")
                    .addTests(unsplit)
                    .putAttributes(Attributes.newBuilder()
                            .putAttribute("splitters", identifier, "false")
                            .build())
                    .build());
        }
        return result;
    }

    @Override
    public void close() {/*do nothing*/}
}