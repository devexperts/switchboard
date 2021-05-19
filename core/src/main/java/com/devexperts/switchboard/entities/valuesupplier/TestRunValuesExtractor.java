/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.entities.valuesupplier;

import com.devexperts.switchboard.entities.TestRun;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Set;

/**
 * This interface abstracts (de)serializable String values extractors from {@link TestRun} which can be used in JSON configuration specification.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface TestRunValuesExtractor extends ValuesExtractor {
    /**
     * Returns string values stored in supplier or calculated based on TestRun or null if value cannot be obtained
     *
     * @param t TestRun to extract value from
     * @return supplier values values
     */
    Set<String> getRunValues(TestRun t);

    /**
     * Returns a single string value representation of supplier values or null if value cannot be obtained
     *
     * @param t TestRun to extract value from
     * @return a single string value representation of supplier values
     */
    String getRunValue(TestRun t);
}