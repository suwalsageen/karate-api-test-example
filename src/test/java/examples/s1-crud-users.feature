@step1
Feature: User CRUD Operation

  Background:
#    * def userJson = read('classpath:examples/users/userData.json')


  Scenario: User CRUD
    * print '<##Create New User'
    * def createUser = call read('classpath:examples/users/create-user.feature')
    * print '##>'

    * def createdUser = createUser.response.result
    * json userUpdateJson = {"first_name":'#(createdUser.first_name)',"last_name":'#(createdUser.last_name)',"gender":'#(createdUser.gender)',"dob":"1995-05-05","email":'#(createdUser.email)',"phone":"333 333-3333","website":"https://gorest.co.in/","address":"Nepal","status":'#(createdUser.status)'}

    * print '<##Update User'
    * call read('classpath:examples/users/update-user.feature') userUpdateJson
    * print '##>'

    * print '<##Get All Users'
    * call read('classpath:examples/users/get-all-users.feature')
    * print '##>'

    * print '<##Get User By Id'
    * call read('classpath:examples/users/get-user-by-id.feature')
    * print '##>'

    * print '<##Search User By First Name'
    * json searchJson = {'firstName' : '#(createdUser.first_name)'}
    * def createUser = call read('classpath:examples/users/search-by-first-name.feature') searchJson
    * print '##>'

    * print '<##Delete User By Id'
    * def createUser = call read('classpath:examples/users/delete-user.feature')
    * print '##>'


