/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.devexperts.switchboard.entities.attributes;

import com.devexperts.switchboard.entities.Attributes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.function.Predicate;

/**
 * Represents a (de)serializable predicate of {@link Attributes}
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface AttributePredicate extends Predicate<Attributes> {
}
