package com.bcmc.xor.flare.client.selenium;

import com.bcmc.xor.flare.client.selenium.selenium_pages.FlareClientHomePageObject;
import com.bcmc.xor.flare.client.selenium.serenity_steps.NavigateTo;
import cucumber.api.java.After;
import cucumber.api.java.Before;

public class Hooks {

    private FlareClientHomePageObject flareClientHomePageObject;
    private NavigateTo navigateTo;

    public Hooks() {
        flareClientHomePageObject = new FlareClientHomePageObject();
        navigateTo = new NavigateTo();
    }

    @Before
    public void beforeScenario() {
        flareClientHomePageObject.openFlareClientHomePage();
    }

    @After
    public void afterScenario() {
        navigateTo.theAccountMenu();
        navigateTo.theSignOutPage();
    }
}
