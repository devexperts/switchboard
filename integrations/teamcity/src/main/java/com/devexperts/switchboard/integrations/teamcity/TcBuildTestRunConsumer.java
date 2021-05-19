/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.integrations.teamcity;

import com.devexperts.switchboard.api.TestRunConsumer;
import com.devexperts.switchboard.entities.TestRun;
import com.devexperts.switchboard.entities.valuesupplier.TestRunValuesExtractor;
import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.model.Agent;
import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.model.Build;
import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.model.Properties;
import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.model.Property;
import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.model.Tag;
import com.devexperts.switchboard.integrations.teamcity.swagger.codegen.model.Tags;
import com.devexperts.switchboard.utils.Arguments;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Implementation of {@link TestRunConsumer} providing ability to run each of specified TestRuns as a TeamCity build
 */
@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class TcBuildTestRunConsumer implements TestRunConsumer<TeamCityIntegrationFeatures> {
    private static final Logger log = LoggerFactory.getLogger(TcBuildTestRunConsumer.class);

    @JsonProperty(required = true)
    private String identifier;
    @JsonProperty(required = true)
    private TestRunValuesExtractor buildTypeId;
    @JsonProperty
    private TestRunValuesExtractor branchName = null;
    @JsonProperty(defaultValue = "true")
    Boolean defaultBranch = true;
    @JsonProperty
    private TestRunValuesExtractor agentStackingCriterion = null;
    @JsonProperty
    private List<AgentRequirement> agentRequirements = new ArrayList<>();
    @JsonProperty
    private Map<String, TestRunValuesExtractor> buildProperties;
    @JsonProperty
    private List<TestRunValuesExtractor> buildTags;
    @JsonProperty(defaultValue = "false")
    private boolean moveToTop = false;

    private TeamCityIntegrationFeatures features;

    private TcBuildTestRunConsumer() {}

    private TcBuildTestRunConsumer(Builder builder) {
        identifier = builder.identifier;
        buildTypeId = builder.buildTypeId;
        branchName = builder.branchName;
        defaultBranch = builder.defaultBranch;
        agentStackingCriterion = builder.agentStackingCriterion;
        agentRequirements = builder.agentRequirements;
        buildProperties = builder.buildProperties;
        buildTags = builder.buildTags;
        moveToTop = builder.moveToTop;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void init(TeamCityIntegrationFeatures integrationFeatures) {
        this.features = integrationFeatures;
    }

    @Override
    public Void accept(List<TestRun> testRuns) {
        Map<TestRun, Build> result = doAccept(testRuns);
        log.info("Queued {} Teamcity builds", result.size());
        return null;
    }

    private Map<TestRun, Build> doAccept(List<TestRun> testRuns) {
        // select matching agents:
        Map<TestRun, Agent> runsWithAgents = selectAgents(testRuns);

        Map<TestRun, Build> queued = new HashMap<>();
        for (Map.Entry<TestRun, Agent> runWithAgent : runsWithAgents.entrySet()) {
            TestRun run = runWithAgent.getKey();
            Properties properties = new Properties();
            buildProperties.entrySet().stream()
                    .filter(e -> e.getValue().getRunValue(run) != null)
                    .forEach(e -> properties.addPropertyItem(new Property().name(e.getKey()).value(e.getValue().getRunValue(run))));

            Tags tags = new Tags();
            buildTags.stream()
                    .map(t -> t.getRunValue(run))
                    .filter(Objects::nonNull)
                    .map(t -> new Tag().name(t))
                    .forEach(tags::addTagItem);

            Build build = new Build()
                    .buildTypeId(buildTypeId.getRunValue(run))
                    .defaultBranch(defaultBranch)
                    .branchName(branchName == null ? null : branchName.getRunValue(run))
                    .tags(tags)
                    .properties(properties)
                    .agent(runWithAgent.getValue());

            queued.put(run, features.queueNewBuild(build, moveToTop));
        }

        return queued;
    }

    @Override
    public void close() {/*do nothing*/}

    private Map<TestRun, Agent> selectAgents(List<TestRun> testRuns) {
        Map<TestRun, Agent> trsWithAgents = mapCompatibleAgents(testRuns);
        Map<TestRun, Agent> stackedRunsWithAgents = stackStackableRuns(trsWithAgents);
        for (Map.Entry<TestRun, Agent> entry : trsWithAgents.entrySet()) {
            stackedRunsWithAgents.putIfAbsent(entry.getKey(), entry.getValue());
        }
        return stackedRunsWithAgents;
    }

    private Map<TestRun, Agent> mapCompatibleAgents(List<TestRun> testRuns) {
        Map<TestRun, Agent> trsWithAgents = new HashMap<>();
        for (TestRun tr : testRuns) {
            String agentLocator = getAgentLocator(tr);
            try {
                trsWithAgents.put(tr, new Agent().pool(features.getAgentPool(agentLocator, null)));
            } catch (Exception e) {
                throw new RuntimeException(
                        String.format("Failed to provide compatible TeamCity agents for TestRun %s with agentRequirements %s",
                                tr, agentRequirements), e);
            }
        }
        return trsWithAgents;
    }

    private String getAgentLocator(TestRun testRun) {
        StringBuilder locator = new StringBuilder(String.format("compatible:(buildType:(id:%s))",
                Arguments.checkNotNull(buildTypeId, "buildTypeId").getRunValue(testRun)));
        for (AgentRequirement agentRequirement : agentRequirements) {
            locator.append(agentRequirement.getAgentRequirement(testRun));
        }
        return locator.toString();
    }

    private Map<TestRun, Agent> stackStackableRuns(Map<TestRun, Agent> trsWithAgents) {
        Map<TestRun, Agent> stackedRunsWithAgents = new HashMap<>();
        if (agentStackingCriterion != null) {
            Map<String, Map<TestRun, List<Agent>>> runsWithAgentsByCriterion = new HashMap<>();
            for (Map.Entry<TestRun, Agent> entry : trsWithAgents.entrySet()) {
                String criterionValue = agentStackingCriterion.getRunValue(entry.getKey());
                if (criterionValue != null) {
                    runsWithAgentsByCriterion
                            .computeIfAbsent(criterionValue, v -> new HashMap<>())
                            .put(entry.getKey(), entry.getValue().getPool().getAgents().getAgent());
                }
            }

            Map<Agent, AtomicInteger> agentsUsed = new HashMap<>();
            for (Map.Entry<String, Map<TestRun, List<Agent>>> runWithAgentsByCriterion : runsWithAgentsByCriterion.entrySet()) {
                List<Agent> agents = runWithAgentsByCriterion.getValue().values().stream()
                        .flatMap(Collection::stream)
                        .distinct()
                        .sorted(Comparator.comparingInt(Agent::getId).reversed())
                        .collect(Collectors.toList());
                for (List<Agent> runAgents : runWithAgentsByCriterion.getValue().values()) {
                    agents.retainAll(runAgents);
                }
                if (agents.isEmpty()) {
                    throw new IllegalStateException(
                            String.format("TestRuns stacked by criterion %s with value %s have no agents matching agentRequirements of all TestRuns",
                                    agentStackingCriterion, runWithAgentsByCriterion.getKey()));
                }
                Agent leastUsed = agents.stream()
                        .peek(a -> agentsUsed.computeIfAbsent(a, i -> new AtomicInteger(0)))
                        .min(Comparator.comparingInt(a -> agentsUsed.get(a).get()))
                        .orElseThrow(() -> new IllegalStateException("No matching common agents found"));
                AtomicInteger counter = agentsUsed.get(leastUsed);
                for (TestRun t : runWithAgentsByCriterion.getValue().keySet()) {
                    counter.incrementAndGet();
                    stackedRunsWithAgents.put(t, leastUsed);
                }
            }
        }
        return stackedRunsWithAgents;
    }

    /**
     * {@code TcBuildTestRunConsumer} builder static inner class.
     */
    public static final class Builder {
        private String identifier;
        private TestRunValuesExtractor buildTypeId;
        private TestRunValuesExtractor branchName;
        private Boolean defaultBranch;
        private TestRunValuesExtractor agentStackingCriterion;
        private List<AgentRequirement> agentRequirements;
        private Map<String, TestRunValuesExtractor> buildProperties;
        private List<TestRunValuesExtractor> buildTags;
        private boolean moveToTop;

        private Builder() {}

        /**
         * Sets the {@code identifier} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param identifier the {@code identifier} to set
         * @return a reference to this Builder
         */
        public Builder identifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        /**
         * Sets the {@code buildTypeId} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param buildTypeId the {@code buildTypeId} to set
         * @return a reference to this Builder
         */
        public Builder buildTypeId(TestRunValuesExtractor buildTypeId) {
            this.buildTypeId = buildTypeId;
            return this;
        }

        /**
         * Sets the {@code branchName} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param branchName the {@code branchName} to set
         * @return a reference to this Builder
         */
        public Builder branchName(TestRunValuesExtractor branchName) {
            this.branchName = branchName;
            return this;
        }

        /**
         * Sets the {@code defaultBranch} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param defaultBranch the {@code defaultBranch} to set
         * @return a reference to this Builder
         */
        public Builder defaultBranch(Boolean defaultBranch) {
            this.defaultBranch = defaultBranch;
            return this;
        }

        /**
         * Sets the {@code agentStackingCriterion} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param agentStackingCriterion the {@code agentStackingCriterion} to set
         * @return a reference to this Builder
         */
        public Builder agentStackingCriterion(TestRunValuesExtractor agentStackingCriterion) {
            this.agentStackingCriterion = agentStackingCriterion;
            return this;
        }

        /**
         * Sets the {@code agentRequirements} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param agentRequirements the {@code agentRequirements} to set
         * @return a reference to this Builder
         */
        public Builder agentRequirements(List<AgentRequirement> agentRequirements) {
            this.agentRequirements = agentRequirements;
            return this;
        }

        /**
         * Sets the {@code buildProperties} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param buildProperties the {@code buildProperties} to set
         * @return a reference to this Builder
         */
        public Builder buildProperties(Map<String, TestRunValuesExtractor> buildProperties) {
            this.buildProperties = buildProperties;
            return this;
        }

        /**
         * Sets the {@code buildTags} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param buildTags the {@code buildTags} to set
         * @return a reference to this Builder
         */
        public Builder buildTags(List<TestRunValuesExtractor> buildTags) {
            this.buildTags = buildTags;
            return this;
        }

        /**
         * Sets the {@code moveToTop} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param moveToTop the {@code moveToTop} to set
         * @return a reference to this Builder
         */
        public Builder moveToTop(boolean moveToTop) {
            this.moveToTop = moveToTop;
            return this;
        }

        /**
         * Returns a {@code TcBuildTestRunConsumer} built from the parameters previously set.
         *
         * @return a {@code TcBuildTestRunConsumer} built with parameters of this {@code TcBuildTestRunConsumer.Builder}
         */
        public TcBuildTestRunConsumer build() {
            return new TcBuildTestRunConsumer(this);
        }
    }
}