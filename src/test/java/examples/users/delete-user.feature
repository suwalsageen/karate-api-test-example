Feature: Delete User By Id

  Background:
    * url appUrl
    * def authorization = 'Bearer ' + token
    * def userId = Java.type('examples.utils.User').getId()

  Scenario: Get User By Id
    Given path 'public-api','users', userId
    And header Authorization = authorization

    When method delete

    Then status 200