package com.bcmc.xor.flare.client.selenium.selenium_pages;

import net.serenitybdd.core.pages.PageObject;
import org.openqa.selenium.By;

public class LoginPageObject extends PageObject {
    private static By loginUsernameField  = By.id("username");
    private static By loginPasswordField  = By.id("password");
    private static By signInButton  = By.xpath("//button[@class='btn btn-primary']");
    private static By cancelButton  = By.xpath("//button[@class='btn btn-secondary']");
    private static By loginLink = By.linkText("login");

    public void enterLoginUsername(String username) {
        getDriver().findElement(loginUsernameField).sendKeys(username);
    }

    public void enterLoginPassword(String password) {
        getDriver().findElement(loginPasswordField).sendKeys(password);
    }

    public void clickLoginButton() {
        getDriver().findElement(signInButton).click();
    }

    public void cancelButton() {
        getDriver().findElement(cancelButton).click();
    }

    public void clickLoginLink() { getDriver().findElement(loginLink).click(); }
}
