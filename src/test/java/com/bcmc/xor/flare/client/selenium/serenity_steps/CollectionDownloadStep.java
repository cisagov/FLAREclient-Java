package com.bcmc.xor.flare.client.selenium.serenity_steps;

import com.bcmc.xor.flare.client.selenium.CukesConfig;
import com.bcmc.xor.flare.client.selenium.selenium_pages.CollectionDownloadPageObject;
import net.thucydides.core.annotations.Step;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CollectionDownloadStep {

    private CollectionDownloadPageObject collectionDownloadPageObject;

    private CukesConfig cukesConfig = new CukesConfig();

    @Step
    public void i_click_download_tab() {
        collectionDownloadPageObject.clickDownloadTab();
    }

    @Step
    public void i_enter_start_date() {
        collectionDownloadPageObject.enterStartDate(cukesConfig.getPropValues().getProperty("STIX11.DOWNLOAD.START.DATE"));
    }

    @Step
    public void i_enter_start_time() {
        collectionDownloadPageObject.enterStartTime(cukesConfig.getPropValues().getProperty("STIX11.DOWNLOAD.START.TIME"));
    }

    @Step
    public void i_enter_end_date() {
        collectionDownloadPageObject.enterEndDate(cukesConfig.getPropValues().getProperty("STIX11.DOWNLOAD.END.DATE"));
    }

    @Step
    public void i_enter_end_time() {
        collectionDownloadPageObject.enterEndTime(cukesConfig.getPropValues().getProperty("STIX11.DOWNLOAD.END.TIME"));
    }

    @Step
    public void i_select_STIX_content_versions() {
        collectionDownloadPageObject.selectSTIX11ObjectTypes(cukesConfig.getPropValues().getProperty("STIX11.DOWNLOAD.STIX.CONTENT.VERSIONS"));
    }

    @Step
    public void i_enter_added_after_date() {
        collectionDownloadPageObject.enterAddedAfterDate(cukesConfig.getPropValues().getProperty("STIX21.DOWNLOAD.ADDED.AFTER.DATE"));
    }

    @Step
    public void i_enter_added_after_time() {
        collectionDownloadPageObject.enterAddedAfterTime(cukesConfig.getPropValues().getProperty("STIX21.DOWNLOAD.ADDED.AFTER.TIME"));
    }

    @Step
    public void i_select_STIX_object_types() {
        collectionDownloadPageObject.selectSTIX21ObjectTypes(cukesConfig.getPropValues().getProperty("STIX21.DOWNLOAD.STIX.OBJECT.TYPES"));
    }

    @Step
    public void i_click_submit_button() {
        collectionDownloadPageObject.clickSubmitButton();
    }

    @Step
    public void i_assert_download_stix_file_is_in_process() {
        assertThat(collectionDownloadPageObject.getDonwloadStatus(), is("Started async fetch"));
    }
}
