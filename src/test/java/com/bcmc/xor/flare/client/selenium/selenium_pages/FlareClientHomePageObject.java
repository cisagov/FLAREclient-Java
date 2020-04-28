package com.bcmc.xor.flare.client.selenium.selenium_pages;

import com.bcmc.xor.flare.client.selenium.CukesConfig;
import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.annotations.findby.How;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import net.thucydides.core.annotations.WhenPageOpens;

public class FlareClientHomePageObject extends PageObject {

    @WhenPageOpens
    public void maximiseScreen() {
        getDriver().manage().window().fullscreen();
    }

    @FindBy(how = How.LINK_TEXT, using = "login")
    private WebElementFacade loginHyperlink;

    private CukesConfig cukesConfig = new CukesConfig();

    public void openFlareClientHomePage() {
        openAt(cukesConfig.getPropValues().getProperty("flareclientreact.url"));
    }

    public void clickLoginHyperlink() {
        loginHyperlink.click();
    }
}
