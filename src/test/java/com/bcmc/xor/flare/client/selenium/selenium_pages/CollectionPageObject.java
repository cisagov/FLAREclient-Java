package com.bcmc.xor.flare.client.selenium.selenium_pages;

import com.bcmc.xor.flare.client.selenium.CukesConfig;
import net.serenitybdd.core.pages.PageObject;
import org.openqa.selenium.By;

public class CollectionPageObject extends PageObject {

    private static CukesConfig cukesConfig = new CukesConfig();

    private static By taxiiVersion = By.xpath("//*[@id='servers']/tbody/tr/td[2]");

    private static By FLAREcloudServerLink = By.linkText(cukesConfig.getPropValues().getProperty("flarecloud.server.name"));

    private static By FLAREcloudCollectionLink  = By.linkText(cukesConfig.getPropValues().getProperty("flarecloud.collection.name"));

    private static By FLARE31ServerLink = By.linkText(cukesConfig.getPropValues().getProperty("flare31.server.name"));

    private static By FLARE31CollectionLink  = By.linkText(cukesConfig.getPropValues().getProperty("flarec31.collection.name"));

    public String getTaxiiVersion() {
        return getDriver().findElement(taxiiVersion).getText();
    }
    public void navigateToCollection() {

        switch(getTaxiiVersion() ) {
            case "TAXII11":
                getDriver().findElement(FLARE31ServerLink).click();
                getDriver().findElement(FLARE31CollectionLink).click();
                break;
            case "TAXII20":
                getDriver().findElement(FLAREcloudServerLink).click();
                getDriver().findElement(FLAREcloudCollectionLink).click();
                break;
            default:
                System.out.println("NO SERVER FOUND");
        }
    }
}
