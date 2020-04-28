package com.bcmc.xor.flare.client.selenium.selenium_pages.common_ui_components;

import org.openqa.selenium.By;
import static net.thucydides.core.webdriver.ThucydidesWebDriverSupport.getDriver;

public class FlareClientAdminMenu {

    public void navigateToPage(AdminMenuItem menuItem) {
        switch (menuItem) {
            case ADMIN:
                getDriver().findElement(By.id("admin-menu")).click();
                break;
            case EVENTS:
                getDriver().findElement(By.xpath("//a[@href='#/events']")).click();
                break;
            case STATUS:
                getDriver().findElement(By.xpath("//a[@href='#/status']")).click();
                break;
            case USER_MANAGEMENT:
                getDriver().findElement(By.linkText("User Management")).click();
                break;
            case METRICS:
                getDriver().findElement(By.linkText("Metrics")).click();
                break;
            case HEALTH:
                getDriver().findElement(By.linkText("Health")).click();
                break;
            case CONFIGURATION:
                getDriver().findElement(By.linkText("Configuration")).click();
                break;
            case AUDIT:
                getDriver().findElement(By.linkText("Audits")).click();
                break;
            case LOGS:
                getDriver().findElement(By.linkText("Logs")).click();
                break;
            case API_DOCS:
                getDriver().findElement(By.linkText("API Docs")).click();
                break;
            default:
                break;
        }
    }

    public enum AdminMenuItem {
        ADMIN, EVENTS, STATUS, USER_MANAGEMENT, METRICS, HEALTH, CONFIGURATION, AUDIT, LOGS,
        API_DOCS
    }
}
