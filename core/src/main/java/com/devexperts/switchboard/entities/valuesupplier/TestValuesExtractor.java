/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.entities.valuesupplier;

import com.devexperts.switchboard.entities.Test;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Set;

/**
 * This interface abstracts (de)serializable String values extractors from {@link Test} which can be used in JSON configuration specification.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface TestValuesExtractor extends ValuesExtractor {
    /**
     * Returns string values stored in supplier or calculated based on Test or null if value cannot be obtained
     *
     * @param t Test to extract value from
     * @return supplier values values
     */
    Set<String> getTestValues(Test t);

    /**
     * Returns a single string value representation of supplier values or null if value cannot be obtained
     *
     * @param t Test to extract value from
     * @return a single string value representation of supplier values
     */
    String getTestValue(Test t);
}