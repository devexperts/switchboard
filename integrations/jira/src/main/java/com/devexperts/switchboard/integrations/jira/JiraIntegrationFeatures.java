/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.integrations.jira;

import com.atlassian.httpclient.apache.httpcomponents.DefaultHttpClientFactory;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.Response;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AtlassianHttpClientDecorator;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;
import com.atlassian.jira.rest.client.internal.json.CimFieldsInfoJsonParser;
import com.atlassian.jira.rest.client.internal.json.GenericJsonArrayParser;
import com.atlassian.jira.rest.client.internal.json.IssueTypeJsonParser;
import com.atlassian.jira.rest.client.internal.json.PageJsonParser;
import com.devexperts.switchboard.api.IntegrationFeatures;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Implementation of {@link IntegrationFeatures} providing access to {@link JiraRestClient} functionality
 */
public class JiraIntegrationFeatures implements IntegrationFeatures {

    private final AuthenticationHandler authenticationHandler;
    final JiraRestClient jiraClient;

    private final Map<String, Project> projectCache = new HashMap<>();
    private final Map<String, CimProject> cimProjectCache = new HashMap<>();
    private final Map<String, Map<String, CimIssueType>> projectIssueTypeCache = new HashMap<>();
    private final URI serverUri;
    private final Map<String, User> usersCache = new HashMap<>();

    private final int socketTimeoutSeconds;
    private final int searchQueryBatch;

    private final DisposableHttpClient httpClient;

    JiraIntegrationFeatures(URI serverUri, String username, String password, int socketTimeoutSeconds, int searchQueryBatch) {
        this.serverUri = serverUri;
        this.socketTimeoutSeconds = socketTimeoutSeconds;
        this.searchQueryBatch = searchQueryBatch;
        this.authenticationHandler = new BasicHttpAuthenticationHandler(username, password);
        this.httpClient = getHttpClient(serverUri, authenticationHandler);
        this.jiraClient = new AsynchronousJiraRestClient(serverUri, httpClient);
    }


    /**
     * Returns a the {@link IssueType} for the specified project with specified {@link IssueType#getName()}
     *
     * @param projectKey    unique key of the Jira project
     * @param issueTypeName name of {@link IssueType} to search
     * @return {@link IssueType}
     */
    public CimIssueType getIssueType(String projectKey, String issueTypeName) {
        return projectIssueTypeCache.computeIfAbsent(projectKey, k -> new HashMap<>())
                .computeIfAbsent(issueTypeName,
                        i -> getIssueTypes(projectKey).stream()
                                .filter(t -> Objects.equals(i, t.getName()))
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException(
                                        String.format("IssueType '%s' not found in project '%s'", issueTypeName, projectKey))));
    }

    private final GenericJsonArrayParser<IssueType> issueTypesParser = GenericJsonArrayParser.create(new IssueTypeJsonParser());
    private final GenericJsonArrayParser<CimFieldInfo> CIM_ISSUE_TYPE_JSON_PARSER = GenericJsonArrayParser.create(new CimFieldsInfoJsonParser());


    private List<CimIssueType> getIssueTypes(String projectKey) {
        // calling the Jira REST API directly as JIRA v. 9.x dropped support for createmeta endpoint (https://confluence.atlassian.com/jiracore/preparing-for-jira-9-0-1115661092.html)
        try {
            String uri = serverUri + "/rest/api/2/issue/createmeta/" + projectKey + "/issuetypes/";
            Response issueTypes = httpClient.newRequest(uri).get().get();
            PageJsonParser<IssueType> pages = new PageJsonParser<>(issueTypesParser);
            Page<IssueType> values = pages.parse(new JSONObject(issueTypes.getEntity()));
            return StreamSupport.stream(values.getValues().spliterator(), false).map(i -> {
                try {
                    Response cimIssueTypeRaw = httpClient.newRequest(uri + i.getId()).get().get();
                    PageJsonParser<CimFieldInfo> cimPages = new PageJsonParser<>(CIM_ISSUE_TYPE_JSON_PARSER);
                    JSONObject json = new JSONObject(cimIssueTypeRaw.getEntity());
                    Page<CimFieldInfo> cimValues = cimPages.parse(json);
                    AtomicInteger index = new AtomicInteger(0);
                    JSONArray array = json.getJSONArray("values");
                    Map<String, CimFieldInfo> fields = StreamSupport.stream(cimValues.getValues().spliterator(), false).map(c -> {
                        // taking the fieldId directly from JSON as it is not bound to the CimFieldInfo object
                        try {
                            String id = ((JSONObject) array.get(index.getAndIncrement())).get("fieldId").toString();
                            return new CimFieldInfo(id, c.isRequired(), c.getName(), c.getSchema(), c.getOperations(), c.getAllowedValues(), c.getAutoCompleteUri());
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toMap(CimFieldInfo::getName, f -> f));

                    return new CimIssueType(i.getIconUri(), i.getId(), i.getName(), i.isSubtask(), i.getDescription(), i.getIconUri(), fields);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a the {@link BasicComponent} for the specified project with specified {@link BasicComponent#getName()}
     *
     * @param projectKey    unique key of the Jira project
     * @param componentName name of {@link BasicComponent} to search
     * @return {@link BasicComponent}
     */
    public BasicComponent getComponent(String projectKey, String componentName) {
        return StreamSupport.stream(projectCache.computeIfAbsent(projectKey,
                                p -> jiraClient.getProjectClient().getProject(p).claim())
                        .getComponents()
                        .spliterator(), false)
                .filter(i -> Objects.equals(i.getName(), componentName))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalStateException(String.format("Component '%s' not found in project '%s'", componentName, projectKey)));
    }

    /**
     * Returns a single Jira issue with all available fields specified by Jira Issue key
     *
     * @param issueKey String issue key
     * @return Jira issue matching the specified key
     */
    public Issue getIssue(String issueKey) {
        return jiraClient.getIssueClient().getIssue(issueKey).claim();
    }

    /**
     * Returns a list of Jira issues returned by executing specified JQL query
     *
     * @param jqlQuery JQL query to search issues
     * @return list of issues returned by query execution
     */
    public List<Issue> searchForIssues(String jqlQuery) {
        return searchForIssues(jqlQuery, null);
    }


    /**
     * Returns a list of Jira issue keys returned by executing specified JQL query
     *
     * @param jqlQuery JQL query to search issues
     * @return list of issue keys returned by query execution
     */
    public List<String> searchForIssueKeys(String jqlQuery) {
        return searchForIssues(jqlQuery, Collections.emptySet())
                .stream().map(BasicIssue::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves information about selected user.
     *
     * @param name - username – JIRA username/login
     * @return complete information about given user
     */
    public User findUser(String name) {
        return usersCache.computeIfAbsent(name, n -> {
            try {
                return jiraClient.getUserClient().getUser(n).claim();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to provide user for name '" + n + "'", e);
            }
        });
    }

    private List<Issue> searchForIssues(String jqlQuery, Set<String> fields) {
        if (searchQueryBatch == 0) {
            return searchForIssues(jqlQuery, Integer.MAX_VALUE, 0, fields);
        }
        List<Issue> result = new ArrayList<>();
        List<Issue> searchResult;
        int i = 0;
        do {
            searchResult = searchForIssues(jqlQuery, searchQueryBatch, i, fields);
            result.addAll(searchResult);
            i += searchQueryBatch;
        } while (!searchResult.isEmpty());
        return result;
    }

    private List<Issue> searchForIssues(String jqlQuery, Integer limit, Integer startAt, Set<String> fields) {
        try {
            return StreamSupport.stream(jiraClient.getSearchClient()
                            .searchJql(jqlQuery, limit, startAt, fields)
                            .claim()
                            .getIssues()
                            .spliterator(), false)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to execute query '%s'", jqlQuery), e);
        }
    }

    @Override
    public void close() throws Exception {
        jiraClient.close();
    }

    private DisposableHttpClient getHttpClient(URI serverUri, AuthenticationHandler authenticationHandler) {
        DefaultHttpClientFactory<?> factory = HttpClientFactoryProvider.createHttpClientFactory(serverUri.getPath());
        HttpClient client = factory.create(getClientOptions());
        return new AtlassianHttpClientDecorator(client, authenticationHandler) {
            @Override
            public void destroy() throws Exception {
                factory.dispose(client);
            }
        };
    }

    private HttpClientOptions getClientOptions() {
        HttpClientOptions options = new HttpClientOptions();
        options.setSocketTimeout(socketTimeoutSeconds, TimeUnit.SECONDS);
        return options;
    }
}
