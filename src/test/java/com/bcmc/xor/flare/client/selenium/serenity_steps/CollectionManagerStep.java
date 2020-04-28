package com.bcmc.xor.flare.client.selenium.serenity_steps;

import com.bcmc.xor.flare.client.selenium.selenium_pages.*;
import net.thucydides.core.annotations.Step;

public class CollectionManagerStep {
    private CollectionPageObject collectionPageObject;

    @Step
    public void i_navigate_to_collection() { collectionPageObject.navigateToCollection(); }

}
