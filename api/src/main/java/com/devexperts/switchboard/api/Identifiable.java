/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.api;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * The interface indicating an entity which as an identifier and can be (de)serialized by fasterxml.jackson
 * Uses default {@link JsonTypeInfo.As#PROPERTY} include for (de)serialization
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface Identifiable {
    /**
     * The entity identifier, unique within functional unit
     * (i.e. a TestSplitter in integration among other TestSplitters in this integration,
     * an Integration among other integrations in run etc.)
     *
     * @return String entity identifier
     */
    @JsonProperty(required = true)
    String getIdentifier();
}
