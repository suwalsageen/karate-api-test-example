Feature: Get User By Id

  Background:
    * url appUrl
    * def authorization = 'Bearer ' + token
    * def userId = Java.type('examples.utils.User').getId()

  Scenario: Get User By Id
    Given path '/public-api/users/' + userId
    And header Authorization = authorization

    When method get

    Then status 201
    And match response.result.id == userId