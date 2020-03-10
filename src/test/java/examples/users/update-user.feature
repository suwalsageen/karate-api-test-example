Feature: Update User
  Background:
    * url appUrl
    * def authorization = 'Bearer ' + token
    * def userId = Java.type('examples.utils.User').getId()

  Scenario: Update User
    Given path 'public-api','users', userId
    And header Authorization = authorization
    And header Content-Type = 'application/json'
    And request __arg

    When method patch
    Then status 200
    * Java.type('examples.utils.User').setObject(response.result)
    * print 'Updated User: ', response