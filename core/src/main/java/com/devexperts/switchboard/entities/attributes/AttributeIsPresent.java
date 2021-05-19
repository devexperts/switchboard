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
 * An implementation of {@link AttributePredicate} which tests whether the {@link Attributes} has a single key matching the specified regex {@link attributeKeyRegex}
 */
public class AttributeIsPresent implements AttributePredicate {
    @JsonProperty(required = true)
    private String attributeKeyRegex;

    private AttributeIsPresent() {}

    public AttributeIsPresent(String attributeKeyRegex) {
        this.attributeKeyRegex = attributeKeyRegex;
    }

    @Override
    public boolean test(Attributes attributes) {
        Arguments.checkNotBlank(attributeKeyRegex, "attributeKeyRegex");
        return attributes.getAttributes().keySet().stream().anyMatch(key -> key.matches(attributeKeyRegex));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + "attributeKeyRegex='" + attributeKeyRegex + '}';
    }
}
