package com.bcmc.xor.flare.client.selenium.selenium_pages;

import net.serenitybdd.core.pages.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class CollectionActivitiesPageObject extends PageObject {

    private static By activitiesTab  = By.id("activities");
    private static By activitiesData = By.xpath("//table/tbody/tr[1]/td[1]");

    public void clickActivitiesTab() {
        WebElement activitiesTabElement = getDriver().findElement(activitiesTab);
        activitiesTabElement.click();
    }

    public String getActivitiesData() {
        WebElement activitiesDataElement = getDriver().findElement(activitiesData);
        return activitiesDataElement.getText();
    }

}
