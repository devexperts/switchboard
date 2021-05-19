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

import java.util.List;

/**
 * An implementation of {@link AttributePredicate} as a conjunction of several other AttributePredicates
 */
public class AndAttributeWrapper implements AttributePredicate {
    @JsonProperty(required = true)
    private List<AttributePredicate> attributePredicates;

    private AndAttributeWrapper() {}

    public AndAttributeWrapper(List<AttributePredicate> attributePredicates) {
        this.attributePredicates = attributePredicates;
    }

    @Override
    public boolean test(Attributes attributes) {
        return Arguments.checkNotEmpty(attributePredicates, "attributePredicates").stream()
                .allMatch(p -> Arguments.checkNotNull(p, "attributePredicate").test(attributes));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "attributePredicates=" + attributePredicates +
                '}';
    }
}
