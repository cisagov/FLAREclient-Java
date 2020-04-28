package com.bcmc.xor.flare.client.selenium.selenium_pages;

import com.bcmc.xor.flare.client.selenium.CukesConfig;
import net.serenitybdd.core.pages.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

public class ServerPageObject extends PageObject {

    private static CukesConfig cukesConfig = new CukesConfig();

    private static By addANewServerButton  = By.linkText("Add a new server");
    private static By serverAvailableCheckbox = By.id("available" + cukesConfig.getPropValues().getProperty("flarecloud.server.name"));
    private static By serverAvailableCheckboxEdit = By.id("available" + cukesConfig.getPropValues().getProperty("flarecloud.server.name") + "edit");
    private static By serverEditButton = By.id("edit" + cukesConfig.getPropValues().getProperty("flarecloud.server.name"));
    private static By deleteServerButton = By.id("delete" + cukesConfig.getPropValues().getProperty("flarecloud.server.name"));

    private static By serverLabel = By.name("label");
    private static By serverDiscoveryURL = By.name("url");
    private static By requireBasicAuthCheckbox = By.name("requiresBasicAuth");
    private static By basicAuthUsername = By.name("username");
    private static By basicAuthPassword = By.name("password");
    private static By saveServerButton = By.cssSelector(".btn.btn-primary");
    private static By deleteServerConfirmButton = By.cssSelector(".btn.btn-danger");

    public void clickAddANewServerButton() {
        WebElement serverElementButton = getDriver().findElement(addANewServerButton);
        assertThat(serverElementButton.getText(), is("Add a new server"));
        serverElementButton.click();
    }

    public void enterServerName(String serverName) {
        WebElement serverNameField = getDriver().findElement(serverLabel);
        serverNameField.clear();
        serverNameField.sendKeys(serverName);

    }

    public void enterServerDiscoveryUrl(String serverUrl) {
        WebElement serverDiscoveryUrlField = getDriver().findElement(serverDiscoveryURL);
        serverDiscoveryUrlField.clear();
        serverDiscoveryUrlField.sendKeys(serverUrl);

    }

    public void clickBasicAuthenticationRequiredCheckBox() {
        WebElement basicAuthenticationRequiredCheckbox = getDriver().findElement(requireBasicAuthCheckbox);
        basicAuthenticationRequiredCheckbox.click();
    }

    public void enterUsername(String username) {
        WebElement usernameField = getDriver().findElement(basicAuthUsername);
        usernameField.clear();
        usernameField.sendKeys(username);

    }

    public void enterPassword(String password) {
        WebElement passwordField = getDriver().findElement(basicAuthPassword);
        passwordField.clear();
        passwordField.sendKeys(password);

    }

    public void clickSaveServerButton() {
        WebElement saveButton = getDriver().findElement(saveServerButton);
        saveButton.click();
        waitFor(20).seconds();
    }

    public String isServerAvailable() {
        WebElement serverAvailableCheckboxElement = getDriver().findElement(serverAvailableCheckbox);
        return serverAvailableCheckboxElement.getAttribute("data-icon");
    }

    public void clickEditServerButton() {
        WebElement editServerElement = getDriver().findElement(serverEditButton);
        editServerElement.click();
    }

    public String isEditedServerAvailable() {
        WebElement serverAvailableCheckboxElement = getDriver().findElement(serverAvailableCheckboxEdit);
        return serverAvailableCheckboxElement.getAttribute("data-icon");
    }

    public void clickDeleteServerButton() {
        WebElement deleteServerButtonElement = getDriver().findElement(deleteServerButton);
        deleteServerButtonElement.click();
        waitFor(5).seconds();
    }

    public void clickDeleteServerConfirmButton() {
        WebElement deleteServerElement = getDriver().findElement(deleteServerConfirmButton);
        deleteServerElement.click();
    }

    public void isServerDeleted() {
        assertEquals(getDriver().findElements(serverAvailableCheckbox).size(),0);
    }

}
