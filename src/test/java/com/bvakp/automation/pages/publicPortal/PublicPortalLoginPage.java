package com.bvakp.automation.pages.publicPortal;

import com.bvakp.automation.core.auth.PortalRol;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Kamuya açık portalın giriş ekranı (/user/login) ve sağ üstteki hesap menüsü (Hesabım, Yönetim,
 * Çıkış Yap).
 *
 * <p>Testler {@link #rolIleGirisYapma} ile rolün kullanıcısıyla girer; rol değiştirmek için aynı
 * metot yeniden çağrılır.
 */
public class PublicPortalLoginPage extends PublicPortalBasePage {

  private final Locator girisLinki;
  private final Locator kullaniciAdiAlani;
  private final Locator sifreAlani;
  private final Locator girisButonu;
  private final Locator hataMesaji;
  private final Locator hesapMenusu;
  private final Locator hesapAdi;
  private final Locator yonetimLinki;
  private final Locator cikisButonu;
  private final Locator talepOlusturLinki;

  public PublicPortalLoginPage(Page page) {
    super(page);
    this.girisLinki = page.locator("a.bvakp-topbar-login");
    this.kullaniciAdiAlani = page.locator("#field-login");
    this.sifreAlani = page.locator("#field-password");
    this.girisButonu = page.locator("form.bvakp-login-form button[type='submit']");
    this.hataMesaji = page.locator(".bvakp-auth-alert.is-error");
    this.hesapMenusu = page.locator("#bvakpAccountToggle");
    this.hesapAdi = hesapMenusu.locator(".bvakp-account-label");
    this.yonetimLinki = page.locator(".bvakp-account-menu a[href$='/ckan-admin/']");
    this.cikisButonu = page.locator(".bvakp-account-menu form[action$='/user/_logout'] button");
    // Ana sayfanın altındaki "Aradığınız veriyi bulamadınız mı?" alanı.
    this.talepOlusturLinki = page.locator("section.bvakp-cta a");
  }

  // =====================================================================
  // İşlemler
  // =====================================================================

  /** Portalın ana sayfasını açar. */
  public void anaSayfaAcma() {
    adresAcma("/");
  }

  /** Sağ üstteki "Giriş Yap" linkine tıklar. */
  public void girisEkraniniAcma() {
    girisLinki.click();
  }

  /** Kullanıcı adı ve şifreyi girip Giriş Yap'a tıklar. */
  public void girisYapma(String kullaniciAdi, String sifre) {
    kullaniciAdiAlani.fill(kullaniciAdi);
    sifreAlani.fill(sifre);
    girisButonu.click();
  }

  /** Oturumu kapatıp (çerezleri silerek) verilen rolün kullanıcısıyla giriş yapar. */
  public void rolIleGirisYapma(PortalRol rol) {
    page.context().clearCookies();
    anaSayfaAcma();
    girisEkraniniAcma();
    girisYapma(rol.kullaniciAdi(), rol.parola());
  }

  /** Hesap menüsünden Yönetim'e gider (yalnız sistem yöneticisinde görünür). */
  public void yonetimeGitme() {
    hesapMenusu.click();
    yonetimLinki.click();
  }

  /** Hesap menüsünü açıp Çıkış Yap'a tıklar. */
  public void cikisYapma() {
    hesapMenusu.click();
    cikisButonu.click();
  }

  /** Ana sayfadaki "Veri Talebi Oluştur" linkine tıklar (giriş yapılmamışken). */
  public void anaSayfadanTalepOlusturma() {
    talepOlusturLinki.click();
  }

  // =====================================================================
  // Durum kontrolleri
  // =====================================================================

  /** Giriş ekranı (kullanıcı adı ve şifre alanları) açıksa true döner. */
  public boolean girisEkraniAcikMi() {
    return gorunurMu(kullaniciAdiAlani) && sifreAlani.isVisible();
  }

  /** Sağ üstte verilen kullanıcının hesap menüsü görünüyorsa true döner. */
  public boolean girisYapildiMi(String kullaniciAdi) {
    return gorunurMu(hesapMenusu) && kullaniciAdi.equals(hesapAdi.innerText().trim());
  }

  /** Hesap menüsü kalkıp "Giriş Yap" linki göründüyse true döner. */
  public boolean cikisYapildiMi() {
    return gorunurMu(girisLinki) && !hesapMenusu.isVisible();
  }

  /** Hesap menüsünde "Yönetim" bağlantısı varsa (sistem yöneticisi) true döner. */
  public boolean yonetimMenusuVarMi() {
    hesapMenusu.click();
    boolean var = gorunurMu(yonetimLinki, 3_000);
    hesapMenusu.click();
    return var;
  }

  /** Giriş hatası mesajının metni; mesaj yoksa boş metin. */
  public String girisHataMesajiAlma() {
    return gorunurMu(hataMesaji) ? hataMesaji.innerText().trim() : "";
  }

  /** Şu anki adres. */
  public String adresAlma() {
    return page.url();
  }
}
