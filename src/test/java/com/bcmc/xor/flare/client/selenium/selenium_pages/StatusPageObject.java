package com.bcmc.xor.flare.client.selenium.selenium_pages;

import net.serenitybdd.core.pages.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class StatusPageObject extends PageObject {

    private static By statusPageHeader  = By.id("statuses");

    public String getStatusPageHeader() {
        WebElement statusPageHeaderElement = getDriver().findElement(statusPageHeader);
        return statusPageHeaderElement.getText();
    }

}
