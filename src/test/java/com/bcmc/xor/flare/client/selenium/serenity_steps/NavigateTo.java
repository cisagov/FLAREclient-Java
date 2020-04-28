package com.bcmc.xor.flare.client.selenium.serenity_steps;

import com.bcmc.xor.flare.client.selenium.selenium_pages.common_ui_components.FlareClientAccountMenu;
import com.bcmc.xor.flare.client.selenium.selenium_pages.common_ui_components.FlareClientAccountMenu.AccountMenuItem;
import com.bcmc.xor.flare.client.selenium.selenium_pages.common_ui_components.FlareClientAdminMenu;
import com.bcmc.xor.flare.client.selenium.selenium_pages.common_ui_components.FlareClientAdminMenu.AdminMenuItem;

public class NavigateTo {

    FlareClientAccountMenu flareClientAccountMenu;
    FlareClientAdminMenu flareClientAdminMenu;

    public NavigateTo() {
        flareClientAccountMenu = new FlareClientAccountMenu();
        flareClientAdminMenu = new FlareClientAdminMenu();
    }

    public void theAccountMenu() {
        flareClientAccountMenu.navigateToPage(AccountMenuItem.ACCOUNT);
    }
    public void theAdminMenu() {
        flareClientAdminMenu.navigateToPage(AdminMenuItem.ADMIN);
    }
    public void theSettingsPage() {
        flareClientAccountMenu.navigateToPage(AccountMenuItem.SETTINGS);
    }
    public void thePasswordPage() {
        flareClientAccountMenu.navigateToPage(AccountMenuItem.PASSWORD);
    }
    public void theSignInPage() {
        flareClientAccountMenu.navigateToPage(AccountMenuItem.SIGN_IN);
    }
    public void theSignOutPage() {
        flareClientAccountMenu.navigateToPage(AccountMenuItem.SIGN_OUT);
    }
    public void theEventsPage() {
        flareClientAdminMenu.navigateToPage(AdminMenuItem.EVENTS);
    }
    public void theStatusPage() {
        flareClientAdminMenu.navigateToPage(AdminMenuItem.STATUS);
    }

}
