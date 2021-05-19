/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.entities;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a hierarchical structure of attributes representing the collected or computed {@link Test} or {@link TestRun}  properties, annotations, tags etc.
 */
public final class Attributes {

    public static final String LOCATION_PROP = "location";
    public static final String COMMENTS_PROP = "comments";
    public static final String PRECONDITIONS_COMMENTS_PROP = "preconditions_comments";
    public static final String POSTCONDITIONS_COMMENTS_PROP = "postconditions_comments";
    public static final String JAVADOC_PROP = "javadoc";
    public static final String DESCRIPTION_PROP = "description";
    public static final String RUN_STRING_PROP = "runnableString";

    private LinkedHashMap<String, LinkedHashMap<String, LinkedHashSet<String>>> attributes;

    private Attributes() {}

    private Attributes(Builder builder) {
        attributes = builder.attributes;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Creates a builder with a copy of all fields of this instance
     *
     * @return a builder with a copy of all fields of this instance
     */
    public Builder toBuilder() {
        return new Builder().putAttributes(getAttributes());
    }

    /**
     * Get all attributes
     *
     * @return full attributes map
     */
    public Map<String, Map<String, Set<String>>> getAttributes() {
        return copyAttributes(attributes);
    }

    /**
     * Get specified attribute value
     *
     * @param attributeKey attribute key
     * @return attribute value
     */
    public Optional<Map<String, Set<String>>> getAttribute(String attributeKey) {
        return Optional.ofNullable(attributes.get(attributeKey)).map(Attributes::copyAttribute);
    }

    /**
     * Get specified attribute value
     *
     * @param attributeKey attribute key
     * @param valueKey     attribute value key
     * @return optional of attribute value
     */
    public Optional<Set<String>> getAttributeValue(String attributeKey, String valueKey) {
        return Optional.ofNullable(attributes.get(attributeKey))
                .map(m -> m.get(valueKey))
                .map(LinkedHashSet::new);
    }

    /**
     * Get specified attribute value
     *
     * @param attributeKey attribute key
     * @param valueKey     attribute value key
     * @return optional of attribute value
     */
    public Optional<String> getSingleAttributeValue(String attributeKey, String valueKey) {
        return getAttributeValue(attributeKey, valueKey)
                .map(s -> {
                    if (s.size() == 1) return s.iterator().next();
                    throw new IllegalStateException(String.format(
                            "Expected one value for %s:%s. Got %s: %s",
                            attributeKey, valueKey, s.size(), s));
                });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attributes that = (Attributes) o;
        return Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributes);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "attributes=" + attributes +
                '}';
    }

    private static Map<String, Map<String, Set<String>>> copyAttributes(Map<String, LinkedHashMap<String, LinkedHashSet<String>>> attributes) {
        Map<String, Map<String, Set<String>>> copy = new LinkedHashMap<>();
        attributes.forEach((key, value) -> copy.put(key, copyAttribute(value)));
        return copy;
    }

    private static Map<String, Set<String>> copyAttribute(Map<String, LinkedHashSet<String>> attribute) {
        Map<String, Set<String>> copy = new LinkedHashMap<>();
        attribute.forEach((key, value) -> copy.put(key, new LinkedHashSet<>(value)));
        return copy;
    }

    public static final class Builder {
        private LinkedHashMap<String, LinkedHashMap<String, LinkedHashSet<String>>> attributes = new LinkedHashMap<>();

        private Builder() {}

        /**
         * Add the specified attributes to current attributes and return current instance
         *
         * @param attributes to put
         * @return updated instance
         */
        public Builder putAttributes(Attributes attributes) {
            return putAttributes(attributes.getAttributes());
        }

        /**
         * Add the specified attributes to current attributes and return current instance
         *
         * @param attributes to add
         * @return updated instance
         */
        public Builder putAttributes(Map<String, Map<String, Set<String>>> attributes) {
            attributes.forEach(this::putAttribute);
            return this;
        }

        /**
         * Add the specified attribute to current attributes and return current instance
         *
         * @param attributeKey key of attribute to add
         * @param value        of attribute to add
         * @return updated instance
         */
        public Builder putAttribute(String attributeKey, Map<String, Set<String>> value) {
            Map<String, LinkedHashSet<String>> valueMap = this.attributes.compute(attributeKey, (k, v) -> new LinkedHashMap<>());
            value.forEach((key, val) -> valueMap.put(key, new LinkedHashSet<>(val)));
            return this;
        }

        /**
         * Add the specified attribute to current attributes and return current instance
         *
         * @param attributeKey key of attribute to add
         * @param valueKey     attribute value key to add
         * @param value        of attribute to add
         * @return updated instance
         */
        public Builder putAttribute(String attributeKey, String valueKey, Collection<String> value) {
            Map<String, Set<String>> map = new LinkedHashMap<>();
            map.put(valueKey, new LinkedHashSet<>(value));
            return putAttribute(attributeKey, map);
        }

        /**
         * Add the specified attribute to current attributes and return current instance
         *
         * @param attributeKey key of attribute to add
         * @param valueKey     attribute value key to add
         * @param value        of attribute to add
         * @return updated instance
         */
        public Builder putAttribute(String attributeKey, String valueKey, String value) {
            return putAttribute(attributeKey, valueKey, Collections.singletonList(value));
        }

        /**
         * Merges the specified attributes to current attributes and return current instance
         *
         * @param attributes to merge
         * @return updated instance
         */
        public Builder mergeAttributes(Attributes attributes) {
            return mergeAttributes(attributes.getAttributes());
        }

        /**
         * Merges the specified attributes to current attributes and return current instance
         *
         * @param attributes to merge
         * @return updated instance
         */
        public Builder mergeAttributes(Map<String, Map<String, Set<String>>> attributes) {
            attributes.forEach(this::mergeAttribute);
            return this;
        }

        /**
         * Merges the specified attribute to current attributes and return current instance
         *
         * @param attributeKey key of attribute to merge
         * @param value        of attribute to merge
         * @return updated instance
         */
        public Builder mergeAttribute(String attributeKey, Map<String, Set<String>> value) {
            Map<String, LinkedHashSet<String>> map = attributes.computeIfAbsent(attributeKey, k -> new LinkedHashMap<>());
            for (Map.Entry<String, Set<String>> entry : value.entrySet()) {
                map.computeIfAbsent(entry.getKey(), k -> new LinkedHashSet<>()).addAll(entry.getValue());
            }
            return this;
        }

        /**
         * Merges the specified attribute to current attributes and return current instance
         *
         * @param attributeKey key of attribute to merge
         * @param valueKey     attribute value key to merge
         * @param value        of attribute to merge
         * @return updated instance
         */
        public Builder mergeAttribute(String attributeKey, String valueKey, Collection<String> value) {
            Map<String, Set<String>> map = new LinkedHashMap<>();
            map.put(valueKey, new LinkedHashSet<>(value));
            return mergeAttribute(attributeKey, map);
        }

        /**
         * Merges the specified attribute to current attributes and return current instance
         *
         * @param attributeKey key of attribute to merge
         * @param valueKey     attribute value key to merge
         * @param value        of attribute to merge
         * @return updated instance
         */
        public Builder mergeAttribute(String attributeKey, String valueKey, String value) {
            return mergeAttribute(attributeKey, valueKey, Collections.singletonList(value));
        }

        /**
         * Removes the existing attribute by it's key
         *
         * @param attributeKey key of attribute to remove
         * @return updated instance
         */
        public Builder remove(String attributeKey) {
            attributes.remove(attributeKey);
            return this;
        }

        public Attributes build() {
            return new Attributes(this);
        }
    }
}