package com.bvakp.automation.pages.publicPortal;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;

/**
 * Kamuya açık portalda hesap şablonundaki sayfalar: Hesabım (veri talebi formu, Taleplerim, API
 * Anahtarları) ve Yönetim › Veri Talepleri.
 *
 * <p>Hepsi aynı şablonu ve sekme yapısını kullandığı için tek sayfa nesnesindedir. Yönetim
 * bölümünü yalnız sistem yöneticisi (onay rolü) görür.
 */
public class PublicPortalAccountPage extends PublicPortalBasePage {

  private final Locator profilAdi;
  private final Locator sekmeler;

  // Veri talebi formu (modal)
  private final Locator talepOlusturButonu;
  private final Locator talepFormu;
  private final Locator talepBaslikAlani;
  private final Locator talepAmacAlani;
  private final Locator talepKategoriSecimi;
  private final Locator talepHerkeseAcikKutusu;
  private final Locator talepGonderButonu;
  private final Locator talepVazgecButonu;

  // API anahtarları
  private final Locator anahtarAdiAlani;
  private final Locator anahtarOlusturButonu;
  private final Locator olusanAnahtarDegeri;
  private final Locator anahtarSatirlari;
  private final Locator onayPenceresiOnayButonu;

  // Yönetim › Veri Talepleri
  private final Locator yonetimAramaAlani;
  private final Locator yonetimGorunurlukSecimi;
  private final Locator yonetimUygulaButonu;

  public PublicPortalAccountPage(Page page) {
    super(page);
    this.profilAdi = page.locator("h1.bvakp-profile-name");
    this.sekmeler = page.locator("nav.bvakp-account-tabs");

    this.talepOlusturButonu = page.locator(".bvakp-profile-actions a.bvakp-request-open");
    this.talepFormu = page.locator("#bvakp-request-dialog");
    this.talepBaslikAlani = talepFormu.locator("#field-request-title");
    this.talepAmacAlani = talepFormu.locator("#field-request-purpose");
    this.talepKategoriSecimi = talepFormu.locator("#field-request-group");
    this.talepHerkeseAcikKutusu = talepFormu.locator("#field-request-public");
    this.talepGonderButonu = talepFormu.locator("button.bvakp-request-submit");
    this.talepVazgecButonu = talepFormu.locator(".bvakp-request-actions .bvakp-btn-ghost");

    this.anahtarAdiAlani = page.locator("#field-token-name");
    this.anahtarOlusturButonu = page.locator("form.bvakp-token-form button[value='create']");
    // "API anahtarı oluşturuldu: <code>…</code>" bildirimi; değer yalnız bir kez gösterilir.
    this.olusanAnahtarDegeri = page.locator("span:has-text('API anahtarı oluşturuldu') code");
    this.anahtarSatirlari = page.locator("ul.bvakp-token-list > li.bvakp-token");
    this.onayPenceresiOnayButonu = page.locator("#bvakp-confirm .bvakp-confirm-ok");

    Locator yonetimFiltreleri = page.locator("form.bvakp-admin-filters");
    this.yonetimAramaAlani = yonetimFiltreleri.locator("input[name='q']");
    this.yonetimGorunurlukSecimi = yonetimFiltreleri.locator("#field-request-visibility");
    this.yonetimUygulaButonu = yonetimFiltreleri.locator("button[type='submit']");
  }

  // =====================================================================
  // Hesabım ve sekmeler
  // =====================================================================

  /** Kullanıcının Hesabım sayfasını açar. */
  public void hesabimAcma(String kullaniciAdi) {
    adresAcma("/user/" + kullaniciAdi);
  }

  /** Hesabım sayfası verilen kullanıcıyla açıksa true döner. */
  public boolean hesabimAcikMi(String kullaniciAdi) {
    return gorunurMu(profilAdi) && kullaniciAdi.equals(profilAdi.innerText().trim());
  }

  /**
   * Sekmeye geçer. Hesabım: "Genel Bakış", "Taleplerim", "API Anahtarları"; Yönetim: "Sistem
   * Yöneticileri", "Veri Talepleri".
   */
  public void sekmeAcma(String sekmeAdi) {
    sekmeler
        .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(sekmeAdi).setExact(true))
        .click();
  }

  // =====================================================================
  // Veri talebi formu
  // =====================================================================

  /** "Veri Talebi Oluştur" ile talep formunu açar. */
  public void talepFormunuAcma() {
    talepOlusturButonu.click();
  }

  /** Talep formu açıksa true döner. */
  public boolean talepFormuAcikMi() {
    return gorunurMu(talepFormu);
  }

  /** Talep formu kapandıysa true döner. */
  public boolean talepFormuKapandiMi() {
    return gorunmuyorMu(talepFormu);
  }

  /**
   * Talep formunu doldurur.
   *
   * @param amac boşsa (null) girilmez
   * @param kategori seçenekte görünen ad (ör. "Ulaşım"); null ise seçilmez
   * @param herkeseAcik "Talebim herkese açık listelensin" işaretli olsun mu
   */
  public void talepBilgileriniGirme(
      String baslik, String amac, String kategori, boolean herkeseAcik) {
    talepBaslikAlani.fill(baslik);
    if (amac != null) {
      talepAmacAlani.fill(amac);
    }
    if (kategori != null) {
      talepKategoriSecimi.selectOption(new SelectOption().setLabel(kategori));
    }
    talepHerkeseAcikKutusu.setChecked(herkeseAcik);
  }

  /** "Talebi Gönder"e tıklar. */
  public void talebiGonderme() {
    talepGonderButonu.click();
  }

  /** "Vazgeç"e tıklar. */
  public void talepVazgecme() {
    talepVazgecButonu.click();
  }

  /** "Talebiniz alındı…" bildirimi görünüyorsa true döner. */
  public boolean talepAlindiMesajiGorunurMu() {
    return gorunurMu(page.getByText("Talebiniz alındı."));
  }

  /** Başlık alanı boş olduğu için tarayıcı formu göndermiyorsa (zorunlu alan) true döner. */
  public boolean talepBasligiEksikMi() {
    return (Boolean) talepBaslikAlani.evaluate("alan => alan.validity.valueMissing");
  }

  /**
   * Başlığında HTML etiketi bulunan talebin listede düz metin gösterilip gösterilmediği.
   *
   * @param isaret başlıkta geçen ve satırı bulmaya yarayan benzersiz metin
   * @param hamBaslik forma girilen başlık (etiketleriyle)
   * @return başlık etiketleriyle birlikte metin olarak görünüyorsa true; HTML işlendiyse false
   */
  public boolean talepBasligiDuzMetinMi(String isaret, String hamBaslik) {
    Locator satir =
        page.locator("ul.bvakp-my-requests-list > li")
            .filter(new Locator.FilterOptions().setHasText(isaret))
            .first();
    if (!gorunurMu(satir)) {
      return false;
    }
    Locator baslik = satir.locator("h2");
    return baslik.locator("*").count() == 0 && hamBaslik.equals(baslik.innerText().trim());
  }

  // =====================================================================
  // API anahtarları
  // =====================================================================

  /** Verilen adla yeni API anahtarı oluşturur. */
  public void anahtarOlusturma(String anahtarAdi) {
    anahtarAdiAlani.fill(anahtarAdi);
    anahtarOlusturButonu.click();
  }

  /**
   * Oluşturulan anahtarın değeri; bildirim yoksa boş metin. Değer gizlidir, loga yazılmamalıdır.
   */
  public String anahtarDegeriOkuma() {
    return gorunurMu(olusanAnahtarDegeri) ? olusanAnahtarDegeri.innerText().trim() : "";
  }

  /** Anahtar değeri sayfada görünüyorsa true döner (yalnız oluşturulduğu anda görünmeli). */
  public boolean anahtarDegeriGorunuyorMu() {
    return gorunurMu(olusanAnahtarDegeri, 3_000);
  }

  /** Verilen adlı anahtar listede varsa true döner. */
  public boolean anahtarListedeMi(String anahtarAdi) {
    return gorunurMu(anahtarSatiri(anahtarAdi));
  }

  /** Verilen adlı anahtar listede yoksa true döner. */
  public boolean anahtarListedeYokMu(String anahtarAdi) {
    return gorunmuyorMu(anahtarSatiri(anahtarAdi));
  }

  /** Anahtarı "İptal et" ile iptal eder ve onay penceresini onaylar. */
  public void anahtarIptalEtme(String anahtarAdi) {
    anahtarSatiri(anahtarAdi).locator("button.bvakp-token-revoke").click();
    onayPenceresiOnayButonu.click();
  }

  /** Sayfayı yeniler. */
  public void sayfayiYenileme() {
    page.reload();
  }

  // =====================================================================
  // Yönetim › Veri Talepleri (yalnız onay rolü)
  // =====================================================================

  /** Yönetim › Veri Talepleri sayfasını adresle açar. */
  public void yonetimVeriTalepleriniAcma() {
    adresAcma("/ckan-admin/veri-talepleri");
  }

  /**
   * Yönetim'de talepleri başlık/amaçta arar ve görünürlüğe göre süzer.
   *
   * @param gorunurluk "Tüm talepler", "Herkese açık" veya "Gizli"
   */
  public void yonetimdeTalepArama(String aramaMetni, String gorunurluk) {
    yonetimAramaAlani.fill(aramaMetni);
    yonetimGorunurlukSecimi.selectOption(new SelectOption().setLabel(gorunurluk));
    yonetimUygulaButonu.click();
  }

  /** Yönetim listesinde talebi oluşturan kullanıcının adı. */
  public String talepSahibiOkuma(String baslik) {
    return talepSatiri(baslik).locator(".bvakp-admin-requester a").first().innerText().trim();
  }

  /** Talebi onaylar. Onay ekranı belli olunca yazılacak. */
  public void talepOnaylama(String baslik) {
    throw new UnsupportedOperationException("Talep onay ekranı henüz belli değil: " + baslik);
  }

  /** Talebi reddeder. Red ekranı belli olunca yazılacak. */
  public void talepReddetme(String baslik) {
    throw new UnsupportedOperationException("Talep red ekranı henüz belli değil: " + baslik);
  }

  /** Adı birebir eşleşen anahtar satırı. */
  private Locator anahtarSatiri(String anahtarAdi) {
    return anahtarSatirlari.filter(
        new Locator.FilterOptions()
            .setHas(
                page.locator(".bvakp-token-name")
                    .getByText(anahtarAdi, new Locator.GetByTextOptions().setExact(true))));
  }
}
