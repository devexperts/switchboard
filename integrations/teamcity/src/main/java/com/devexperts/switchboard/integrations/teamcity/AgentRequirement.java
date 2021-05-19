/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.integrations.teamcity;

import com.devexperts.switchboard.entities.TestRun;
import com.devexperts.switchboard.entities.valuesupplier.TestRunValuesExtractor;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;

/**
 * This class represent a single field requirement to TeamCity Agent
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class AgentRequirement {
    @JsonProperty(required = true)
    private TestRunValuesExtractor valueSupplier;
    @JsonProperty(required = true)
    private String agentParamName;
    @JsonProperty(required = true)
    private AgentParamMatchType agentParamMatchType;

    private AgentRequirement() {}

    public AgentRequirement(TestRunValuesExtractor valueSupplier, String agentParamName, AgentParamMatchType agentParamMatchType) {
        this.valueSupplier = valueSupplier;
        this.agentParamName = agentParamName;
        this.agentParamMatchType = agentParamMatchType;
    }

    public String getAgentRequirement(TestRun tr) {
        String agentParamValue = valueSupplier.getRunValue(tr);
        if (agentParamValue == null) {
            return "";
        }
        return String.format(",parameter:(name:%s,value:%s,matchType:%s)", agentParamName, agentParamValue, agentParamMatchType.value());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentRequirement that = (AgentRequirement) o;
        return Objects.equals(valueSupplier, that.valueSupplier) &&
                Objects.equals(agentParamName, that.agentParamName) &&
                agentParamMatchType == that.agentParamMatchType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueSupplier, agentParamName, agentParamMatchType);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "valueHolder=" + valueSupplier +
                ", agentParamName='" + agentParamName + '\'' +
                ", agentParamMatchType=" + agentParamMatchType +
                '}';
    }
}
