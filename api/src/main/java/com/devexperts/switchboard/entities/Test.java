/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.function.Function;

/**
 * An entity representing a single test (e.g. TestNG or JUnit test, a Cucumber scenario, etc.)
 */
public final class Test extends AttributedEntity {
    @JsonIgnore
    private Function<Test, String> toRunnerString;

    private Test() {super();}

    public Test(String identifier, Attributes attributes, Function<Test, String> toRunnerString) {
        super(identifier, attributes);
        this.toRunnerString = toRunnerString;
    }

    /**
     * Return the String test representation acceptable to specify test run by test framework of this test
     *
     * @return String test representation acceptable to specify test run by test framework of this test
     */
    public String toRunnerString() {
        return toRunnerString.apply(this);
    }
}