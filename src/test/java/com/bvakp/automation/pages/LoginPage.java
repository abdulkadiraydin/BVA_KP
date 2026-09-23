package com.bvakp.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class LoginPage extends BasePage {

    private final Locator usernameInput;
    private final Locator passwordInput;
    private final Locator loginButton;

    public LoginPage(Page page) {

        super(page);

        this.usernameInput = page.locator("#username");
        this.passwordInput = page.locator("#password");
        this.loginButton = page.locator("#loginButton");
    }

    public LoginPage open() {

        openBaseUrl();

        return this;
    }

    public LoginPage enterUsername(String username) {

        usernameInput.fill(username);

        return this;
    }

    public LoginPage enterPassword(String password) {

        passwordInput.fill(password);

        return this;
    }

    public LoginPage clickLogin() {

        loginButton.click();

        return this;
    }


}