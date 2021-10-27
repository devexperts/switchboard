@UserRole#user2

Feature: Mixed feature 2

  Background:
    Given Everything is ok

  @Regression
  @operatingSystem#Windows
  Scenario: Scenario 1

    When I do something
    Then Everything is ok

  @Sanity
  @operatingSystem#Linux
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