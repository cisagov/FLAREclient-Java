@Manage_servers
Feature: Manage servers
    As a FLARE Client user
    I want to manage the server
    So that, I can configure the api roots

    Background:
        Given I am a FLARE client Admin User

    @Create_server
    Scenario: Create new server
        When I create the server
        Then the server should be available

    @Edit_server
    Scenario: Edit the server config
        When I edit the server information
        Then the server should be updated

    @Delete_server
    Scenario: Delete the server
        When I delete the server
        Then the server should be deleted
