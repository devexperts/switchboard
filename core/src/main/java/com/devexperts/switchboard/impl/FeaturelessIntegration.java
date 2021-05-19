/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.impl;

import com.devexperts.switchboard.api.Integration;


/**
 * The basic implementation of {@link Integration} with no special features provided to run default implementations of components
 */
public class FeaturelessIntegration extends IntegrationImpl<FeaturelessIntegration, VoidIntegrationFeatures, FeaturelessIntegration.Builder> {

    private FeaturelessIntegration() {}

    protected FeaturelessIntegration(Builder builder) {
        super(builder);
    }

    @Override
    public VoidIntegrationFeatures getIntegrationFeatures() {
        return VoidIntegrationFeatures.INSTANCE;
    }

    public static Builder newBuilder() {
        return new FeaturelessIntegration.Builder();
    }

    public static class Builder extends IntegrationImpl.Builder<FeaturelessIntegration, VoidIntegrationFeatures, Builder> {

        private Builder() {}

        @Override
        public FeaturelessIntegration build() {
            return new FeaturelessIntegration(this);
        }
    }
}