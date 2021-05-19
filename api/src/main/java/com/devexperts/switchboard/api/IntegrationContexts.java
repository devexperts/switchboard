/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * This class contains parsed and prepared integrations mapped to their class loaders
 */
public final class IntegrationContexts {
    private static final Map<Integration<?>, ClassLoader> CONTEXTS = new ConcurrentHashMap<>();

    private IntegrationContexts() { }

    /**
     * Stores the collected contexts
     *
     * @param contexts Map of integrations with their class loaders
     */
    public static void register(Map<Integration<?>, ClassLoader> contexts) {
        CONTEXTS.putAll(contexts);
    }

    /**
     * Stores the context for a single integration
     *
     * @param integration to store context for
     * @param classLoader classloader for this integration
     */
    public static void register(Integration<?> integration, ClassLoader classLoader) {
        CONTEXTS.put(integration, classLoader);
    }

    /**
     * Returns the list of all integrations
     *
     * @return full list of instantiated integrations integrations
     */
    public static List<Integration<?>> getIntegrations() {
        return Collections.unmodifiableList(new ArrayList<>(CONTEXTS.keySet()));
    }

    /**
     * Returns the list of integrations marked as runnable
     *
     * @return list of integrations marked as runnable
     */
    public static List<Integration<?>> getRunnableIntegrations() {
        return CONTEXTS.keySet().stream()
                .filter(Integration::isRunnable)
                .collect(Collectors.toList());
    }

    /**
     * Returns the specific integration by its {@link Integration#getIdentifier()}
     *
     * @param identifier identifier of integration to get
     * @return the integration with matching identifier
     */
    public static Integration<?> getIntegration(String identifier) {
        List<Integration<?>> list = CONTEXTS.keySet().stream()
                .filter(i -> Objects.equals(identifier, i.getIdentifier()))
                .collect(Collectors.toList());
        if (list.size() > 1) {
            throw new IllegalStateException(String.format("Found duplicate integration identifiers '%s' in %s  integrations", identifier, list.size()));
        }
        if (list.isEmpty()) {
            throw new IllegalStateException(String.format("No integrations with identifier '%s' found", identifier));
        }
        return list.get(0);
    }

    /**
     * Returns the classloader for the specified integration
     *
     * @param integration integration to get the classloader for
     * @return the classloader for specified integration
     */
    public static ClassLoader getContext(Integration<?> integration) {
        if (!CONTEXTS.containsKey(integration)) {
            throw new IllegalStateException(String.format("Integration %s not found in stored contexts. Existing: %s", integration,
                    CONTEXTS.keySet().stream().map(i -> i.getClass() + ": " + i.getIdentifier()).collect(Collectors.joining(", "))));
        }
        ClassLoader cl = CONTEXTS.get(integration);
        if (cl == null) {
            throw new IllegalStateException(String.format("Classloader not stored for integration %s", integration));
        }
        return cl;
    }
}