package com.bcmc.xor.flare.client.selenium.selenium_pages;

import net.serenitybdd.core.pages.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class CollectionContentPageObject extends PageObject {

    private static By contentTab  = By.id("content");
    private static By firstContentLink = By.xpath("(//*[@id='stixPackage'])[1]");
    private static By contentDetail = By.xpath("//code");

    public void clickContentTab() {
        WebElement contentTabElement = getDriver().findElement(contentTab);
        contentTabElement.click();
    }

    public void clickFirstContentLink() {
        WebElement firstContentLinkElement = getDriver().findElement(firstContentLink);
        firstContentLinkElement.click();
    }

    public String getContentDetail() {
        WebElement contentDetailElement = getDriver().findElement(contentDetail);
        return contentDetailElement.getText();
    }
}
