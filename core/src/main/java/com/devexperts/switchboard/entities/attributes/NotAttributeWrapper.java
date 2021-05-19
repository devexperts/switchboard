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
 * An implementation of {@link AttributePredicate} as a negation of other AttributePredicates
 */
public class NotAttributeWrapper implements AttributePredicate {
    @JsonProperty(required = true)
    private AttributePredicate attributePredicate;

    public NotAttributeWrapper() { }

    public NotAttributeWrapper(AttributePredicate attributePredicate) {
        this.attributePredicate = attributePredicate;
    }

    @Override
    public boolean test(Attributes attributes) {
        return !Arguments.checkNotNull(attributePredicate, "attributePredicate").test(attributes);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "attributePredicate=" + attributePredicate +
                '}';
    }
}
