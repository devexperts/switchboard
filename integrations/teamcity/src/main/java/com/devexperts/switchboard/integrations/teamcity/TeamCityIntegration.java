/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.integrations.teamcity;

import com.devexperts.switchboard.impl.IntegrationImpl;
import com.devexperts.switchboard.utils.Arguments;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Integration providing to TeamCity functionality utilizing generated API
 */
public class TeamCityIntegration extends IntegrationImpl<TeamCityIntegration, TeamCityIntegrationFeatures, TeamCityIntegration.Builder> {
    @JsonProperty(required = true)
    private String basePath;
    @JsonProperty(required = true)
    private String login;
    @JsonProperty(required = true)
    private String password;

    private TeamCityIntegrationFeatures features;

    private TeamCityIntegration() {}

    public TeamCityIntegration(Builder builder) {
        super(builder);
        this.basePath = builder.basePath;
        this.login = builder.login;
        this.password = builder.password;
    }

    @Override
    public void init() {
        this.features = new TeamCityIntegrationFeatures(
                Arguments.checkNotNull(basePath, "Teamcity server base path is not specified"),
                Arguments.checkNotBlank(login, "Teamcity user login is not specified"),
                Arguments.checkNotBlank(password, "Teamcity user password is not specified")
        );
        super.init();
    }

    @Override
    public TeamCityIntegrationFeatures getIntegrationFeatures() {
        return features;
    }

    public static TeamCityIntegration.Builder newBuilder() {
        return new TeamCityIntegration.Builder();
    }

    public static class Builder extends IntegrationImpl.Builder<TeamCityIntegration, TeamCityIntegrationFeatures, Builder> {
        private String basePath;
        private String login;
        private String password;

        private Builder() {}

        public Builder basePath(String val) {
            this.basePath = val;
            return this;
        }

        public Builder login(String val) {
            this.login = val;
            return this;
        }

        public Builder password(String val) {
            this.password = val;
            return this;
        }

        @Override
        public TeamCityIntegration build() {
            return new TeamCityIntegration(this);
        }
    }
}
