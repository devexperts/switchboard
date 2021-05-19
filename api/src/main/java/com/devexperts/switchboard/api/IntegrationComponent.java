/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */


package com.devexperts.switchboard.api;


/**
 * The interface indicating a single component of an {@link Integration}
 *
 * @param <F> compatible implementation class of {@link IntegrationFeatures} acceptable by a specific implementation of this interface
 */
public interface IntegrationComponent<F extends IntegrationFeatures> extends Identifiable, AutoCloseable {

    /**
     * This method initializes the component using both its own fields
     *
     * @param integrationFeatures features of integration containing the component
     */
    void init(F integrationFeatures);
}
