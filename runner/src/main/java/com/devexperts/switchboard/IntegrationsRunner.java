/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard;


import com.devexperts.switchboard.api.Integration;
import com.devexperts.switchboard.api.IntegrationContexts;
import com.devexperts.switchboard.utils.FileUtils;
import com.devexperts.switchboard.utils.JacksonUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class IntegrationsRunner {
    private static final Logger log = LoggerFactory.getLogger(IntegrationsRunner.class);
    private static final Option CONFIG_PATH = Option.builder("c")
            .longOpt("configPath").desc("JSON configuration file path").required(true).hasArg().build();
    private static final Option INTEGRATIONS_PATHS = Option.builder("i")
            .longOpt("integrationsPath").desc("Paths to integration lib jars and/or directories with libs").required(true).hasArgs().build();
    private static final Options OPTIONS = new Options().addOption(CONFIG_PATH).addOption(INTEGRATIONS_PATHS);
    private static final String INT_LIBS_PATTERN = "glob:**.*-all.jar";

    private IntegrationsRunner() {}

    public static void main(String[] args) {
        new IntegrationsRunner().run(args);
    }

    private void run(String[] args) {
        setup(args);
        IntegrationContexts.getIntegrations().forEach(Integration::init);
        try {
            for (Integration<?> i : IntegrationContexts.getRunnableIntegrations()) {
                log.info("Starting integration run of {} '{}'", i.getClass().getName(), i.getIdentifier());
                i.run();
                log.info("Completed integration run of {} '{}'", i.getClass().getName(), i.getIdentifier());
            }
        } finally {
            IntegrationContexts.getIntegrations().forEach(i -> {
                try {
                    i.close();
                } catch (Exception e) {
                    log.error("Failed to close integration {}", i.getIdentifier(), e);
                }
            });
        }
    }

    private static void setup(String[] args) {
        try {
            CommandLine cl = new DefaultParser().parse(OPTIONS, args, false);
            URL config = FileUtils.toUrl(cl.getOptionValue(CONFIG_PATH.getOpt()));
            String[] libPaths = cl.getOptionValues(INTEGRATIONS_PATHS.getOpt());
            List<URL> libUrls = FileUtils.listFileURLsRecursively(INT_LIBS_PATTERN, Arrays.asList(libPaths));
            IntegrationContexts.register(JacksonUtils.parseIntegrations(config, libUrls));
        } catch (ParseException e) {
            new HelpFormatter().printHelp("IntegrationsRunner", OPTIONS);
            throw new RuntimeException(e);
        }
    }
}