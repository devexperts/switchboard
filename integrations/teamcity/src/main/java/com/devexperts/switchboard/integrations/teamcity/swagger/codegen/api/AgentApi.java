package com.devexperts.switchboard.integrations.teamcity.swagger.codegen.api;

import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.invoker.ApiClient;
import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.invoker.ApiException;
import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.invoker.ApiResponse;
import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.invoker.Configuration;
import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.invoker.Pair;
import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.model.AgentPool;

import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-06-01T01:12:43.660+03:00")
public class AgentApi {
    private ApiClient apiClient;

    public AgentApi() {
        this(Configuration.getDefaultApiClient());
    }

    public AgentApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * @param agentLocator (required)
     * @param fields       (optional)
     * @return AgentPool
     * @throws ApiException if fails to make API call
     */
    public AgentPool getAgentPool(String agentLocator, String fields) throws ApiException {
        return getAgentPoolWithHttpInfo(agentLocator, fields).getData();
    }

    /**
     * @param agentLocator (required)
     * @param fields       (optional)
     * @return ApiResponse&lt;AgentPool&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<AgentPool> getAgentPoolWithHttpInfo(String agentLocator, String fields) throws ApiException {
        Object localVarPostBody = null;

        // verify the required parameter 'agentLocator' is set
        if (agentLocator == null) {
            throw new ApiException(400, "Missing the required parameter 'agentLocator' when calling getAgentPool");
        }

        // create path and map variables
        String localVarPath = "/app/rest/agents/{agentLocator}/pool"
                .replaceAll("\\{" + "agentLocator" + "\\}", apiClient.escapeString(agentLocator.toString()));

        // query params
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        localVarQueryParams.addAll(apiClient.parameterToPairs("", "fields", fields));


        final String[] localVarAccepts = {

        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

        final String[] localVarContentTypes = {

        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {};

        GenericType<AgentPool> localVarReturnType = new GenericType<AgentPool>() {};
        return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }
}