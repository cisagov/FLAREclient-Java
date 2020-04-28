package com.bcmc.xor.flare.client.selenium.serenity_steps;

import com.bcmc.xor.flare.client.selenium.selenium_pages.CollectionContentPageObject;
import net.thucydides.core.annotations.Step;
import org.junit.Assert;

public class CollectionContentStep {
    private CollectionContentPageObject collectionContentPageObject;

    @Step
    public void i_click_content_tab() {
        collectionContentPageObject.clickContentTab();
    }

    @Step
    public void i_click_first_content_link() {collectionContentPageObject.clickFirstContentLink();}


    @Step
    public void i_assert_the_content_is_not_empty() {
        Assert.assertTrue(!collectionContentPageObject.getContentDetail().isEmpty());
    }

}
