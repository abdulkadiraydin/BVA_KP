package com.bvakp.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class LoginPage extends BasePage {

    private final Locator usernameInput;
    private final Locator passwordInput;
    private final Locator loginButton;
    private final Locator loginErrorMessage;

    public LoginPage(Page page) {
        super(page);

        this.usernameInput = page.locator("#username");
        this.passwordInput = page.locator("#password");
        this.loginButton = page.locator("#kc-login");
        this.loginErrorMessage = page.locator(".kc-feedback-text");
    }

    public LoginPage open() {
        openBaseUrl();
        return this;
    }

    /**
     * Login ekranındaki kullanıcı adı alanına verilen kullanıcı adını girmek için kullanılır.
     *
     * @param kullaniciAdi giriş yapılacak kullanıcı adı
     */
    public void kullaniciAdiGirme(String kullaniciAdi) {
        usernameInput.fill(kullaniciAdi);
    }

    /**
     * Login ekranındaki şifre alanına verilen şifreyi girmek için kullanılır.
     *
     * @param sifre giriş yapılacak kullanıcı şifresi
     */
    public void sifreGirme(String sifre) {
        passwordInput.fill(sifre);
    }

    /**
     * Kullanıcı adı ve şifre bilgileri girildikten sonra
     * giriş işlemini başlatmak için Sign In butonuna tıklar.
     */
    public void girisButonunaTiklama() {
        signInButton.click();
    }
    public boolean isLoginPageDisplayed() {
        try {
            usernameInput.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(10_000)
            );

            return usernameInput.isVisible();

        } catch (Exception e) {
            return false;
        }
    }
    public String getLoginErrorMessage() {
        return loginErrorMessage.innerText().trim();
    }

    public boolean isLoginPageVisible() {

        usernameInput.waitFor();

        return usernameInput.isVisible()
                && passwordInput.isVisible()
                && signInButton.isVisible();
    }

    /**
     * Giriş işlemi sonrasında kullanıcı adı veya şifre hatası
     * gösterilip gösterilmediğini kontrol etmek için kullanılır.
     *
     * @return hata mesajı görünüyorsa true, görünmüyorsa false
     */
    public boolean girisHataMesajiGoruntulenmeKontrolu() {
        return girisHataMesaji.isVisible();
    }

    /**
     * Login ekranındaki kullanıcı adı alanının o anda
     * görünür olup olmadığını kontrol etmek için kullanılır.
     *
     * @return login ekranı görünüyorsa true
     */
    public boolean girisEkraniGoruntuleniyorMu() {
        return usernameInput.isVisible();
    }
}