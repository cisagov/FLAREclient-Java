package com.bcmc.xor.flare.client.selenium.cucumber_step_definitions;

import com.bcmc.xor.flare.client.selenium.selenium_pages.ServerPageObject;
import com.bcmc.xor.flare.client.selenium.serenity_steps.Login;
import com.bcmc.xor.flare.client.selenium.serenity_steps.NavigateTo;
import com.bcmc.xor.flare.client.selenium.serenity_steps.ServerManagerStep;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.thucydides.core.annotations.Steps;

public class ServerManagerStepDefinition {
    @Steps
    private Login login;

    @Steps
    private ServerPageObject serverPageObject;

    @Steps
    private ServerManagerStep serverManagerStep;

    @Steps
    private NavigateTo navigateTo;

    @Given("^the user account exists$")
    public void the_user_account_exists(){
        login.withUser();
    }

    @When("^I create the server$")
    public void i_create_the_server() {
        serverManagerStep.i_click_add_a_new_server_button();
        serverManagerStep.i_enter_server_name();
        serverManagerStep.i_enter_discovery_url();
        serverManagerStep.i_check_basic_authentication_required();
        serverManagerStep.i_enter_username();
        serverManagerStep.i_enter_password();
        serverManagerStep.i_click_save_button();
    }

    @Then("^the server should be available$")
    public void the_server_should_be_available() {
        serverManagerStep.i_assert_the_server_is_available();
    }

    @When("^I edit the server information$")
    public void i_edit_the_server_information() {
        serverManagerStep.i_click_edit_server_button();
        serverManagerStep.i_enter_server_name_in_edit();
        serverManagerStep.i_enter_username();
        serverManagerStep.i_enter_password();
        serverManagerStep.i_click_save_button();
        navigateTo.theAccountMenu();
        navigateTo.theSignOutPage();
        login.clickLogin();
        login.withUser();
    }

    @Then("^the server should be updated$")
    public void the_server_should_be_update() {
        serverManagerStep.i_assert_the_server_is_updated();
    }

    @When("^I delete the server$")
    public void i_delete_the_server() {
        serverManagerStep.i_click_delete_button();
        serverManagerStep.i_click_delete_confirm_button();
    }

    @Then("^the server should be deleted$")
    public void the_server_should_be_deleted() {
        serverManagerStep.i_assert_the_server_is_deleted();
    }
}
