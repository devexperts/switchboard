/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.entities.valuesupplier;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Interface marking (de)serializable String values extractors used in JSON configuration specification
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface ValuesExtractor {
}