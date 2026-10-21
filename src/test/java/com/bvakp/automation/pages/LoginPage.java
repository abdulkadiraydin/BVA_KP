package com.bvakp.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;

/**
 * Keycloak giriş ekranı ve giriş sonrası oturum durumu.
 */
public class LoginPage extends BasePage {

    private static final int LOGIN_EKRANI_TIMEOUT_MS = 15_000;
    private static final int BASARILI_GIRIS_TIMEOUT_MS = 20_000;
    private static final int HATA_MESAJI_TIMEOUT_MS = 10_000;
    private static final int OTURUM_DURUMU_TIMEOUT_MS = 30_000;

    /** Portal açıldığında hangi ekranın geldiği. */
    public enum OturumDurumu {
        /** Kayıtlı oturum geçerli, doğrudan Gösterge Panosu açıldı. */
        GOSTERGE_PANOSU,
        /** Oturum yok veya süresi dolmuş, giriş ekranı açıldı. */
        LOGIN_EKRANI,
        /** Süre içinde ikisi de görünmedi. */
        BELIRSIZ
    }

    private final Locator kullaniciAdiAlani;
    private final Locator sifreAlani;
    private final Locator girisButonu;
    private final Locator girisHataMesaji;
    private final Locator gostergePanosu;

    public LoginPage(Page page) {

        super(page);

        this.kullaniciAdiAlani = page.locator("#username");
        this.sifreAlani = page.locator("#password");
        this.girisButonu = page.locator("#kc-login").or(page.locator("form button[type='submit']"));

        this.girisHataMesaji = page.getByText("Kullanıcı adı veya şifre hatalı.",
                new Page.GetByTextOptions().setExact(true));

        this.gostergePanosu = page.getByLabel("breadcrumb").getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Gösterge Panosu").setExact(true));
    }

    // =====================================================================
    // İşlemler
    // =====================================================================

    /** Portal adresini açarak giriş ekranına yönlendirir. */
    public void girisSayfasiAcma() {
        openBaseUrl();
    }

    public void kullaniciAdiGirme(String kullaniciAdi) {
        kullaniciAdiAlani.fill(kullaniciAdi);
    }

    public void sifreGirme(String sifre) {
        sifreAlani.fill(sifre);
    }

    public void girisButonunaTiklama() {
        girisButonu.click();
    }

    /** Kullanıcı adı ve şifreyle giriş yapar. */
    public void girisYapma(String kullaniciAdi, String sifre) {
        kullaniciAdiGirme(kullaniciAdi);
        sifreGirme(sifre);
        girisButonunaTiklama();
    }

    // =====================================================================
    // Durum kontrolleri
    // =====================================================================

    /** Kullanıcı adı, şifre alanları ve giriş butonu görünüyorsa true. */
    public boolean girisEkraniGoruntuleniyorMu() {
        return gorunurMu(kullaniciAdiAlani, LOGIN_EKRANI_TIMEOUT_MS)
                && sifreAlani.isVisible()
                && girisButonu.isVisible();
    }

    /** Girişten sonra Gösterge Panosu görünürse true. */
    public boolean basariliGirisYapildiMi() {
        return gorunurMu(gostergePanosu, BASARILI_GIRIS_TIMEOUT_MS);
    }

    public boolean girisHataMesajiGoruntulendiMi() {
        return gorunurMu(girisHataMesaji, HATA_MESAJI_TIMEOUT_MS);
    }

    public String girisHataMesajiAlma() {
        return girisHataMesaji.innerText().trim();
    }

    /**
     * Portal açıldıktan sonra Gösterge Panosu'nun veya giriş ekranının gelmesini bekler;
     * hangisi önce görünürse beklemeden döner.
     *
     * <p>Kayıtlı oturum geçerliyse pano, değilse giriş ekranı gelir. İkisi birlikte
     * beklendiği için sayfa hızlı açıldığında boşa bekleme olmaz.
     */
    public OturumDurumu oturumDurumunuBekle() {

        try {
            gostergePanosu.or(kullaniciAdiAlani).first().waitFor(
                    new Locator.WaitForOptions().setTimeout(OTURUM_DURUMU_TIMEOUT_MS));
        } catch (PlaywrightException e) {
            return OturumDurumu.BELIRSIZ;
        }

        if (gostergePanosu.isVisible()) {
            return OturumDurumu.GOSTERGE_PANOSU;
        }
        if (kullaniciAdiAlani.isVisible()) {
            return OturumDurumu.LOGIN_EKRANI;
        }
        return OturumDurumu.BELIRSIZ;
    }
}