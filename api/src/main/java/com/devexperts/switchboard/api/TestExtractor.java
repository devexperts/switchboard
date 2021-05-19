/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.api;

import com.devexperts.switchboard.entities.Test;

import java.util.List;

/**
 * A component supplying tests from some external source (files, DB, URL etc.)
 */
public interface TestExtractor<F extends IntegrationFeatures> extends IntegrationComponent<F> {
    /**
     * Extracts the tests from some source
     *
     * @return list of extracted tests
     */
    List<Test> get();
}