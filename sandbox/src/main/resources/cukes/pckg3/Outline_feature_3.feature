@Regression
@Sanity
@Bat
@UserRole#user2
@operatingSystem#Windows

Feature: Outline scenario feature 3

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