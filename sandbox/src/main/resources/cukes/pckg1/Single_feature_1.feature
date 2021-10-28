/*
* Copyright (C) 2002 - 2021 Devexperts LLC
* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at https://mozilla.org/MPL/2.0/.
*/

@Regression
@Bat
@UserRole#user1

Feature: Single scenario feature

  Background:
    Given Everything is ok

  @Another_tag1
  Scenario: Scenario 1

    When I do something
    And I do something else
    Then Everything is ok