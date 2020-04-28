package com.bcmc.xor.flare.client.selenium.selenium_pages.common_ui_components;

import org.openqa.selenium.By;
import static net.thucydides.core.webdriver.ThucydidesWebDriverSupport.getDriver;

public class FlareClientAccountMenu {

    public void navigateToPage(AccountMenuItem menuItem) {
        switch (menuItem) {
            case ACCOUNT:
                getDriver().findElement(By.id("account-menu")).click();
                break;
            case SETTINGS:
                getDriver().findElement(By.linkText("Settings")).click();
                break;
            case PASSWORD:
                getDriver().findElement(By.linkText("Password")).click();
                break;
            case SIGN_IN:
                getDriver().findElement(By.xpath("//a[@href='#/login']")).click();
                break;
            case SIGN_OUT:
                getDriver().findElement(By.xpath("//a[@href='#/logout']")).click();
                break;
            default:
                break;
        }
    }

    public enum AccountMenuItem {
        ACCOUNT, SETTINGS, PASSWORD, SIGN_IN, SIGN_OUT
    }
}
