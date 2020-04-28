package com.bcmc.xor.flare.client.selenium.serenity_steps;

import com.bcmc.xor.flare.client.selenium.CukesConfig;
import com.bcmc.xor.flare.client.selenium.selenium_pages.*;
import net.thucydides.core.annotations.Step;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CollectionUploadStep {
    private CollectionUploadPageObject collectionUploadPageObject;

    private CukesConfig cukesConfig = new CukesConfig();

    @Step
    public void i_click_upload_tab() {
        collectionUploadPageObject.clickUploadTab();
    }

    @Step
    public void i_select_STIX11_file_to_upload() {
        collectionUploadPageObject.selectFileUpload(cukesConfig.getPropValues().getProperty("STIX11.UPLOAD.FILE.PATH"));
    }

    @Step
    public void i_select_STIX21_file_to_upload() {
        collectionUploadPageObject.selectFileUpload(cukesConfig.getPropValues().getProperty("STIX21.UPLOAD.FILE.PATH"));
    }

    @Step
    public void i_assert_the_stix_file_is_validated() {
        assertThat(collectionUploadPageObject.getValidateIcon(), is("check"));
    }

    @Step
    public void i_assert_the_stix_file_is_uploaded() {
        assertThat(collectionUploadPageObject.getPulishStatus(), is("SUCCESS"));
    }

    @Step
    public void i_click_publish_button() {
        collectionUploadPageObject.clickPublishButton();
    }

}
