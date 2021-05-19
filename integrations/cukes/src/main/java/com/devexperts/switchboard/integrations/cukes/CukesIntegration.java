/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.integrations.cukes;

import com.devexperts.switchboard.impl.IntegrationImpl;

/**
 * Implementation of {@link com.devexperts.switchboard.api.Integration} providing access to {@link cucumber.runtime.io} functionality
 * NB: Not compatible with Cucumber versions above 1.2.x. Use a separate integration for newer versions.
 */
public class CukesIntegration extends IntegrationImpl<CukesIntegration, CukesIntegrationFeatures, CukesIntegration.Builder> {
    private CukesIntegration() {}

    public CukesIntegration(Builder builder) {
        super(builder);
    }

    @Override
    public CukesIntegrationFeatures getIntegrationFeatures() {
        return new CukesIntegrationFeatures();
    }

    public static CukesIntegration.Builder newBuilder() {
        return new CukesIntegration.Builder();
    }

    public static class Builder extends IntegrationImpl.Builder<CukesIntegration, CukesIntegrationFeatures, Builder> {

        private Builder() {}

        @Override
        public CukesIntegration build() {
            return new CukesIntegration(this);
        }
    }
}
