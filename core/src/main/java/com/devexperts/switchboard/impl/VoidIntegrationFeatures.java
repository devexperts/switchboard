/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.impl;

import com.devexperts.switchboard.api.IntegrationFeatures;

/**
 * A stub implementation of IntegrationFeatures for usage in Integration implementations with no special features required
 */
public class VoidIntegrationFeatures implements IntegrationFeatures {
    public static final VoidIntegrationFeatures INSTANCE = new VoidIntegrationFeatures();

    @Override
    public void close() {/*do nothing*/}
}
