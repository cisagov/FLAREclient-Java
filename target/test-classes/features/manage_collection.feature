@Manage_collections
Feature: Manage collections
    As a FLARE Client user
    I want to view STIX contents, upload, download, manifest, and view activities

    Background:
        Given I am a FLARE client Admin User
        And the server and collection is exist

    @Contents
    Scenario: View STIX contents
        When I view the STIX content
        Then the content is not empty

    @Upload_STIX11
    Scenario: Upload STIX 1.1 contents
        When I select STIX 1.1 file to upload
        Then the file should be uploaded

    @Upload_STIX21
    Scenario: Upload STIX 2.1 contents
        When I select STIX 2.1 file to upload
        Then the file should be uploaded

    @Download_STIX11
    Scenario: Download STIX 1.1  contents
        When I download STIX 1.1 contents
        Then download contents should be in process

    @Download_STIX21
    Scenario: Download STIX 2.1 contents
        When I download STIX 2.1 contents
        Then download contents should be in process

    @Manifest
    Scenario: Manifest STIX 1.1 contents
        When I manifest the object in the collection
        Then the manifest should be fetching

    @Activities
    Scenario: View the activities
        When I view the collection activities
        Then the activities should not be empty

    @Statuses
    Scenario: View the statuses
        When I view the statuses page
        Then the statuses page should be display
