package com.bcmc.xor.flare.client.selenium.selenium_pages;

import net.serenitybdd.core.pages.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class CollectionDownloadPageObject extends PageObject {

    private static By startDate = By.id("startDate");
    private static By startTime = By.id("startTime");
    private static By endDate = By.id("endDate");
    private static By endTime = By.id("endTime");
    private static By multipleSelect11 = By.id("contentBindings");
    private static By downloadTab  = By.id("download");
    private static By addedAfterDate = By.id("addedAfterDate");
    private static By addedAfterTime = By.id("addedAfterTime");
    private static By multipleSelect21 = By.id("types");
    private static By submitButton = By.name("submit");
    private static By downloadStatus = By.className("Toastify__toast-body");

    public void clickDownloadTab() {
        WebElement downloadTabElement = getDriver().findElement(downloadTab);
        downloadTabElement.click();
    }

    public void enterStartDate(String date) {
        WebElement startDateElement = getDriver().findElement(startDate);
        startDateElement.sendKeys(date);
    }

    public void enterStartTime(String date) {
        WebElement startTimeElement = getDriver().findElement(startTime);
        startTimeElement.sendKeys(date);
    }

    public void enterEndDate(String date) {
        WebElement endDateElement = getDriver().findElement(endDate);
        endDateElement.sendKeys(date);
    }

    public void enterEndTime(String date) {
        WebElement endTimeElement = getDriver().findElement(endTime);
        endTimeElement.sendKeys(date);
    }

    public void selectSTIX11ObjectTypes(String multipleVals) {
        String multipleSel[] = multipleVals.split(",");

        for (String valueToBeSelected : multipleSel) {
            new Select(getDriver().findElement(multipleSelect11)).selectByVisibleText(valueToBeSelected);
        }
    }

    public void enterAddedAfterDate(String date) {
        WebElement addedAfterDateElement = getDriver().findElement(addedAfterDate);
        addedAfterDateElement.sendKeys(date);
    }

    public void enterAddedAfterTime(String time) {
        WebElement addedAfterTimeElement = getDriver().findElement(addedAfterTime);
        addedAfterTimeElement.sendKeys(time);
    }

    public void selectSTIX21ObjectTypes(String multipleVals) {
        String multipleSel[] = multipleVals.split(",");

        for (String valueToBeSelected : multipleSel) {
            new Select(getDriver().findElement(multipleSelect21)).selectByVisibleText(valueToBeSelected);
        }
    }

    public void clickSubmitButton() {
        WebElement submitButtonElement = getDriver().findElement(submitButton);
        submitButtonElement.click();
        waitFor(5).seconds();
    }

    public String getDonwloadStatus() {
        WebElement downloadStatusElement = getDriver().findElement(downloadStatus);
        String status = downloadStatusElement.getText();
        waitFor(10).seconds();
        return status;
    }
}
