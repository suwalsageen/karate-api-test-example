Feature: Get All Users

  Background:
    * url appUrl
    * def authorization = 'Bearer ' + token

  Scenario: Get User By Id
    Given path 'public-api','users'
    And header Authorization = authorization

    When method get

    Then status 200