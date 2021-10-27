/*
* Copyright (C) 2002 - 2021 Devexperts LLC
* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at https://mozilla.org/MPL/2.0/.
*/

@Regression
@Sanity
@Bat
@UserRole#user1
@operatingSystem#Windows

Feature: Outline scenario feature

  Background:
    Given Everything is ok

  @Another_tag1
  Scenario Outline: Scenario 1

    When I do <action>
    Then Everything is ok

    Examples:
      | action         |
      | something      |
      | something else |