package com.bvakp.automation.pages.publicPortal;

import com.bvakp.automation.core.auth.PublicPortalRol;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.regex.Pattern;

/**
 * Kamuya açık portalın giriş ekranı (/user/login) ve sağ üstteki hesap menüsü (Hesabım, Çıkış Yap).
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
  private final Locator hesabimLinki;
  private final Locator cikisButonu;
  private final Locator talepOlusturLinki;

  public PublicPortalLoginPage(Page page) {
    super(page);
    this.girisLinki = link("Giriş Yap");
    this.kullaniciAdiAlani =
        page.getByLabel("E-posta veya kullanıcı adı", new Page.GetByLabelOptions().setExact(true));
    this.sifreAlani = page.getByLabel("Şifre", new Page.GetByLabelOptions().setExact(true));
    this.girisButonu = buton("Giriş Yap");
    this.hataMesaji = page.getByRole(AriaRole.ALERT);
    // Düğmenin adı "<görünen ad>, Hesap menüsü"; görünen ad kullanıcı adından farklı olabilir.
    this.hesapMenusu =
        page.getByRole(
            AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("Hesap menüsü")));
    // Menü kapalıyken gizlidir; yalnız adresi (/user/<kullanıcı adı>) okunur.
    this.hesabimLinki =
        page.getByRole(
                AriaRole.LINK,
                new Page.GetByRoleOptions()
                    .setName("Hesabım")
                    .setExact(true)
                    .setIncludeHidden(true))
            .first();
    this.cikisButonu = buton("Çıkış Yap");
    // Ana sayfanın altındaki "Aradığınız veriyi bulamadınız mı?" alanı.
    this.talepOlusturLinki = link("Veri Talebi Oluştur");
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
  public void rolIleGirisYapma(PublicPortalRol rol) {
    page.context().clearCookies();
    anaSayfaAcma();
    girisEkraniniAcma();
    girisYapma(rol.kullaniciAdi(), rol.parola());
  }

  /** Hesap menüsünü açıp Çıkış Yap'a tıklar. */
  public void cikisYapma() {
    sayfaHazirOlanaKadarBekleme();
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

  /**
   * Hesap menüsü görünüyor ve "Hesabım" linki verilen kullanıcının sayfasına gidiyorsa giriş
   * yapılmıştır. Ekranda görünen ad kullanıcı adından farklı olabileceği için adrese bakılır.
   */
  public boolean girisYapildiMi(String kullaniciAdi) {
    return gorunurMu(hesapMenusu)
        && ("/user/" + kullaniciAdi).equals(hesabimLinki.getAttribute("href"));
  }

  /** Hesap menüsü kalkıp "Giriş Yap" linki göründüyse true döner. */
  public boolean cikisYapildiMi() {
    return gorunurMu(girisLinki) && !hesapMenusu.isVisible();
  }

  /** Giriş hatası mesajının metni; mesaj yoksa boş metin. */
  public String girisHataMesajiAlma() {
    return gorunurMu(hataMesaji) ? hataMesaji.innerText().trim() : "";
  }

  /** Şu anki adres. */
  public String adresAlma() {
    return page.url();
  }

  // =====================================================================
  // Yardımcılar
  // =====================================================================

  /** Görünen adı birebir eşleşen link. */
  private Locator link(String ad) {
    return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(ad).setExact(true));
  }

  /** Görünen adı birebir eşleşen düğme. */
  private Locator buton(String ad) {
    return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(ad).setExact(true));
  }
}
