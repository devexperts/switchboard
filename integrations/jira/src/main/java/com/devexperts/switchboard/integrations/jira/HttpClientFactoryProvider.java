/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.integrations.jira;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.apache.httpcomponents.DefaultHttpClientFactory;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.executor.ThreadLocalContextManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Date;

/**
 * We need this class to be able to pass custom {@link HttpClientOptions} at HttpClient creation
 */
public final class HttpClientFactoryProvider {

    private HttpClientFactoryProvider() {}

    public static DefaultHttpClientFactory<?> createHttpClientFactory(String serverBaseUrl) {
        return new DefaultHttpClientFactory<>(new NoOpEventPublisher(),
                new ClientApplicationProperties(serverBaseUrl),
                new NoOpEventThreadLocalContextManager<>());
    }

    private static final class NoOpEventPublisher implements EventPublisher {
        @Override
        public void publish(Object o) {
        }

        @Override
        public void register(Object o) {
        }

        @Override
        public void unregister(Object o) {
        }

        @Override
        public void unregisterAll() {
        }
    }

    private static final class NoOpEventThreadLocalContextManager<T> implements ThreadLocalContextManager<T> {
        @Override
        public T getThreadLocalContext() {
            return null;
        }

        @Override
        public void setThreadLocalContext(T context) {}

        @Override
        public void clearThreadLocalContext() {}
    }

    private static final class ClientApplicationProperties implements ApplicationProperties {

        private final String baseUrl;

        public ClientApplicationProperties(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        @Override
        public String getBaseUrl() {
            return baseUrl;
        }

        @Nonnull
        @Override
        public String getBaseUrl(UrlMode urlMode) {
            return baseUrl;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Atlassian JIRA Rest Java Client";
        }

        @Nonnull
        @Override
        public String getPlatformId() {
            return ApplicationProperties.PLATFORM_JIRA;
        }

        @Nonnull
        @Override
        public String getVersion() {
            return "unknown";
        }

        @Nonnull
        @Override
        public Date getBuildDate() {
            throw new UnsupportedOperationException();
        }

        @Nonnull
        @Override
        public String getBuildNumber() {
            return getClass().getPackage().getImplementationVersion();
        }

        @Nullable
        @Override
        public File getHomeDirectory() {
            return new File(".");
        }

        @Override
        public String getPropertyValue(String key) {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}