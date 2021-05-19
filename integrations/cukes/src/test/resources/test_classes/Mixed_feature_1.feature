@USER#user2

Feature: Mixed feature

  Background:
    Given Everything is ok

  @Regression
  Scenario: Scenario 1

    When I do something
    Then Everything is ok

  @Sanity
  Scenario Outline: Scenario outline 1

    When I do <action>
    Then Everything is ok

    Examples:
      | action         |
      | something      |
      | something else |

  @Bat
  Scenario: Scenario 2

    When I do something else
    Then Everything is ok