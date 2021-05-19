@Regression
@Sanity
@Bat
@USER#user3

Feature: Outline scenario feature 2

  Background:
    Given Everything is ok

  @Another_tag2
  Scenario Outline: Scenario 1

    When I do <action>
    Then Everything is ok

    Examples:
      | action         |
      | something      |
      | something else |