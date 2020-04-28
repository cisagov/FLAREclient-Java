package com.bcmc.xor.flare.client.selenium.serenity_steps;

import com.bcmc.xor.flare.client.selenium.selenium_pages.CollectionActivitiesPageObject;
import net.thucydides.core.annotations.Step;
import org.junit.Assert;

public class CollectionActivitiesStep {

    private CollectionActivitiesPageObject collectionActivitiesPageObject;

    @Step
    public void i_click_activities_tab() {
        collectionActivitiesPageObject.clickActivitiesTab();
    }

    @Step
    public void i_assert_activities_is_not_empty() {
        Assert.assertTrue(!collectionActivitiesPageObject.getActivitiesData().isEmpty());
    }

}
