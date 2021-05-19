/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.utils;

import com.devexperts.switchboard.api.TestRunConsumer;
import com.devexperts.switchboard.entities.TestRun;
import com.devexperts.switchboard.impl.VoidIntegrationFeatures;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.List;

public class SystemPropsTestConsumer implements TestRunConsumer<VoidIntegrationFeatures> {

    private String identifier;

    private SystemPropsTestConsumer() {}

    public SystemPropsTestConsumer(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public void init(VoidIntegrationFeatures integrationFeatures) {/*do nothing*/}

    @Override
    public Void accept(List<TestRun> testRuns) {
        ObjectMapper mapper = new ObjectMapper()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.getFactory().enable(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS);
        try {
            System.setProperty("runResult", mapper.writeValueAsString(testRuns.toArray(new TestRun[0])));
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to write TestRuns to JSON and store it in SystemProperties", e);
        }
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void close() {/*do nothing*/}
}