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
import com.devexperts.switchboard.entities.attributes.AttributeIsPresent;
import com.devexperts.switchboard.utils.Arguments;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link TestSplitter} based on a list of attribute keys
 */
public class AttributeKeySplitter<F extends IntegrationFeatures> implements TestSplitter<F> {
    @JsonProperty(required = true)
    private String identifier;
    @JsonProperty(required = true)
    private List<String> attributeKeys;

    @JsonIgnore
    private final Map<String, AttributeIsPresent> attributes = new HashMap<>();

    private AttributeKeySplitter() {}

    public AttributeKeySplitter(String identifier, List<String> attributeKeys) {
        this.identifier = identifier;
        this.attributeKeys = attributeKeys;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void init(IntegrationFeatures integrationFeatures) {
        Arguments.checkNotEmpty(attributeKeys, "attributeKeys").forEach(k -> {
            if (k == null || k.isEmpty()) {
                throw new IllegalStateException("Specified empty attribute for test splitter " + identifier);
            }
            attributes.put(k, new AttributeIsPresent(k));
        });
    }

    @Override
    public List<TestRun> split(Collection<Test> tests) {
        Map<String, List<Test>> split = new HashMap<>();
        List<Test> unsplit = new ArrayList<>(tests);
        Iterator<Test> iterator = unsplit.iterator();
        while (iterator.hasNext()) {
            Test test = iterator.next();
            String val = getAttribute(test);
            if (val != null) {
                split.computeIfAbsent(val, v -> new ArrayList<>()).add(test);
                iterator.remove();
            }
        }
        List<TestRun> result = new ArrayList<>();
        for (Map.Entry<String, List<Test>> s : split.entrySet()) {
            result.add(TestRun.newBuilder()
                    .identifier(s.getKey())
                    .addTests(s.getValue())
                    .putAttributes(Attributes.newBuilder()
                            .putAttribute("splitters", identifier, "true")
                            .putAttribute(s.getKey(), Collections.emptyMap())
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

    private String getAttribute(Test test) {
        for (Map.Entry<String, AttributeIsPresent> attribute : attributes.entrySet()) {
            if (attribute.getValue().test(test.getAttributes())) {
                return attribute.getKey();
            }
        }
        return null;
    }
}