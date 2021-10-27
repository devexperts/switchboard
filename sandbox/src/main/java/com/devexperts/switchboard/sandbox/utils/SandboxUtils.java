/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.sandbox.utils;

import com.devexperts.switchboard.IntegrationsRunner;
import com.devexperts.switchboard.api.Identifiable;
import com.devexperts.switchboard.api.Integration;
import com.devexperts.switchboard.utils.JacksonUtils;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class SandboxUtils {
    private static final Logger log = LoggerFactory.getLogger(SandboxUtils.class);

    private SandboxUtils() {}

    public static String writeIntegrationConfig(Integration<?> integration) throws IOException {
        return writeIntegrationConfig(Collections.singletonList(integration));
    }

    public static String writeIntegrationConfig(List<Integration<?>> integrations) throws IOException {
        CollectionType collType = JacksonUtils.getMapper().getTypeFactory().constructCollectionType(List.class, Integration.class);

        String config = JacksonUtils.getMapper().writer().forType(collType).writeValueAsString(integrations);
        String name = integrations.stream().map(Identifiable::getIdentifier).collect(Collectors.joining("_"));
        log.info("Config for integration `{}`:\n{}", name, config);
        return writeToFileAndLogResult(name, config);
    }

    public static void prepareAndRunIntegration(String configPath) {
        IntegrationsRunner.run(new String[] {"-c", configPath, "-i", Paths.get("").toAbsolutePath().toString()});
    }

    private static String writeToFileAndLogResult(String fileName, String content) {
        File file = new File("sandbox/out/configs/" + fileName + ".json");
        file.getParentFile().mkdirs();
        try (PrintWriter out = new PrintWriter(file)) {
            out.write(content);
            log.info("Config written to: {}", file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to write to " + file.getAbsolutePath(), e);
        }
    }
}
