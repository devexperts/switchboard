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

import java.util.Collection;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * An implementation of {@link AttributePredicate} which tests whether the {@link Attributes} has values
 * contains {@link #attributeValues} (according to {@link Modifier specified})
 * with a value key  matching the specified regex {@link #attributeValueKeyRegex}
 * and key matching the specified regex {@link #attributeKeyRegex}.
 */
public class AttributeValueSetContains implements AttributePredicate {
    /**
     * Enum specifying the checked relation between a set of attribute values and the specified collection of values:
     * either the set of attribute values contains {@link #ALL}, {@link #ANY} or {@link #EXACTLY} the same values as specified
     */
    public enum Modifier {
        ALL(Set::containsAll),
        ANY((attrValues, testValues) -> testValues.stream().anyMatch(attrValues::contains)),
        EXACTLY((attrValues, testValues) -> attrValues.containsAll(testValues) && testValues.containsAll(attrValues));
        private final BiPredicate<Set<String>, Collection<String>> test;

        Modifier(BiPredicate<Set<String>, Collection<String>> test) {
            this.test = test;
        }
    }

    @JsonProperty(required = true)
    private String attributeKeyRegex;
    @JsonProperty(required = true)
    private String attributeValueKeyRegex;
    @JsonProperty(required = true)
    private Collection<String> attributeValues;
    @JsonProperty
    private Modifier modifier = Modifier.ALL;

    private AttributeValueSetContains() {}

    public AttributeValueSetContains(String attributeKeyRegex, String attributeValueKeyRegex, Collection<String> attributeValues, Modifier modifier) {
        this.attributeKeyRegex = attributeKeyRegex;
        this.attributeValueKeyRegex = attributeValueKeyRegex;
        this.attributeValues = attributeValues;
        this.modifier = modifier;
    }

    @Override
    public boolean test(Attributes attributes) {
        return attributes.getAttributes().entrySet().stream()
                .anyMatch(e -> e.getKey().matches(Arguments.checkNotBlank(attributeKeyRegex, "attributeKeyRegex"))
                        && e.getValue().entrySet().stream()
                        .anyMatch(v -> v.getKey().matches(Arguments.checkNotNull(attributeValueKeyRegex, "attributeValueKeyRegex"))
                                && Arguments.checkNotNull(modifier, "modifier").test
                                .test(v.getValue(), Arguments.checkNotEmpty(attributeValues, "attributeValues"))));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "attributeKeyRegex='" + attributeKeyRegex + '\'' +
                ", attributeValueKeyRegex='" + attributeValueKeyRegex + '\'' +
                ", modifier='" + modifier + '\'' +
                ", attributeValues='" + attributeValues + '\'' +
                '}';
    }
}
