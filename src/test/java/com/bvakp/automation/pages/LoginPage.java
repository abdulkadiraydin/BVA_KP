package com.bvakp.automation.pages;

import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;

public class LoginPage extends BasePage {

    private final Locator kullaniciAdiAlani;
    private final Locator sifreAlani;
    private final Locator girisButonu;
    private final Locator girisHataMesaji;
    private final Locator gostergePanosu;


    /**
     * Login ekranındaki elementleri tanımlar.
     *
     * @param page aktif Playwright sayfası
     */
    public LoginPage(Page page) {

        super(page);

        this.kullaniciAdiAlani =
                page.getByLabel(
                        "Username or email"
                );

        this.sifreAlani =
                page.getByLabel(
                        "Password",
                        new Page.GetByLabelOptions()
                                .setExact(true)
                );

        this.girisButonu =
                page.getByRole(
                        AriaRole.BUTTON,
                        new Page.GetByRoleOptions()
                                .setName("Sign In")
                                .setExact(true)
                );

        this.girisHataMesaji =
                page.getByText(
                        "Invalid username or password.",
                        new Page.GetByTextOptions()
                                .setExact(true)
                );

        /*
         * Recorder çıktısından alınan
         * başarılı giriş sonrası stabil locator.
         */
        this.gostergePanosu =
                page.getByLabel(
                        "breadcrumb"
                ).getByRole(
                        AriaRole.BUTTON,
                        new Locator.GetByRoleOptions()
                                .setName("Gösterge Panosu")
                                .setExact(true)
                );
    }


    /**
     * Portal login adresini açar ve temel
     * DOM yüklemesinin tamamlanmasını bekler.
     */
    public void girisSayfasiAcma() {

        openBaseUrl();

        page.waitForLoadState(
                LoadState.DOMCONTENTLOADED
        );
    }


    /**
     * Kullanıcı adı alanına verilen değeri girer.
     *
     * @param kullaniciAdi kullanıcı adı
     */
    public void kullaniciAdiGirme(
            String kullaniciAdi) {

        kullaniciAdiAlani.fill(
                kullaniciAdi
        );
    }


    /**
     * Şifre alanına verilen değeri girer.
     *
     * @param sifre kullanıcı şifresi
     */
    public void sifreGirme(
            String sifre) {

        sifreAlani.fill(
                sifre
        );
    }


    /**
     * Sign In butonuna tıklayarak
     * giriş işlemini başlatır.
     */
    public void girisButonunaTiklama() {

        girisButonu.click();
    }


    /**
     * Login ekranındaki kullanıcı adı, şifre ve
     * Sign In butonunun görünür olduğunu doğrular.
     *
     * Bu metot gerektiğinde elementleri bekler.
     *
     * @return tüm login elementleri görünüyorsa true
     */
    public boolean girisEkraniGoruntuleniyorMu() {

        try {

            kullaniciAdiAlani.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(15000)
            );

            sifreAlani.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(15000)
            );

            girisButonu.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(15000)
            );

            return kullaniciAdiAlani.isVisible()
                    && sifreAlani.isVisible()
                    && girisButonu.isVisible();

        } catch (PlaywrightException e) {

            return false;
        }
    }


    /**
     * Login ekranının o anda görünür olup
     * olmadığını bekleme yapmadan kontrol eder.
     *
     * AVP oturum hazırlama sırasında polling
     * kontrolü için kullanılır.
     *
     * @return login ekranı görünüyorsa true
     */
    public boolean loginEkraniGorunurMu() {

        try {

            return kullaniciAdiAlani.isVisible()
                    && sifreAlani.isVisible()
                    && girisButonu.isVisible();

        } catch (PlaywrightException e) {

            return false;
        }
    }


    /**
     * Login ekranının açılmasını kısa süre bekler.
     *
     * Eski kullanımlarla uyumluluk için korunmaktadır.
     *
     * @return login ekranı görünüyorsa true
     */
    public boolean loginEkraniAcikMi() {

        try {

            kullaniciAdiAlani.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(5000)
            );

            return kullaniciAdiAlani.isVisible();

        } catch (PlaywrightException e) {

            return false;
        }
    }


    /**
     * Gösterge Panosu elementinin o anda görünür
     * olup olmadığını bekleme yapmadan kontrol eder.
     *
     * @return Gösterge Panosu görünüyorsa true
     */
    public boolean gostergePanosuGorunurMu() {

        try {

            return gostergePanosu.isVisible();

        } catch (PlaywrightException e) {

            return false;
        }
    }


    /**
     * Başarılı giriş sonrasında Gösterge Panosu
     * ekranının görüntülenmesini bekler.
     *
     * @return Gösterge Panosu görünüyorsa true
     */
    public boolean basariliGirisYapildiMi() {

        try {

            gostergePanosu.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(20000)
            );

            return gostergePanosu.isVisible();

        } catch (PlaywrightException e) {

            return false;
        }
    }


    /**
     * Hatalı giriş mesajının görüntülenip
     * görüntülenmediğini kontrol eder.
     *
     * @return hata mesajı görüntüleniyorsa true
     */
    public boolean girisHataMesajiGoruntulendiMi() {

        try {

            girisHataMesaji.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(10000)
            );

            return girisHataMesaji.isVisible();

        } catch (PlaywrightException e) {

            return false;
        }
    }


    /**
     * Login ekranındaki hata mesajını döndürür.
     *
     * @return hata mesajı
     */
    public String girisHataMesajiAlma() {

        return girisHataMesaji
                .innerText()
                .trim();
    }


    /**
     * Eski testlerde kullanılan open metodunu destekler.
     * Yeni testlerde girisSayfasiAcma() kullanılmalıdır.
     *
     * @return mevcut LoginPage
     */
    @Deprecated
    public LoginPage open() {

        girisSayfasiAcma();

        ReportManager.info(
                "Login yönlendirmesi sonrası URL: "
                        + page.url()
        );

        return this;
    }


    /**
     * Eski testlerde kullanılan kullanıcı adı
     * giriş metodunu destekler.
     *
     * @param kullaniciAdi kullanıcı adı
     * @return mevcut LoginPage
     */
    @Deprecated
    public LoginPage enterUsername(
            String kullaniciAdi) {

        kullaniciAdiGirme(
                kullaniciAdi
        );

        return this;
    }


    /**
     * Eski testlerde kullanılan şifre
     * giriş metodunu destekler.
     *
     * @param sifre kullanıcı şifresi
     * @return mevcut LoginPage
     */
    @Deprecated
    public LoginPage enterPassword(
            String sifre) {

        sifreGirme(
                sifre
        );

        return this;
    }


    /**
     * Eski testlerde kullanılan giriş butonu
     * metodunu destekler.
     *
     * @return mevcut LoginPage
     */
    @Deprecated
    public LoginPage clickLogin() {

        girisButonunaTiklama();

        return this;
    }
}