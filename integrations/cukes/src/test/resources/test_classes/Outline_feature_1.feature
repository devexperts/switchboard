@Regression
@Sanity
@Bat
@USER#user2

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