package com.bcmc.xor.flare.client.selenium.selenium_pages;

import net.serenitybdd.core.pages.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class CollectionUploadPageObject extends PageObject {

    private static By uploadTab  = By.id("upload");
    private static By dropZoneUploadFile = By.xpath("//input[@type='file']");
    private static By publishButton = By.name("publish");
    private static By validateIcon = By.id("validateIcon");
    private static By publishStatus = By.className("Toastify__toast-body");

    public void clickUploadTab() {
        WebElement uploadTabElement = getDriver().findElement(uploadTab);
        uploadTabElement.click();
    }

    public void selectFileUpload(String uploadFilePath) {
        WebElement uploadFileElement = getDriver().findElement(dropZoneUploadFile);
        uploadFileElement.sendKeys(uploadFilePath);
    }

    public String getValidateIcon() {
        WebElement validateIconElement = getDriver().findElement(validateIcon);
        return validateIconElement.getAttribute("data-icon");
    }

    public void clickPublishButton() {
        WebElement publishButtonElement = getDriver().findElement(publishButton);
        publishButtonElement.click();
        waitFor(5).seconds();
    }

    public String getPulishStatus() {
        WebElement modalAlertElement = getDriver().findElement(publishStatus);
        String status = modalAlertElement.getText().substring(10,17);
        waitFor(10).seconds();
        return status;
    }
}
