/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.impl;

import com.devexperts.switchboard.api.ComponentsRunner;
import com.devexperts.switchboard.api.Integration;
import com.devexperts.switchboard.api.IntegrationComponent;
import com.devexperts.switchboard.api.IntegrationContexts;
import com.devexperts.switchboard.api.IntegrationFeatures;
import com.devexperts.switchboard.api.TestExtractor;
import com.devexperts.switchboard.api.TestFilter;
import com.devexperts.switchboard.api.TestProcessor;
import com.devexperts.switchboard.api.TestRunConsumer;
import com.devexperts.switchboard.api.TestRunProcessor;
import com.devexperts.switchboard.api.TestSplitter;
import com.devexperts.switchboard.entities.Test;
import com.devexperts.switchboard.entities.TestRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * The basic implementation of {@link Integration}
 */
@SuppressWarnings({"unchecked", "Convert2Diamond"})
public abstract class IntegrationImpl<
        I extends IntegrationImpl<I, F, B>,
        F extends IntegrationFeatures,
        B extends IntegrationImpl.Builder<I, F, B>
        >
        implements Integration<F>
{
    private static final Logger log = LoggerFactory.getLogger(IntegrationImpl.class);

    protected String identifier;
    private boolean runnable;
    private List<TestExtractor<F>> testExtractors = new ArrayList<>();
    private List<TestProcessor<F>> testProcessors = new ArrayList<>();
    private List<TestFilter<F>> testFilters = new ArrayList<>();
    private List<TestSplitter<F>> testSplitters = new ArrayList<>();
    private List<TestRunProcessor<F>> testRunProcessors = new ArrayList<>();
    private List<TestRunConsumer<F>> testRunConsumers = new ArrayList<>();

    protected IntegrationImpl() {}

    protected IntegrationImpl(B builder) {
        this.identifier = builder.identifier;
        this.runnable = builder.runnable;
        this.testExtractors = builder.testExtractors;
        this.testProcessors = builder.testProcessors;
        this.testFilters = builder.testFilters;
        this.testSplitters = builder.testSplitters;
        this.testRunProcessors = builder.testRunProcessors;
        this.testRunConsumers = builder.testRunConsumers;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void init() {
        F integrationFeatures = getIntegrationFeatures();
        forEachComponent(c -> c.init(integrationFeatures));
    }

    @Override
    public void run() {
        if (!isRunnable()) {
            throw new IllegalStateException(String.format("Integration '%s' with class '%s' is not runnable", getIdentifier(), this.getClass()));
        }
        inIntegrationContext(() -> {
            List<Test> extractedTests = getExtractorRunner().run(null);
            List<Test> processedTests = getTestProcessorRunner().run(extractedTests);
            List<Test> filteredTests = getFilterRunner().run(processedTests);
            List<TestRun> splitRuns = getSplitterRunner().run(filteredTests);
            List<TestRun> processedRuns = getTestRunProcessorRunner().run(splitRuns);
            getConsumerRunner().run(processedRuns);
            return null;
        });
    }

    @Override
    public boolean isRunnable() {
        return runnable;
    }

    @Override
    public ComponentsRunner<List<Test>, Void> getExtractorRunner() {
        return new ComponentsRunner<List<Test>, Void>() {
            @Override
            public List<Test> run(Void val) {
                return testExtractors.stream()
                        .map(TestExtractor::get)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
            }

            @Override
            public List<Test> run(String identifier, Void val) {
                return inIntegrationContext(() ->
                        getComponent(testExtractors, identifier)
                                .map(TestExtractor::get)
                                .orElseThrow(() -> new IllegalStateException(
                                        String.format("Extractor '%s' not found in integration `%s`", identifier, getIdentifier()))));
            }
        };
    }

    @Override
    public ComponentsRunner<List<Test>, List<Test>> getTestProcessorRunner() {
        return new ComponentsRunner<List<Test>, List<Test>>() {

            @Override
            public List<Test> run(List<Test> tests) {
                return testProcessors.isEmpty() ? tests : testProcessors.stream()
                        .map(e -> e.processTests(tests))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
            }

            @Override
            public List<Test> run(String identifier, List<Test> tests) {
                return inIntegrationContext(() ->
                        getComponent(testProcessors, identifier)
                                .map(e -> e.processTests(tests))
                                .orElseThrow(() -> new IllegalStateException(
                                        String.format("Test processor '%s' not found in integration `%s`", identifier, getIdentifier()))));
            }
        };
    }

    @Override
    public ComponentsRunner<List<Test>, List<Test>> getFilterRunner() {
        return new ComponentsRunner<List<Test>, List<Test>>() {

            @Override
            public List<Test> run(List<Test> tests) {
                return testFilters.isEmpty() ? tests : testFilters.stream()
                        .map(e -> e.filter(tests))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
            }

            @Override
            public List<Test> run(String identifier, List<Test> tests) {
                return inIntegrationContext(() ->
                        getComponent(testFilters, identifier)
                                .map(e -> e.filter(tests))
                                .orElseThrow(() -> new IllegalStateException(
                                        String.format("Filter '%s' not found in integration `%s`", identifier, getIdentifier()))));
            }
        };
    }

    @Override
    public ComponentsRunner<List<TestRun>, List<Test>> getSplitterRunner() {
        return new ComponentsRunner<List<TestRun>, List<Test>>() {
            @Override
            public List<TestRun> run(List<Test> tests) {
                List<TestRun> result = new ArrayList<>();
                result.add(TestRun.newBuilder().addTests(tests).build());

                for (TestSplitter<F> splitter : testSplitters) {
                    List<TestRun> splitterResult = new ArrayList<>();
                    result.forEach(run ->
                            splitter.split(run.getTests()).stream()
                                    .filter(r -> !r.getTests().isEmpty())
                                    .forEach(r -> splitterResult.add(mergeAttributes(run, r)))
                    );
                    result = splitterResult;
                }
                return result;
            }

            @Override
            public List<TestRun> run(String identifier, List<Test> tests) {
                return inIntegrationContext(() ->
                        getComponent(testSplitters, identifier)
                                .map(e -> e.split(tests))
                                .orElseThrow(() -> new IllegalStateException(String.format("Splitter '%s' not found in integration `%s`", identifier, getIdentifier()))));
            }
        };
    }

    @Override
    public ComponentsRunner<List<TestRun>, List<TestRun>> getTestRunProcessorRunner() {
        return new ComponentsRunner<List<TestRun>, List<TestRun>>() {

            @Override
            public List<TestRun> run(List<TestRun> testRuns) {
                return testRunProcessors.isEmpty() ? testRuns : testRunProcessors.stream()
                        .map(e -> e.processRuns(testRuns))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
            }

            @Override
            public List<TestRun> run(String identifier, List<TestRun> testRuns) {
                return inIntegrationContext(() ->
                        getComponent(testRunProcessors, identifier)
                                .map(e -> e.processRuns(testRuns))
                                .orElseThrow(() -> new IllegalStateException(
                                        String.format("Test run processor '%s' not found in integration `%s`", identifier, getIdentifier()))));
            }
        };
    }

    @Override
    public ComponentsRunner<Void, List<TestRun>> getConsumerRunner() {
        return new ComponentsRunner<Void, List<TestRun>>() {
            @Override
            public Void run(List<TestRun> testRuns) {
                testRunConsumers.forEach(e -> e.accept(testRuns));
                return null;
            }

            @Override
            public Void run(String identifier, List<TestRun> testRuns) {
                inIntegrationContext(() -> getComponent(testRunConsumers, identifier)
                        .orElseThrow(() -> new IllegalStateException(String.format("Consumer '%s' not found in integration `%s`", identifier, getIdentifier())))
                        .accept(testRuns));
                return null;
            }
        };
    }

    @Override
    public void close() {
        forEachComponent(c -> {
            try {
                c.close();
            } catch (Exception e) {
                log.error("Failed to close component {}", c.getIdentifier(), e);
            }
        });
        try {
            getIntegrationFeatures().close();
        } catch (Exception e) {
            log.error("Failed to close IntegrationFeatures of integration {}", getIdentifier(), e);
        }
    }

    private <T> T inIntegrationContext(Supplier<T> runnable) {
        ClassLoader threadCl = Thread.currentThread().getContextClassLoader();
        ClassLoader integrationCl = IntegrationContexts.getContext(this);
        try {
            if (!threadCl.equals(integrationCl)) {
                Thread.currentThread().setContextClassLoader(integrationCl);
            }
            return runnable.get();
        } finally {
            Thread.currentThread().setContextClassLoader(threadCl);
        }
    }

    private static <C extends IntegrationComponent<?>> Optional<C> getComponent(List<C> components, String identifier) {
        return components.stream()
                .filter(c -> Objects.equals(identifier, c.getIdentifier()))
                .findFirst();
    }

    private static TestRun mergeAttributes(TestRun baseRun, TestRun splitRun) {
        return TestRun.newBuilder()
                .identifier(Stream.of(baseRun.getIdentifier(), splitRun.getIdentifier())
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.joining(" | ")))
                .addTests(splitRun.getTests())
                .mergeAttributes(baseRun.getAttributes())
                .mergeAttributes(splitRun.getAttributes())
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntegrationImpl<?, ?, ?> that = (IntegrationImpl<?, ?, ?>) o;
        return runnable == that.runnable &&
                Objects.equals(identifier, that.identifier) &&
                Objects.equals(testExtractors, that.testExtractors) &&
                Objects.equals(testProcessors, that.testProcessors) &&
                Objects.equals(testFilters, that.testFilters) &&
                Objects.equals(testSplitters, that.testSplitters) &&
                Objects.equals(testRunProcessors, that.testRunProcessors) &&
                Objects.equals(testRunConsumers, that.testRunConsumers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, runnable, testExtractors, testProcessors, testFilters, testSplitters, testRunProcessors, testRunConsumers);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "identifier='" + identifier + '\'' +
                ", runnable=" + runnable +
                ", testExtractors=" + testExtractors +
                ", testProcessors=" + testProcessors +
                ", testFilters=" + testFilters +
                ", testSplitters=" + testSplitters +
                ", testRunProcessors=" + testRunProcessors +
                ", testRunConsumers=" + testRunConsumers +
                '}';
    }

    private void forEachComponent(Consumer<IntegrationComponent<F>> consumer) {
        Stream.of(testExtractors, testProcessors, testFilters, testSplitters, testRunProcessors, testRunConsumers)
                .flatMap(Collection::stream)
                .forEach(consumer);
    }

    public abstract static class Builder<I extends IntegrationImpl<I, F, B>, F extends IntegrationFeatures, B extends Builder<I, F, B>> {
        protected String identifier;
        protected boolean runnable;
        protected List<TestExtractor<F>> testExtractors = new ArrayList<>();
        protected List<TestProcessor<F>> testProcessors = new ArrayList<>();
        protected List<TestFilter<F>> testFilters = new ArrayList<>();
        protected List<TestSplitter<F>> testSplitters = new ArrayList<>();
        protected List<TestRunProcessor<F>> testRunProcessors = new ArrayList<>();
        protected List<TestRunConsumer<F>> testRunConsumers = new ArrayList<>();

        protected Builder() {}

        public B identifier(String val) {
            this.identifier = val;
            return self();
        }

        public B isRunnable(boolean val) {
            this.runnable = val;
            return self();
        }

        public B testExtractors(List<TestExtractor<F>> val) {
            this.testExtractors = val;
            return self();
        }

        public B testProcessors(List<TestProcessor<F>> val) {
            this.testProcessors = val;
            return self();
        }

        public B testFilters(List<TestFilter<F>> val) {
            this.testFilters = val;
            return self();
        }

        public B testSplitters(List<TestSplitter<F>> val) {
            this.testSplitters = val;
            return self();
        }

        public B testRunProcessors(List<TestRunProcessor<F>> val) {
            this.testRunProcessors = val;
            return self();
        }

        public B testRunConsumers(List<TestRunConsumer<F>> val) {
            this.testRunConsumers = val;
            return self();
        }

        public abstract I build();

        @SuppressWarnings("unchecked")
        final B self() {
            return (B) this;
        }
    }
}