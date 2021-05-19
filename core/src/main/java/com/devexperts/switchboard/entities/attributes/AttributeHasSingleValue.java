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
 * {@link AttributePredicate} which tests whether the {@link Attributes} has a single value
 * with key matching the specified regex {@link attributeKeyRegex} and value key matching the specified regex {@link attributeValueKeyRegex}
 */
public class AttributeHasSingleValue implements AttributePredicate {
    @JsonProperty(required = true)
    private String attributeKeyRegex;
    @JsonProperty(required = true)
    private String attributeValueKeyRegex;

    private AttributeHasSingleValue() {}

    public AttributeHasSingleValue(String attributeKeyRegex, String attributeValueKeyRegex) {
        this.attributeKeyRegex = attributeKeyRegex;
        this.attributeValueKeyRegex = attributeValueKeyRegex;
    }

    @Override
    public boolean test(Attributes attributes) {
        return attributes.getAttributes().entrySet().stream()
                .anyMatch(e -> e.getKey().matches(Arguments.checkNotBlank(attributeKeyRegex, "attributeKeyRegex"))
                        && e.getValue().entrySet().stream()
                        .anyMatch(v -> v.getKey().matches(Arguments.checkNotNull(attributeValueKeyRegex, "attributeValueKeyRegex"))
                                && v.getValue().size() == 1));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "attributeKeyRegex='" + attributeKeyRegex + '\'' +
                ", attributeValueKeyRegex='" + attributeValueKeyRegex + '\'' +
                '}';
    }
}
