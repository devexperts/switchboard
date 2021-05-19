package com.devexperts.switchboard.integrations.teamcity.swagger.codegen.api;

import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.invoker.ApiClient;
import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.invoker.ApiException;
import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.invoker.ApiResponse;
import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.invoker.Configuration;
import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.invoker.Pair;
import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.model.Build;

import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-06-01T01:12:43.660+03:00")
public class BuildQueueApi {
    private ApiClient apiClient;

    public BuildQueueApi() {
        this(Configuration.getDefaultApiClient());
    }

    public BuildQueueApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * @param body      (optional)
     * @param moveToTop (optional)
     * @return Build
     * @throws ApiException if fails to make API call
     */
    public Build queueNewBuild(Build body, Boolean moveToTop) throws ApiException {
        return queueNewBuildWithHttpInfo(body, moveToTop).getData();
    }

    /**
     * @param body      (optional)
     * @param moveToTop (optional)
     * @return ApiResponse&lt;Build&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<Build> queueNewBuildWithHttpInfo(Build body, Boolean moveToTop) throws ApiException {
        Object localVarPostBody = body;

        // create path and map variables
        String localVarPath = "/app/rest/buildQueue";

        // query params
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        localVarQueryParams.addAll(apiClient.parameterToPairs("", "moveToTop", moveToTop));


        final String[] localVarAccepts = {

        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

        final String[] localVarContentTypes = {

        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] {};

        GenericType<Build> localVarReturnType = new GenericType<Build>() {};
        return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }
}
