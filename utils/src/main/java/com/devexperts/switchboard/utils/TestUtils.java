/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.utils;

import com.devexperts.switchboard.api.Integration;
import com.devexperts.switchboard.api.IntegrationComponent;
import com.devexperts.switchboard.api.IntegrationFeatures;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class TestUtils {
    private TestUtils() { }

    public static URL findLibUrl(String path) {
        return Arrays.stream(Objects.requireNonNull(new File(path).listFiles(), "Files from path " + path))
                .filter(f -> f.getName().matches(".*-all\\.jar"))
                .map(File::toURI)
                .map(f -> {
                    try {
                        return f.toURL();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException("Failed to transform URI '" + f + "' to URL", e);
                    }
                })
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Failed to find library url in path " + path));
    }

    @SuppressWarnings("unchecked")
    public static <F extends IntegrationFeatures> List<IntegrationComponent<F>> getIntegrationComponents(Integration<F> integration, String fieldName) {
        return (List) getIntegrationValue(integration, integration.getClass(), fieldName);
    }

    public static String getField(Integration<?> integration, String fieldName) {
        return getIntegrationValue(integration, integration.getClass(), fieldName).toString();
    }

    private static Object getIntegrationValue(Integration<?> integration, Class<?> clazz, String fieldName) {
        try {
            if (Arrays.stream(clazz.getDeclaredFields()).anyMatch(f -> Objects.equals(f.getName(), fieldName))) {
                Field field = clazz.getDeclaredField(fieldName);
                if (Modifier.isPrivate(field.getModifiers())) {
                    field.setAccessible(true);
                }
                return field.get(integration);
            }
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null) {
                return getIntegrationValue(integration, superclass, fieldName);
            }
            throw new IllegalStateException("Reached null parent, field not found");
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field '" + fieldName + "' from integration " + integration, e);
        }
    }

    public static String getTextSample(int maxParagraphCount) {
        List<String> rows = new ArrayList<>();
        try (
                InputStream is = Objects.requireNonNull(
                        Thread.currentThread().getContextClassLoader().getResourceAsStream("lorem_ipsum.txt"),
                        "Failed to get InputStream from resource 'lorem_ipsum.txt'");
                InputStreamReader ir = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(ir))
        {
            int i = 0;
            String line = "";
            while (i < maxParagraphCount && ((line = br.readLine()) != null)) {
                rows.add(line);
                if (!line.trim().isEmpty()) {
                    i++;
                }
            }
            return String.join("\n", rows);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file with text sample", e);
        }
    }
}