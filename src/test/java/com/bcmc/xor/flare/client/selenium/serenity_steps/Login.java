package com.bcmc.xor.flare.client.selenium.serenity_steps;

import com.bcmc.xor.flare.client.selenium.CukesConfig;
import com.bcmc.xor.flare.client.selenium.selenium_pages.LoginPageObject;

public class Login {
    private CukesConfig cukesConfig = new CukesConfig();

    private LoginPageObject loginPageObject;

    public void withUser() {
        loginPageObject.enterLoginUsername(cukesConfig.getPropValues().getProperty("flareclientreact.user.email"));
        loginPageObject.enterLoginPassword(cukesConfig.getPropValues().getProperty("flareclientreact.user.password"));
        loginPageObject.clickLoginButton();
    }

    public void clickLogin() {
        loginPageObject.clickLoginLink();
    }

}
