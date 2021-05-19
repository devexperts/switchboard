/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.utils;

import com.devexperts.switchboard.api.Integration;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JacksonUtils {
    private static final Logger log = LoggerFactory.getLogger(JacksonUtils.class);
    private static final String PROP_KEY_REGEX = "[\\w-.]";

    private JacksonUtils() {}

    /**
     * Creates and configures {@link ObjectMapper} with current thread context classloader for TypeFactory
     *
     * @return ObjectMapper with current thread context classloader for TypeFactory
     */

    public static ObjectMapper getMapper() {
        return getMapper(null);
    }


    /**
     * Creates and configures {@link ObjectMapper} with specified classloader for TypeFactory
     *
     * @param classloader nullable classloader for ObjectMapper TypeFactory; if null current thread contextClassLoader() is used
     * @return ObjectMapper with specified classloader for TypeFactory
     */
    public static ObjectMapper getMapper(ClassLoader classloader) {
        ObjectMapper mapper = new ObjectMapper()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.getFactory().enable(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS);
        if (classloader != null) {
            mapper.setTypeFactory(mapper.getTypeFactory().withClassLoader(classloader));
        }
        return mapper;
    }

    /**
     * Parse the JSON integrations configuration file and returns a map of integrations with matching class loaders
     *
     * @param url  path to JSON configuration file
     * @param libs paths to fat jar of integrations to create
     * @return Map of integrations with matching class loaders
     */
    public static Map<Integration<?>, ClassLoader> parseIntegrations(URL url, Collection<URL> libs) {
        try {
            ObjectMapper commonMapper = getMapper();
            List<Map<?, ?>> rawParse = commonMapper.readValue(readAndResolve(url), commonMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
            ClassLoader searchClassLoader = ClassLoaderUtils.getClassloader("locatorClassLoader", true, libs.toArray(new URL[0]));
            Map<URL, ClassLoader> classloadersCache = new HashMap<>();
            classloadersCache.put(null, Thread.currentThread().getContextClassLoader());

            Map<Integration<?>, ClassLoader> integrations = new HashMap<>();

            for (Map<?, ?> m : rawParse) {
                Object integrationClassName = m.get("@class");
                if (integrationClassName == null) {
                    throw new IllegalStateException("Integration class is not defined for integration " + m);
                }
                try {
                    Class<?> integrationClass = searchClassLoader.loadClass(integrationClassName.toString());
                    CodeSource source = integrationClass.getProtectionDomain().getCodeSource();
                    ClassLoader integrationClassLoader = classloadersCache.computeIfAbsent(source == null ? null : source.getLocation(),
                            libUrl -> libUrl == null ? null : ClassLoaderUtils.getClassloader(new File(libUrl.getFile()).getName(), false, libUrl));

                    ObjectMapper integrationMapper = getMapper(integrationClassLoader);
                    Integration<?> integration = integrationMapper.readValue(integrationMapper.writeValueAsString(m), Integration.class);
                    integrations.put(integration, integrationClassLoader);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Failed to load integration class " + integrationClassName.toString(), e);
                }
            }
            return integrations;
        } catch (IOException e) {
            throw new RuntimeException("Failed to process JSON configuration", e);
        }
    }

    private static String readAndResolve(URL configUrl) {
        try {
            String configuration = new String(Files.readAllBytes(Paths.get(configUrl.toURI())));
            // search for placeholders (%some_key%) to replace them with matching Env/SystemProperties values ignoring escaped (%%some_key%%) placeholders
            Matcher matcher = Pattern.compile("(?<!%)%(" + PROP_KEY_REGEX + "+)%(?!%)").matcher(configuration);
            Set<String> placeholders = new HashSet<>();
            while (matcher.find()) {
                placeholders.add(matcher.group(1));
            }

            Map<String, String> resolvedPlaceholders = new HashMap<>();
            for (String placeholder : placeholders) {
                String val = System.getenv(placeholder);
                if (val == null) {
                    val = System.getProperty(placeholder);
                }
                if (val != null) {
                    resolvedPlaceholders.put(placeholder, val);
                }
            }

            for (Map.Entry<String, String> resolvedPlaceholder : resolvedPlaceholders.entrySet()) {
                log.info("Replacing '{}' placeholder by resolved value", resolvedPlaceholder.getKey());
                configuration = configuration.replaceAll("(?<!%)%" + resolvedPlaceholder.getKey() + "%(?!%)",
                        Matcher.quoteReplacement(resolvedPlaceholder.getValue()));
            }

            // unescape escaped placeholders:
            Matcher escapedMatcher = Pattern.compile("%(%" + PROP_KEY_REGEX + "+%)%").matcher(configuration);
            Set<String> escapedPlaceholders = new HashSet<>();
            while (escapedMatcher.find()) {
                escapedPlaceholders.add(escapedMatcher.group(1));
            }
            for (String escapedPlaceholder : escapedPlaceholders) {
                log.info("Unescaping escaped '{}' placeholder", escapedPlaceholder);
                configuration = configuration.replaceAll("%" + escapedPlaceholder + "%",
                        Matcher.quoteReplacement(escapedPlaceholder));
            }

            return configuration;
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to parse url '" + configUrl + "'to JSON configuration", e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON configuration from url " + configUrl, e);
        }
    }
}
