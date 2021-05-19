/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.integrations.jira;

import com.devexperts.switchboard.entities.Test;
import com.devexperts.switchboard.impl.filters.DefaultTestFilter;
import com.devexperts.switchboard.utils.Arguments;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link com.devexperts.switchboard.api.TestFilter} implementation based a result of {@link jqlQuery} execution in Jira
 * matched by a specified {@link attributeKey} and {@link attributeValueKey} of {@link Attributes} of filtered Test
 */
public class JqlQueryTestFilter extends DefaultTestFilter<JiraIntegrationFeatures> {
    private static final Logger log = LoggerFactory.getLogger(JqlQueryTestFilter.class);

    @JsonProperty(required = true)
    private String identifier;
    @JsonProperty(required = true)
    private String jqlQuery;
    @JsonProperty(required = true)
    private String attributeKey;
    @JsonProperty
    private String attributeValueKey = "";

    @JsonIgnore
    private Predicate<Test> predicate;

    private JqlQueryTestFilter() { }

    public JqlQueryTestFilter(String identifier, String jqlQuery, String attributeKey, String attributeValueKey) {
        this.identifier = identifier;
        this.jqlQuery = jqlQuery;
        this.attributeKey = attributeKey;
        this.attributeValueKey = attributeValueKey;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void init(JiraIntegrationFeatures integrationFeatures) {
        List<String> queryResult = integrationFeatures.searchForIssueKeys(jqlQuery);
        if (queryResult.isEmpty()) {
            log.info("{} '{}' found no Jira issue matching query '{}'", getClass().getSimpleName(), getIdentifier(), jqlQuery);
            predicate = t -> false;
        } else {
            log.info("{} '{}' found {} Jira issue matching query '{}': {}", getClass().getSimpleName(), getIdentifier(),
                    queryResult.size(), jqlQuery, String.join(", ", queryResult));
            Pattern pattern = Pattern.compile(String.join("|", queryResult));
            predicate = t -> {
                Optional<Set<String>> oValue = t.getAttributes().getAttributeValue(
                        Arguments.checkNotBlank(attributeKey, "attributeKey for Test attribute matching Jira issue key not specified "),
                        attributeValueKey);
                return oValue.isPresent() && oValue.get().stream().map(pattern::matcher).anyMatch(Matcher::matches);
            };
        }
    }

    @Override
    protected boolean matches(Test test) {
        return predicate.test(test);
    }

    @Override
    public void close() {/*do nothing*/}
}
