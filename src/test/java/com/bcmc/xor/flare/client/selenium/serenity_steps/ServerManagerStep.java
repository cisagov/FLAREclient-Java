package com.bcmc.xor.flare.client.selenium.serenity_steps;

import com.bcmc.xor.flare.client.selenium.CukesConfig;
import com.bcmc.xor.flare.client.selenium.selenium_pages.ServerPageObject;
import net.thucydides.core.annotations.Step;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;

public class ServerManagerStep {
    private ServerPageObject serverPageObject;
    private CukesConfig cukesConfig = new CukesConfig();

    @Step
    public void i_click_add_a_new_server_button() {
        serverPageObject.clickAddANewServerButton();
    }

    @Step
    public void i_click_edit_server_button() {
        serverPageObject.clickEditServerButton();
    }

    @Step
    public void i_enter_server_name() {
        serverPageObject.enterServerName(cukesConfig.getPropValues().getProperty("flarecloud.server.name"));
    }

    @Step
    public void i_enter_server_name_in_edit() {
        serverPageObject.enterServerName(cukesConfig.getPropValues().getProperty("flarecloud.server.name") + "edit");
    }

    @Step
    public void i_enter_discovery_url() {
        serverPageObject.enterServerDiscoveryUrl(cukesConfig.getPropValues().getProperty("flarecloud.server.discovery.url"));
    }

    @Step
    public void i_check_basic_authentication_required() {
        serverPageObject.clickBasicAuthenticationRequiredCheckBox();
    }

    @Step
    public void i_enter_username() {
        serverPageObject.enterUsername(cukesConfig.getPropValues().getProperty("flarecloud.server.username"));
    }

    @Step
    public void i_enter_password() {
        serverPageObject.enterPassword(cukesConfig.getPropValues().getProperty("flarecloud.server.password"));
    }

    @Step
    public void i_click_save_button() {
        serverPageObject.clickSaveServerButton();
    }

    @Step
    public void i_assert_the_server_is_available() {
        assertThat(serverPageObject.isServerAvailable(), is("check"));
    }

    @Step
    public void i_assert_the_server_is_updated() {
        assertThat(serverPageObject.isEditedServerAvailable(), is("check"));
    }

    @Step
    public void i_click_delete_button() {
        serverPageObject.clickDeleteServerButton();
    }

    @Step
    public void i_click_delete_confirm_button() {
        serverPageObject.clickDeleteServerConfirmButton();
    }

    @Step
    public void i_assert_the_server_is_deleted() {
        assertThat(serverPageObject.isServerAvailable(), isEmptyOrNullString());
    }
}
