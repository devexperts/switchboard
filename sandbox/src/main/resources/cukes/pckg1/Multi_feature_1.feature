@Regression
@Sanity
@UserRole#user1

Feature: Multi scenario feature

  Background:
    Given Everything is ok

  @Another_tag1
  @Negative
  Scenario: Scenario 1

    When I do something
    Then Everything is ok

  @Bat
  Scenario: Scenario 2

    When I do something else
    Then Everything is ok