/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.api;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

/**
 * This interface represents the features instantiated per {@link Integration} instance and then passed to each integration component
 * e.g. jira/teamcity/http client, DB connection etc.
 */
@JsonIgnoreType
public interface IntegrationFeatures extends AutoCloseable {
}
