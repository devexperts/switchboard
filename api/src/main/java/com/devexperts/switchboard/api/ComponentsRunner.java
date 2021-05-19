/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.api;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

/**
 * This interface abstracts running {@link IntegrationComponent} implementations from {@link Integration}
 *
 * @param <T> specific IntegrationComponent implementation run result class
 * @param <V> specific IntegrationComponent implementation accepted argument class
 */
@JsonIgnoreType
public interface ComponentsRunner<T, V> {

    /**
     * Runs all {@link IntegrationComponent} of specified type in {@link Integration} with specified argument
     *
     * @param val the argument accepted by specific {@link IntegrationComponent} implementation
     * @return the result of {@link IntegrationComponent} implementation execution
     */
    T run(V val);

    /**
     * Runs a specific {@link IntegrationComponent} of specified type in {@link Integration} with specified argument
     *
     * @param identifier a string identifier of component to tun
     * @param val        the argument accepted by specific {@link IntegrationComponent} implementation
     * @return the result of {@link IntegrationComponent} implementation execution
     */
    T run(String identifier, V val);
}