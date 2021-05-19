/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.integrations.cukes;

import com.devexperts.switchboard.entities.Attributes;
import com.devexperts.switchboard.entities.Test;
import com.devexperts.switchboard.impl.extractors.FileTestExtractor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.model.CucumberExamples;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberScenarioOutline;
import cucumber.runtime.model.CucumberTagStatement;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Row;
import gherkin.formatter.model.Tag;
import gherkin.formatter.model.TagStatement;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class CukesTestExtractor extends FileTestExtractor<CukesIntegrationFeatures> {
    private static final String DIR_PATH_KEY = "dir_path";
    private static final String SCENARIO_NAME_KEY = "scenario_name";

    private static final String DEFAULT_FILE_PATTERN = ".*.feature";

    @JsonProperty(defaultValue = DEFAULT_FILE_PATTERN)
    private String filePattern = DEFAULT_FILE_PATTERN;
    @JsonProperty
    private String cucumberNativeFilters = "";
    @JsonProperty
    private String basePath = "";

    @JsonIgnore
    private RuntimeOptions runtimeOptions;
    @JsonIgnore
    private MultiLoader loader;

    private CukesTestExtractor() {
        super();
    }

    public CukesTestExtractor(String identifier, List<String> testLocation, String filePattern, String basePath, String cucumberNativeFilters) {
        super(identifier, testLocation);
        this.filePattern = filePattern;
        this.basePath = basePath;
        this.cucumberNativeFilters = cucumberNativeFilters;
    }

    @Override
    protected String getFilePattern() {
        return filePattern;
    }

    @Override
    public void init(CukesIntegrationFeatures integrationFeatures) {
        this.runtimeOptions = new RuntimeOptions(cucumberNativeFilters);
        this.loader = integrationFeatures.getLoader();
    }

    @Override
    protected List<Test> extractTests(File file) {
        List<CucumberFeature> cFeatures = CucumberFeature.load(loader, Collections.singletonList(file.getAbsolutePath()),
                runtimeOptions.getFilters(), System.out);
        List<Test> tests = new ArrayList<>();
        for (CucumberFeature cFeature : cFeatures) {
            Feature feature = cFeature.getGherkinFeature();
            Path relativePath = Paths.get(basePath == null ? "" : basePath).toAbsolutePath().relativize(Paths.get(cFeature.getPath()));
            Attributes.Builder featureAttributesBuilder = Attributes.newBuilder()
                    .mergeAttribute(Attributes.LOCATION_PROP, CukesIntegrationFeatures.FEATURE_PATH_KEY, relativePath.toString());
            if (relativePath.getParent() != null) {
                featureAttributesBuilder.mergeAttribute(Attributes.LOCATION_PROP, DIR_PATH_KEY, relativePath.getParent().toString());
            }

            setTags(featureAttributesBuilder, feature.getTags());

            Attributes featureAttributes = featureAttributesBuilder.build();

            List<CucumberTagStatement> elements = cFeature.getFeatureElements();
            for (CucumberTagStatement element : elements) {
                List<String> lines = getLines(element);
                TagStatement scenario = element.getGherkinModel();
                Attributes.Builder scenarioAttributesBuilder = featureAttributes.toBuilder()
                        .mergeAttribute(Attributes.LOCATION_PROP, SCENARIO_NAME_KEY, scenario.getName())
                        .mergeAttribute(Attributes.LOCATION_PROP, CukesIntegrationFeatures.LOCATION_LINES_KEY, lines);
                setTags(scenarioAttributesBuilder, scenario.getTags());

                tests.add(new Test(getTestName(feature.getName(), scenario.getName(), lines),
                        scenarioAttributesBuilder.build(), CukesIntegrationFeatures.TEST_TO_RUNNABLE_STRING));
            }
        }
        return tests;
    }

    private static void setTags(Attributes.Builder builder, List<Tag> tags) {
        extractTags(tags).forEach(t -> builder.putAttribute(t, new HashMap<>()));
    }

    private static List<String> extractTags(List<Tag> tags) {
        return tags.stream()
                .map(Tag::getName)
                .map(t -> t.replaceAll("^@", ""))
                .collect(Collectors.toList());
    }

    private static List<String> getLines(CucumberTagStatement element) {
        if (element instanceof CucumberScenarioOutline) {
            return ((CucumberScenarioOutline) element).getCucumberExamplesList().stream()
                    .map(CucumberExamples::getExamples)
                    .map(Examples::getRows)
                    .filter(r -> !r.isEmpty())
                    .map(r -> r.subList(1, r.size()))
                    .flatMap(Collection::stream)
                    .map(Row::getLine)
                    .map(Object::toString)
                    .collect(Collectors.toList());
        } else {
            return Collections.singletonList(element.getGherkinModel().getLine().toString());
        }
    }

    private static String getTestName(String featureName, String scenarioName, List<String> lines) {
        return String.format("%s : %s [%s]", featureName, scenarioName, String.join(", ", lines));
    }

    @Override
    public void close() {/*do nothing*/}
}