/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.entities;


import com.devexperts.switchboard.api.Identifiable;

import java.util.Objects;

/**
 * An entity representing an {@link Identifiable} with {@link Attributes}
 */
public abstract class AttributedEntity implements Identifiable {
    private String identifier;
    private Attributes attributes;

    protected AttributedEntity() {}

    protected AttributedEntity(String identifier, Attributes attributes) {
        this.identifier = identifier;
        this.attributes = attributes;
    }

    /**
     * Return unique String identifier of this test
     *
     * @return String identifier
     */
    @Override
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Return the String value of test suitable for the runner of specified test
     *
     * @return the String value of test suitable for the runner
     */
    public Attributes getAttributes() {
        return attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttributedEntity test = (AttributedEntity) o;
        return Objects.equals(identifier, test.identifier) &&
                Objects.equals(attributes, test.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, attributes);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "identifier='" + identifier + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}
