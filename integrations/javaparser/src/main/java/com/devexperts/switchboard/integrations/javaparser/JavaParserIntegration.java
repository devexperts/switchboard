/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.integrations.javaparser;

import com.devexperts.switchboard.impl.IntegrationImpl;

/**
 * Integration utilizing {@link com.github.javaparser} functionality to process Java code, e.g. for extracting TestNG or JUnit tests
 */
public class JavaParserIntegration extends IntegrationImpl<JavaParserIntegration, JavaParserIntegrationFeatures, JavaParserIntegration.Builder> {
    private final JavaParserIntegrationFeatures features = new JavaParserIntegrationFeatures();

    private JavaParserIntegration() {}

    public JavaParserIntegration(Builder builder) {
        super(builder);
    }

    @Override
    public JavaParserIntegrationFeatures getIntegrationFeatures() {
        return features;
    }

    public static JavaParserIntegration.Builder newBuilder() {
        return new JavaParserIntegration.Builder();
    }

    public static class Builder extends IntegrationImpl.Builder<JavaParserIntegration, JavaParserIntegrationFeatures, Builder> {

        private Builder() {}

        @Override
        public JavaParserIntegration build() {
            return new JavaParserIntegration(this);
        }
    }
}
