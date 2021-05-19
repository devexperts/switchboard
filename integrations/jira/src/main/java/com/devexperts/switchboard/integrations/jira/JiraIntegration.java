/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.integrations.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.devexperts.switchboard.impl.IntegrationImpl;
import com.devexperts.switchboard.utils.Arguments;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Integration utilizing {@link JiraRestClient} functionality to get test data from Atlassian Jira
 */
public class JiraIntegration extends IntegrationImpl<JiraIntegration, JiraIntegrationFeatures, JiraIntegration.Builder> {
    @JsonProperty(required = true)
    private String uri;
    @JsonProperty(required = true)
    private String login;
    @JsonProperty(required = true)
    private String password;

    @JsonProperty(defaultValue = "60")
    private int socketTimeoutSeconds = 60;
    @JsonProperty(defaultValue = "100")
    private int searchQueryBatch = 100;

    private JiraIntegrationFeatures features;

    private JiraIntegration() {}

    private JiraIntegration(Builder builder) {
        super(builder);
        this.uri = builder.uri;
        this.login = builder.login;
        this.password = builder.password;
        this.socketTimeoutSeconds = builder.socketTimeoutSeconds;
        this.searchQueryBatch = builder.searchQueryBatch;
    }

    @Override
    public void init() {
        try {
            this.features = new JiraIntegrationFeatures(
                    new URI(Arguments.checkNotNull(uri, "Jira server URI is not specified")),
                    Arguments.checkNotBlank(login, "Jira user login is not specified"),
                    Arguments.checkNotBlank(password, "Jira user password is not specified"),
                    socketTimeoutSeconds, searchQueryBatch);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Incorrect URI syntax: " + uri, e);
        }
        super.init();
    }

    @Override
    public JiraIntegrationFeatures getIntegrationFeatures() {
        return features;
    }

    public static JiraIntegration.Builder newBuilder() {
        return new JiraIntegration.Builder();
    }

    public static class Builder extends IntegrationImpl.Builder<JiraIntegration, JiraIntegrationFeatures, Builder> {
        private String uri;
        private String login;
        private String password;
        private int socketTimeoutSeconds;
        private int searchQueryBatch;

        private Builder() {}

        public Builder uri(String val) {
            this.uri = val;
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

        public Builder socketTimeout(int val) {
            this.socketTimeoutSeconds = val;
            return this;
        }

        public Builder searchQueryBatch(int val) {
            this.searchQueryBatch = val;
            return this;
        }

        @Override
        public JiraIntegration build() {
            return new JiraIntegration(this);
        }
    }
}
