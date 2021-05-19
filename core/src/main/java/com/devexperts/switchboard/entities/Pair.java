/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Util class representing a pair of entities
 *
 * @param <K> class of key entity of pair
 * @param <V> class of value entity of pair
 */
public class Pair<K, V> {
    @JsonProperty
    private K key;
    @JsonProperty
    private V value;

    private Pair() {}

    private Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Creates a new pair with specified key and value
     *
     * @param key   key of created pair
     * @param value value of created pair
     * @param <K>   pair key
     * @param <V>   pair value
     * @return a created pair with specified key and value
     */
    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<>(key, value);
    }

    /**
     * @return key of this pair
     */
    public K getKey() {
        return key;
    }

    /**
     * @return value of this pair
     */
    public V getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(key, pair.key) &&
                Objects.equals(value, pair.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}
