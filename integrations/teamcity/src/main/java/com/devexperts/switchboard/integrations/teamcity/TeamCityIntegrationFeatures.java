/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.integrations.teamcity;

import com.devexperts.switchboard.api.IntegrationFeatures;
import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.api.AgentApi;
import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.api.BuildQueueApi;
import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.invoker.ApiClient;
import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.invoker.ApiException;
import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.model.AgentPool;
import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.model.Build;

public class TeamCityIntegrationFeatures implements IntegrationFeatures {
    private final ApiClient apiClient;
    private final AgentApi agentApi;
    private final BuildQueueApi buildQueueApi;

    TeamCityIntegrationFeatures(String basePath, String login, String password) {
        this.apiClient = new ApiClient(basePath, login, password);
        this.agentApi = new AgentApi(apiClient);
        this.buildQueueApi = new BuildQueueApi(apiClient);
    }

    public AgentPool getAgentPool(String agentLocator, String fields) {
        try {
            return agentApi.getAgentPool(agentLocator, fields);
        } catch (ApiException e) {
            throw new RuntimeException("Failed to get agent pool with agentLocator '" + agentLocator + "', fields: " + fields, e);
        }
    }

    public Build queueNewBuild(Build body, Boolean moveToTop) {
        try {
            return buildQueueApi.queueNewBuild(body, moveToTop);
        } catch (ApiException e) {
            throw new RuntimeException("Failed to queue new Build " + body, e);
        }
    }

    @Override
    public void close() {
        apiClient.getHttpClient().close();
    }
}