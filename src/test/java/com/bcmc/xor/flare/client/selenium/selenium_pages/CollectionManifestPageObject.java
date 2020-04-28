package com.bcmc.xor.flare.client.selenium.selenium_pages;

import net.serenitybdd.core.pages.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class CollectionManifestPageObject extends PageObject {

    private static By manifestTab  = By.id("manifest");
    private static By dateAddedAfter = By.id("addedAfterDate");
    private static By timeAddedAfter = By.id("addedAfterTime");
    private static By objectIdField = By.id("objectId");
    private static By dateObjectVersion = By.id("objectVersionDate");
    private static By timeObjectVersion = By.id("objectVersionTime");
    private static By filterButton = By.name("filter");
    private static By manifestStatus = By.className("Toastify__toast-body");

    public void clickManifestTab() {
        WebElement manifestTabElement = getDriver().findElement(manifestTab);
        manifestTabElement.click();
    }

    public void enterDateObjectAddedAfter(String date) {
        WebElement dateAddedAfterElement = getDriver().findElement(dateAddedAfter);
        dateAddedAfterElement.sendKeys(date);
    }

    public void enterTimeObjectAddedAfter(String time) {
        WebElement timeAddedAfterElement = getDriver().findElement(timeAddedAfter);
        timeAddedAfterElement.sendKeys(time);
    }

    public void enterObjectId(String objectId) {
        WebElement objectIdElement = getDriver().findElement(objectIdField);
        objectIdElement.sendKeys(objectId);
    }

    public void enterDateObjectVersion(String date) {
        WebElement dateObjectVersionElement = getDriver().findElement(dateObjectVersion);
        dateObjectVersionElement.sendKeys(date);
    }

    public void enterTimeObjectVersion(String time) {
        WebElement timeObjectVersionElement = getDriver().findElement(timeObjectVersion);
        timeObjectVersionElement.sendKeys(time);
    }

    public void clickFilterButton() {
        WebElement filterButtonElement = getDriver().findElement(filterButton);
        filterButtonElement.click();
        waitFor(5).seconds();
    }

    public String getManifestStatus() {
        WebElement manifestStatusElement = getDriver().findElement(manifestStatus);
        String status = manifestStatusElement.getText();
        waitFor(10).seconds();
        return status;
    }

}
