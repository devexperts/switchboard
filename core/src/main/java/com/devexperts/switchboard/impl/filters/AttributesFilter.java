/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.impl.filters;

import com.devexperts.switchboard.api.IntegrationFeatures;
import com.devexperts.switchboard.entities.Test;
import com.devexperts.switchboard.entities.attributes.AttributePredicate;
import com.devexperts.switchboard.utils.Arguments;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Implementation of TestFilter based on checking {@link Test.attributes} by specified {@link AttributePredicate}
 */
public class AttributesFilter<F extends IntegrationFeatures> extends DefaultTestFilter<F> {
    @JsonProperty(required = true)
    private String identifier;
    @JsonProperty(required = true)
    private AttributePredicate attributePredicate;

    private AttributesFilter() {}

    public AttributesFilter(String identifier, AttributePredicate attributePredicate) {
        this.identifier = identifier;
        this.attributePredicate = attributePredicate;
    }

    @Override
    public void init(IntegrationFeatures integrationFeatures) { /*do nothing*/ }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    protected boolean matches(Test test) {
        return Arguments.checkNotNull(attributePredicate, "attributePredicate").test(test.getAttributes());
    }

    @Override
    public void close() {/*do nothing*/}
}
