/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.entities;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * An entity representing a test run of specifically implemented {@link Test}
 */
public class TestRun extends AttributedEntity {
    private final Set<Test> tests;

    private TestRun(Builder builder) {
        super(builder.identifier, builder.attributes.build());
        tests = Collections.unmodifiableSet(builder.tests);
    }

    /**
     * @return a set of tests included into this test run
     */
    public Set<Test> getTests() {
        return tests;
    }

    public Builder toBuilder() {
        return new Builder()
                .identifier(getIdentifier())
                .addTests(new HashSet<>(tests))
                .putAttributes(getAttributes());
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && Objects.equals(tests, ((TestRun) o).tests);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tests);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "identifier='" + getIdentifier() + '\'' +
                ", attributes=" + getAttributes() +
                ", tests=" + tests +
                '}';
    }

    public static final class Builder {
        private String identifier;
        private final Set<Test> tests = new HashSet<>();
        private final Attributes.Builder attributes = Attributes.newBuilder();

        private Builder() {}

        public Builder identifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder addTests(Collection<Test> tests) {
            this.tests.addAll(tests);
            return this;
        }

        public Builder putAttributes(Attributes attributes) {
            this.attributes.putAttributes(attributes);
            return this;
        }

        public Builder mergeAttributes(Attributes attributes) {
            this.attributes.mergeAttributes(attributes);
            return this;
        }

        public TestRun build() {
            return new TestRun(this);
        }
    }
}