package com.bvakp.automation.pages;

import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;

public class LoginPage extends BasePage {

    private static final double LOGIN_EKRANI_TIMEOUT = 15000;
    private static final double BASARILI_GIRIS_TIMEOUT = 20000;
    private static final double HATA_MESAJI_TIMEOUT = 10000;

    private final Locator kullaniciAdiAlani;
    private final Locator sifreAlani;
    private final Locator girisButonu;
    private final Locator girisHataMesaji;
    private final Locator gostergePanosu;


    /**
     * Login sayfasında kullanılacak locator'ları tanımlar.
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
     * Portal login sayfasını açmak için kullanılır.
     */
    public void girisSayfasiAcma() {

        openBaseUrl();
    }


    /**
     * Kullanıcı adı alanına verilen değeri girmek için kullanılır.
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
     * Şifre alanına verilen değeri girmek için kullanılır.
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
     * Sign In butonuna tıklayarak giriş işlemini başlatmak için kullanılır.
     */
    public void girisButonunaTiklama() {

        girisButonu.click();
    }


    /**
     * Kullanıcı adı ve şifre ile login işlemini tek adımda gerçekleştirmek için kullanılır.
     *
     * @param kullaniciAdi kullanıcı adı
     * @param sifre kullanıcı şifresi
     */
    public void girisYapma(
            String kullaniciAdi,
            String sifre) {

        kullaniciAdiGirme(
                kullaniciAdi
        );

        sifreGirme(
                sifre
        );

        girisButonunaTiklama();
    }


    /**
     * Login ekranındaki temel alanların görüntülendiğini doğrulamak için kullanılır.
     *
     * @return login alanları görünüyorsa true
     */
    public boolean girisEkraniGoruntuleniyorMu() {

        try {

            kullaniciAdiAlani.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(
                                    LOGIN_EKRANI_TIMEOUT
                            )
            );

            return kullaniciAdiAlani.isVisible()
                    && sifreAlani.isVisible()
                    && girisButonu.isVisible();

        } catch (PlaywrightException e) {

            ReportManager.error(
                    "Login ekranı kontrol edilirken hata oluştu: "
                            + e.getMessage()
            );

            return false;
        }
    }


    /**
     * Başarılı login sonrasında ana uygulama ekranının açıldığını doğrulamak için kullanılır.
     *
     * @return gösterge panosu görünüyorsa true
     */
    public boolean basariliGirisYapildiMi() {

        try {

            gostergePanosu.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(
                                    BASARILI_GIRIS_TIMEOUT
                            )
            );

            return gostergePanosu.isVisible();

        } catch (PlaywrightException e) {

            ReportManager.error(
                    "Başarılı giriş kontrol edilirken hata oluştu: "
                            + e.getMessage()
            );

            return false;
        }
    }


    /**
     * Hatalı giriş sonrasında hata mesajının görüntülendiğini doğrulamak için kullanılır.
     *
     * @return hata mesajı görünüyorsa true
     */
    public boolean girisHataMesajiGoruntulendiMi() {

        try {

            girisHataMesaji.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(
                                    HATA_MESAJI_TIMEOUT
                            )
            );

            return girisHataMesaji.isVisible();

        } catch (PlaywrightException e) {

            ReportManager.error(
                    "Login hata mesajı kontrol edilirken hata oluştu: "
                            + e.getMessage()
            );

            return false;
        }
    }


    /**
     * Login ekranında görüntülenen hata mesajının metnini almak için kullanılır.
     *
     * @return hata mesajı metni
     */
    public String girisHataMesajiAlma() {

        return girisHataMesaji
                .innerText()
                .trim();
    }
    /**
     * Login ekranının o anda görünür olup olmadığını
     * bekleme yapmadan kontrol etmek için kullanılır.
     *
     * AVP oturum hazırlama sırasında polling kontrolünde kullanılır.
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
     * Başarılı giriş sonrası Gösterge Panosu elementinin
     * o anda görünür olup olmadığını bekleme yapmadan kontrol eder.
     *
     * AVP oturum hazırlama sırasında polling kontrolünde kullanılır.
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
}