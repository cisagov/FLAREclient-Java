package com.bcmc.xor.flare.client.selenium.serenity_steps;

import com.bcmc.xor.flare.client.selenium.selenium_pages.StatusPageObject;
import net.thucydides.core.annotations.Step;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CollectionStatusesStep {

    private StatusPageObject statusPageObject;
    private NavigateTo navigateTo = new NavigateTo();

    @Step
    public void i_navigate_to_statuses() {
        navigateTo.theAdminMenu();
        navigateTo.theStatusPage();
    }

    @Step
    public void i_assert_the_statuses_page_display() {
        assertThat(statusPageObject.getStatusPageHeader(), is("Statuses"));
    }

}
