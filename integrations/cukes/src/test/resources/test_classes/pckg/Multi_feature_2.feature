@Regression
@Sanity
@USER#user2

Feature: Multi scenario feature 2

  Background:
    Given Everything is ok

  @Another_tag3
  Scenario: Scenario 1

    When I do something
    Then Everything is ok

  @Bat
  Scenario: Scenario 2

    When I do something else
    Then Everything is ok