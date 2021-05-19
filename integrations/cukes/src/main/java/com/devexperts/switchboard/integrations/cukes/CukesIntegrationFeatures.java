/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.integrations.cukes;

import com.devexperts.switchboard.api.IntegrationFeatures;
import com.devexperts.switchboard.entities.Attributes;
import com.devexperts.switchboard.entities.Test;
import cucumber.runtime.io.MultiLoader;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Implementation of {@link IntegrationFeatures} providing access to {@link cucumber.runtime.io} functionality
 * NB: Not compatible with Cucumber versions above 1.2.x. Use a separate integration for newer versions.
 */
public class CukesIntegrationFeatures implements IntegrationFeatures {
    public static final String LOCATION_LINES_KEY = "lines";
    public static final String FEATURE_PATH_KEY = "path";
    public static final Function<Test, String> TEST_TO_RUNNABLE_STRING = test -> {
        Attributes attributes = test.getAttributes();
        Optional<String> path = attributes.getSingleAttributeValue(Attributes.LOCATION_PROP, FEATURE_PATH_KEY);
        if (!path.isPresent()) {
            throw new IllegalStateException("Cucumber test path not specified");
        }
        Optional<Set<String>> lines = attributes.getAttributeValue(Attributes.LOCATION_PROP, LOCATION_LINES_KEY);
        String linesPart = !lines.isPresent() || lines.get().isEmpty() ? "" : (":" + String.join(":", lines.get()));
        return path.get() + linesPart;
    };

    private final MultiLoader loader = new MultiLoader(Thread.currentThread().getContextClassLoader());

    CukesIntegrationFeatures() {}

    public MultiLoader getLoader() {
        return loader;
    }

    @Override
    public void close() {/*do nothing*/}
}
