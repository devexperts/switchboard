/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.entities.attributes;

import com.devexperts.switchboard.entities.Attributes;
import com.devexperts.switchboard.utils.Arguments;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An implementation of {@link AttributePredicate} which tests whether the {@link Attributes} has a value
 * matching the specified regex {@link #valueRegex} with a value key  matching the specified regex {@link #attributeValueKeyRegex}
 * and key matching the specified regex {@link #attributeKeyRegex}
 */
public class AttributeValueMatches implements AttributePredicate {
    @JsonProperty(required = true)
    private String attributeKeyRegex;
    @JsonProperty(required = true)
    private String attributeValueKeyRegex;
    @JsonProperty(required = true)
    private String valueRegex;

    private AttributeValueMatches() {}

    public AttributeValueMatches(String attributeKeyRegex, String attributeValueKeyRegex, String valueRegex) {
        this.attributeKeyRegex = attributeKeyRegex;
        this.attributeValueKeyRegex = attributeValueKeyRegex;
        this.valueRegex = valueRegex;
    }

    @Override
    public boolean test(Attributes attributes) {
        return attributes.getAttributes().entrySet().stream()
                .anyMatch(e -> e.getKey().matches(Arguments.checkNotBlank(attributeKeyRegex, "attributeKeyRegex"))
                        && e.getValue().entrySet().stream()
                        .anyMatch(v -> v.getKey().matches(Arguments.checkNotNull(attributeValueKeyRegex, "attributeValueKeyRegex"))
                                && v.getValue().stream()
                                .anyMatch(vv -> vv.matches(Arguments.checkNotBlank(valueRegex, "valueRegex")))));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "attributeKeyRegex='" + attributeKeyRegex + '\'' +
                ", attributeValueKeyRegex='" + attributeValueKeyRegex + '\'' +
                ", valueRegex='" + valueRegex + '\'' +
                '}';
    }
}
