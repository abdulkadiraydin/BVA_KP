package com.bvakp.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class LoginPage extends BasePage {

    private final Locator usernameInput;
    private final Locator passwordInput;
    private final Locator signInButton;
    private final Locator girisHataMesaji;

    public LoginPage(Page page) {
        super(page);

        usernameInput =
                page.getByLabel("Username or email");

        passwordInput =
                page.getByLabel(
                        "Password",
                        new Page.GetByLabelOptions().setExact(true)
                );

        signInButton =
                page.getByRole(
                        AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Sign In")
                );

        girisHataMesaji = page.getByText(
                "Invalid username or password.",
                new Page.GetByTextOptions().setExact(true)
        );
    }

    public void open() {
        openBaseUrl();
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