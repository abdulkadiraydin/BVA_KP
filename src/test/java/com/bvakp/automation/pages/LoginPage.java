
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
     * Yeni giriş ekranına ait locator'ları tanımlar.
     *
     * @param page aktif Playwright sayfası
     */
    public LoginPage(Page page) {

        super(page);

        this.kullaniciAdiAlani =
                page.getByPlaceholder(
                        "name.surname@tcddtasimacilik."
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
                                .setName("Sign in")
                                .setExact(true)
                );

        // Yeni ekrandaki hata metni doğrulanana kadar korunur.
        this.girisHataMesaji =
                page.getByText(
                        "Invalid username or password.",
                        new Page.GetByTextOptions()
                                .setExact(true)
                );

        // Başarılı giriş sonrası mevcut uygulama locator'ı.
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
     * Portal adresini açarak giriş ekranına yönlendirir.
     */
    public void girisSayfasiAcma() {

        openBaseUrl();
    }

    /**
     * Kullanıcı adı veya e-posta alanını doldurur.
     *
     * @param kullaniciAdi kullanıcı adı
     */
    public void kullaniciAdiGirme(String kullaniciAdi) {

        kullaniciAdiAlani.fill(kullaniciAdi);
    }

    /**
     * Şifre alanını doldurur.
     *
     * @param sifre kullanıcı şifresi
     */
    public void sifreGirme(String sifre) {

        sifreAlani.fill(sifre);
    }

    /**
     * Giriş butonuna tıklayarak kimlik doğrulamayı başlatır.
     */
    public void girisButonunaTiklama() {

        girisButonu.click();
    }

    /**
     * Kullanıcı adı ve şifreyle giriş işlemini gerçekleştirir.
     *
     * @param kullaniciAdi kullanıcı adı
     * @param sifre kullanıcı şifresi
     */
    public void girisYapma(String kullaniciAdi, String sifre) {

        kullaniciAdiGirme(kullaniciAdi);

        sifreGirme(sifre);

        girisButonunaTiklama();
    }

    /**
     * Login ekranındaki temel alanların görünmesini bekler
     * ve kullanıcı adı, şifre ve giriş butonunu kontrol eder.
     *
     * @return tüm giriş alanları görünüyorsa true
     */
    public boolean girisEkraniGoruntuleniyorMu() {

        try {

            kullaniciAdiAlani.waitFor(
                    new Locator.WaitForOptions()
                            .setState(
                                    com.microsoft.playwright.options.WaitForSelectorState.VISIBLE
                            )
                            .setTimeout(LOGIN_EKRANI_TIMEOUT)
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
     * Başarılı girişin ardından Gösterge Panosu'nun
     * görüntülenmesini bekleyerek giriş sonucunu doğrular.
     *
     * @return Gösterge Panosu görünüyorsa true
     */
    public boolean basariliGirisYapildiMi() {

        try {

            gostergePanosu.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(BASARILI_GIRIS_TIMEOUT)
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
     * Hatalı girişten sonra beklenen hata mesajının
     * görüntülenip görüntülenmediğini kontrol eder.
     *
     * @return hata mesajı görünüyorsa true
     */
    public boolean girisHataMesajiGoruntulendiMi() {

        try {

            girisHataMesaji.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(HATA_MESAJI_TIMEOUT)
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
     * Giriş ekranındaki hata mesajının metnini döndürür.
     *
     * @return görüntülenen hata mesajı
     */
    public String girisHataMesajiAlma() {

        return girisHataMesaji
                .innerText()
                .trim();
    }

    /**
     * AVP oturum hazırlama sırasında login ekranının
     * görünürlüğünü bekleme yapmadan kontrol eder.
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
     * AVP oturum hazırlama sırasında Gösterge Panosu'nun
     * görünürlüğünü bekleme yapmadan kontrol eder.
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
