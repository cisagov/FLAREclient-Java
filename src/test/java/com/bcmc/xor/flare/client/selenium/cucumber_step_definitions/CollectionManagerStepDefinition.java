package com.bcmc.xor.flare.client.selenium.cucumber_step_definitions;

import com.bcmc.xor.flare.client.selenium.serenity_steps.*;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.thucydides.core.annotations.Steps;

public class CollectionManagerStepDefinition {

    @Steps
    private CollectionManagerStep collectionManagerStep;

    @Steps
    private CollectionUploadStep collectionUploadStep;

    @Steps
    private CollectionContentStep collectionContentStep;

    @Steps
    private CollectionDownloadStep collectionDownloadStep;

    @Steps
    private CollectionManifestStep collectionManifestStep;

    @Steps
    private CollectionActivitiesStep collectionActivitiesStep;

    @Steps
    private CollectionStatusesStep collectionStatusesStep;

    @Given("^the server and collection is exist$")
    public void the_server_and_collection_is_exist() {
        collectionManagerStep.i_navigate_to_collection();
    }

    @When("^I view the STIX content$")
    public void i_view_the_stix_contents() {
        collectionContentStep.i_click_content_tab();
        collectionContentStep.i_click_first_content_link();
    }

    @Then("^the content is not empty$")
    public void the_content_is_not_empty() {
        collectionContentStep.i_assert_the_content_is_not_empty();
    }

    @When("^I view the collection activities$")
    public void i_view_the_collection_activities() {
        collectionActivitiesStep.i_click_activities_tab();
    }

    @Then("^the activities should not be empty$")
    public void the_activities_should_not_be_empty() {
        collectionActivitiesStep.i_assert_activities_is_not_empty();
    }

    @When("^I manifest the object in the collection$")
    public void i_manifest_the_object_in_the_collection() {
        collectionManifestStep.i_click_manifest_tab();
        collectionManifestStep.i_enter_date_object_added_after();
        collectionManifestStep.i_enter_time_object_added_after();
        collectionManifestStep.i_enter_object_id();
        collectionManifestStep.i_enter_date_object_version();
        collectionManifestStep.i_enter_time_object_version();
        collectionManifestStep.i_click_filter_button();
    }

    @Then("^the manifest should be fetching$")
    public void the_manifest_should_be_fetching() {
        collectionManifestStep.i_assert_manifest_is_fetching_the_resource();
    }

    @When("^I select STIX 1.1 file to upload$")
    public void i_select_STIX11_file_to_upload() {
        collectionUploadStep.i_click_upload_tab();
        collectionUploadStep.i_select_STIX11_file_to_upload();
        collectionUploadStep.i_assert_the_stix_file_is_validated();
        collectionUploadStep.i_click_publish_button();
    }

    @When("^I select STIX 2.1 file to upload$")
    public void i_select_STIX21_file_to_upload() {
        collectionUploadStep.i_click_upload_tab();
        collectionUploadStep.i_select_STIX21_file_to_upload();
        //verify stix validation
        collectionUploadStep.i_click_publish_button();
    }

    @Then("^the file should be uploaded$")
    public void the_file_should_be_uploaded() {
        collectionUploadStep.i_assert_the_stix_file_is_uploaded();
    }

    @When("^I download STIX 1.1 contents$")
    public void i_download_STIX11_contents() {
        collectionDownloadStep.i_click_download_tab();
        collectionDownloadStep.i_enter_start_date();
        collectionDownloadStep.i_enter_start_time();
        collectionDownloadStep.i_enter_end_date();
        collectionDownloadStep.i_enter_end_time();
        collectionDownloadStep.i_select_STIX_content_versions();
        collectionDownloadStep.i_click_submit_button();
    }

    @When("^I download STIX 2.1 contents$")
    public void i_download_STIX21_contents() {
        collectionDownloadStep.i_click_download_tab();
        collectionDownloadStep.i_enter_added_after_date();
        collectionDownloadStep.i_enter_added_after_time();
        collectionDownloadStep.i_select_STIX_object_types();
        collectionDownloadStep.i_click_submit_button();
    }

    @Then("^download contents should be in process$")
    public void download_contents_should_be_in_process() {
        collectionDownloadStep.i_assert_download_stix_file_is_in_process();
    }


    @When("^I view the statuses page$")
    public void i_view_the_statuses_page() {
        collectionStatusesStep.i_navigate_to_statuses();
    }

    @Then("^the statuses page should be display$")
    public void the_statuses_page_should_be_display() {
        collectionStatusesStep.i_assert_the_statuses_page_display();
    }
}
