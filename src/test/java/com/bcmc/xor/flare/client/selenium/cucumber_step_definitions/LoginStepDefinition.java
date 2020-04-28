package com.bcmc.xor.flare.client.selenium.cucumber_step_definitions;

import com.bcmc.xor.flare.client.selenium.serenity_steps.Login;
import cucumber.api.java.en.When;
import net.thucydides.core.annotations.Steps;

public class LoginStepDefinition {
    @Steps
    private Login login;

    @When("^I am a FLARE client Admin User$")
    public void i_am_a_flare_client_admin_user() {
        login.withUser();
    }
}
