/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.integrations.jira;

import com.atlassian.httpclient.apache.httpcomponents.DefaultHttpClientFactory;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptionsBuilder;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.CimIssueType;
import com.atlassian.jira.rest.client.api.domain.CimProject;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AtlassianHttpClientDecorator;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;
import com.devexperts.switchboard.api.IntegrationFeatures;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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
    private final Map<String, User> usersCache = new HashMap<>();

    private final int socketTimeoutSeconds;
    private final int searchQueryBatch;

    JiraIntegrationFeatures(URI serverUri, String username, String password, int socketTimeoutSeconds, int searchQueryBatch) {
        this.socketTimeoutSeconds = socketTimeoutSeconds;
        this.searchQueryBatch = searchQueryBatch;
        this.authenticationHandler = new BasicHttpAuthenticationHandler(username, password);
        this.jiraClient = new AsynchronousJiraRestClient(serverUri, getHttpClient(serverUri, authenticationHandler));
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
                i -> StreamSupport.stream(getCimProject(projectKey).getIssueTypes().spliterator(), false)
                    .filter(t -> Objects.equals(i, t.getName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                        String.format("IssueType '%s' not found in project '%s'", issueTypeName, projectKey))));
    }

    private CimProject getCimProject(String projectKey) {
        return cimProjectCache.computeIfAbsent(projectKey,
            k -> StreamSupport.stream(jiraClient.getIssueClient().getCreateIssueMetadata(new GetCreateIssueMetadataOptionsBuilder()
                    .withExpandedIssueTypesFields()
                    .withProjectKeys(k)
                    .build()).claim().spliterator(), false)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("Project '%s' not found", projectKey))));
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
