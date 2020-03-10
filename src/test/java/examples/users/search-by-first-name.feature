Feature: Get User By Id

  Background:
    * url appUrl
    * def authorization = 'Bearer ' + token

  Scenario: Get User By Id
    Given path 'public-api','users'
    And param first_name = __arg.firstName
    And header Authorization = authorization

    When method get

    Then status 200
    And match response.result[0].first_name == __arg.firstName