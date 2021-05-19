/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.integrations.teamcity;

/**
 * This enum contains the match type values available in TeamCity agent search by agent property and value
 */
public enum AgentParamMatchType {
    EXISTS("exists"),
    NOT_EXISTS("not-exists"),
    EQUALS("equals"),
    DOES_NOT_EQUAL("does-not-equal"),
    STARTS_WITH("starts-with"),
    CONTAINS("contains"),
    DOES_NOT_CONTAIN("does-not-contain"),
    ENDS_WITH("ends-with"),
    MATCHES("matches"),
    DOES_NOT_MATCH("does-not-match"),
    MORE_THAN("more-than"),
    NO_MORE_THAN("no-more-than"),
    LESS_THAN("less-than"),
    NO_LESS_THAN("no-less-than"),
    VER_MORE_THAN("ver-more-than"),
    VER_NO_MORE_THAN("ver-no-more-than"),
    VER_LESS_THAN("ver-less-than"),
    VER_NO_LESS_THAN("ver-no-less-than");

    private final String value;

    AgentParamMatchType(String value) {
        this.value = value;
    }

    /**
     * @return the TeamCity match type value
     */
    public String value() {
        return value;
    }
}
