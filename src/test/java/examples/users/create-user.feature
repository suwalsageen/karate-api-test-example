Feature: Create New User
  Background:
    * url appUrl
    * def authorization = 'Bearer ' + token
    * def userJson = read('classpath:examples/users/userData.json')

  Scenario: Create New User
    Given path 'public-api','users'
    And header Authorization = authorization
    And header Content-Type = 'application/json'
    And request userJson

    When method post

    Then status 200
    And print 'The Response is: ', response
    * Java.type('examples.utils.User').setId(response.result.id)
    * Java.type('examples.utils.User').setObject(response.result)