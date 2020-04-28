package com.bcmc.xor.flare.client.selenium.serenity_steps;

import com.bcmc.xor.flare.client.selenium.CukesConfig;
import com.bcmc.xor.flare.client.selenium.selenium_pages.CollectionManifestPageObject;
import net.thucydides.core.annotations.Step;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CollectionManifestStep {

    private CollectionManifestPageObject collectionManifestPageObject;

    private CukesConfig cukesConfig = new CukesConfig();

    @Step
    public void i_click_manifest_tab() {
        collectionManifestPageObject.clickManifestTab();
    }

    @Step
    public void i_enter_date_object_added_after() {
        collectionManifestPageObject.enterDateObjectAddedAfter(cukesConfig.getPropValues().getProperty("manifest.date.object.added.after"));
    }

    @Step
    public void i_enter_time_object_added_after() {
        collectionManifestPageObject.enterTimeObjectAddedAfter(cukesConfig.getPropValues().getProperty("manifest.time.object.added.after"));
    }

    @Step
    public void i_enter_object_id() {
        collectionManifestPageObject.enterObjectId(cukesConfig.getPropValues().getProperty("manifest.object.id"));
    }

    @Step
    public void i_enter_date_object_version() {
        collectionManifestPageObject.enterDateObjectVersion(cukesConfig.getPropValues().getProperty("manifest.date.object.version"));
    }

    @Step
    public void i_enter_time_object_version() {
        collectionManifestPageObject.enterTimeObjectVersion(cukesConfig.getPropValues().getProperty("manifest.time.object.version"));
    }

    @Step
    public void i_click_filter_button() {
        collectionManifestPageObject.clickFilterButton();
    }

    @Step
    public void i_assert_manifest_is_fetching_the_resource() {
        assertThat(collectionManifestPageObject.getManifestStatus(), is("Fetching Manifest Resource."));
    }

}
