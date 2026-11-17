package com.bvakp.automation.pages.publicPortal;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Kamuya açık portalda hesap şablonundaki sayfalar: Hesabım (veri talebi formu, Taleplerim, API
 * Anahtarları) ve talep detayı (yönetici işlemleri).
 *
 * <p>Hepsi aynı şablonu kullandığı için tek sayfa nesnesindedir. Talep detayındaki Onayla / Reddet
 * / Karşılandı işlemlerini yalnız sistem yöneticisi (onay rolü) görür.
 */
public class PublicPortalAccountPage extends PublicPortalBasePage {

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

  // Talep detayı (yönetici işlemleri)
  private final Locator talepDetayDurumu;
  private final Locator yoneticiIslemleri;
  private final Locator karsilayanVeriSetiAlani;
  private final Locator veriSetiSecenekleri;

  public PublicPortalAccountPage(Page page) {
    super(page);
    this.sekmeler =
        page.getByRole(
            AriaRole.NAVIGATION,
            new Page.GetByRoleOptions().setName("Hesap bölümleri").setExact(true));

    // Ana sayfa ve Veri Talepleri'nde "Veri Talebi Oluştur", Hesabım'da "Veri talebi oluşturun"
    // kartı; ikisi de aynı formu açar. Playwright regex'te yalnız CASE_INSENSITIVE, DOTALL ve
    // MULTILINE bayraklarını kabul eder.
    this.talepOlusturButonu =
        page.getByRole(
                AriaRole.LINK,
                new Page.GetByRoleOptions()
                    .setName(Pattern.compile("^Veri talebi oluştur", Pattern.CASE_INSENSITIVE)))
            .first();
    this.talepFormu =
        page.getByRole(
            AriaRole.DIALOG,
            new Page.GetByRoleOptions().setName("Veri Talebi Oluştur").setExact(true));
    this.talepBaslikAlani = talepFormu.getByLabel("Hangi veriye ihtiyacınız var?");
    this.talepAmacAlani = talepFormu.getByLabel("Ne için kullanacaksınız?");
    this.talepKategoriSecimi =
        talepFormu.getByLabel("Kategori", new Locator.GetByLabelOptions().setExact(true));
    this.talepHerkeseAcikKutusu = talepFormu.getByLabel("Talebim herkese açık listelensin");
    this.talepGonderButonu = buton(talepFormu, "Talebi Gönder");
    this.talepVazgecButonu = buton(talepFormu, "Vazgeç");

    this.anahtarAdiAlani =
        page.getByLabel("Anahtar adı", new Page.GetByLabelOptions().setExact(true));
    this.anahtarOlusturButonu =
        page.getByRole(
            AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Anahtar oluştur").setExact(true));
    // "API anahtarı oluşturuldu: <code>…</code>" bildirimi; değer yalnız bir kez gösterilir.
    this.olusanAnahtarDegeri = page.getByText("API anahtarı oluşturuldu").locator("code");
    this.anahtarSatirlari = bolum("Anahtarlarınız").getByRole(AriaRole.LISTITEM);
    // Onay penceresindeki onay düğmesi (adı işleme göre değişir: Onayla, Reddet, Karşılandı...).
    // Sayfada birden fazla onay penceresi olabileceği için yalnız görünen alınır.
    this.onayPenceresiOnayButonu = page.locator("[data-bvakp-confirm-ok]:visible");

    // Talep detayının başlığındaki durum etiketi (ör. "Onay bekliyor", "Kapanmış · Reddedildi").
    this.talepDetayDurumu = page.getByRole(AriaRole.MAIN).locator("header .bvakp-history-kind");
    // Onayla / Reddet / Karşılandı düğmeleri bu bölgededir. Onay penceresinde de "Onayla" düğmesi
    // olduğu için düğmeler bölgeyle daraltılır.
    this.yoneticiIslemleri =
        page.getByRole(
            AriaRole.REGION,
            new Page.GetByRoleOptions().setName("Yönetici işlemleri").setExact(true));
    this.karsilayanVeriSetiAlani =
        page.getByRole(
            AriaRole.COMBOBOX,
            new Page.GetByRoleOptions().setName("Talebi karşılayan veri seti").setExact(true));
    // Kutuya tıklanınca açılan listede her veri seti bir düğmedir.
    this.veriSetiSecenekleri =
        page.getByRole(AriaRole.LIST, new Page.GetByRoleOptions().setName("Veri setleri"))
            .getByRole(AriaRole.BUTTON);
  }

  // =====================================================================
  // Hesabım ve sekmeler
  // =====================================================================

  /** Kullanıcının Hesabım sayfasını açar. */
  public void hesabimAcma(String kullaniciAdi) {
    adresAcma("/user/" + kullaniciAdi);
  }

  /**
   * Hesabım sayfası verilen kullanıcıyla açıksa true döner. Başlıkta görünen ad kullanıcı adından
   * farklı olabileceği için adrese ve hesap sekmelerine bakılır.
   */
  public boolean hesabimAcikMi(String kullaniciAdi) {
    return gorunurMu(sekmeler) && page.url().endsWith("/user/" + kullaniciAdi);
  }

  /** Hesabım'da sekmeye geçer: "Genel Bakış", "Taleplerim", "API Anahtarları". */
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
    sayfaHazirOlanaKadarBekleme();
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
   * @param kategori seçenekte görünen ad (ör. "Altyapı ve İstasyonlar"); null ise seçilmez
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

  /** Robot doğrulamasını tamamlayıp "Talebi Gönder"e tıklar. */
  public void talebiGonderme() {
    robotDogrulamasiniTamamlama(talepFormu);
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
   * Başlığında {@code isaret} geçen talebin listedeki başlığının HTML'i. Başlık link olarak
   * gösterildiği için {@code <a>} içerir; HTML'in işlenip işlenmediği bu metinden anlaşılır.
   */
  public String talepBasligiHtmlOkuma(String isaret) {
    Locator satir =
        page.getByRole(AriaRole.LISTITEM)
            .filter(new Locator.FilterOptions().setHasText(isaret))
            .first();
    return satir
        .getByRole(AriaRole.HEADING, new Locator.GetByRoleOptions().setLevel(2))
        .innerHTML();
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
    sayfaHazirOlanaKadarBekleme();
    buton(anahtarSatiri(anahtarAdi), "İptal et").click();
    onayPenceresiOnayButonu.click();
  }

  /** Sayfayı yeniler. */
  public void sayfayiYenileme() {
    page.reload();
  }

  // =====================================================================
  // Talep detayı (Onayla / Reddet / Karşılandı yalnız onay rolünde görünür)
  // =====================================================================

  /** "Onay bekliyor" sekmesinde başlıkla arar ve talebin detayını açar. */
  public void onayBekleyenTalebiAcma(String baslik) {
    adresAcma("/datarequest?status=pending&q=" + URLEncoder.encode(baslik, StandardCharsets.UTF_8));
    link(baslik).click();
  }

  /** Açık olan talep detayındaki durum etiketi (ör. "Açık", "Kapanmış · Karşılandı"). */
  public String talepDetayDurumuOkuma() {
    return talepDetayDurumu.innerText().trim();
  }

  /** Açık olan talep detayında Onayla'ya tıklar; onay penceresi açılmadan talep onaylanır. */
  public void talebiOnaylama() {
    sayfaHazirOlanaKadarBekleme();
    buton(yoneticiIslemleri, "Onayla").click();
  }

  /** Açık olan talep detayında Reddet'e tıklar ve onay penceresini onaylar. */
  public void talebiReddetme() {
    sayfaHazirOlanaKadarBekleme();
    buton(yoneticiIslemleri, "Reddet").click();
    onayPenceresiOnayButonu.click();
  }

  /**
   * Talebi "Karşılandı" olarak kapatır. Karşılayan veri seti olarak listedeki ilk veri setini seçer
   * ve adını döndürür.
   */
  public String talebiKarsilandiOlarakKapatma() {
    // Yorum eklendikten sonra sayfa yenilenir; "Karşılandı" da sayfayı yeniden yükler.
    sayfaHazirOlanaKadarBekleme();
    page.locator("[data-bvakp-fulfil-open]").click();
    sayfaHazirOlanaKadarBekleme();

    // Radyo butonu özel tasarımlı olabilir; görünür olan etikete tıklanır.
    Locator ilkVeriSeti = page.locator("[data-bvakp-dataset-list] label").first();
    String veriSetiAdi = ilkVeriSeti.locator("input[type=radio]").getAttribute("data-title");
    ilkVeriSeti.click();

    page.locator("[data-bvakp-fulfil-submit]").click();
    onayPenceresiOnayButonu.click();
    return veriSetiAdi;
  }

  /** Ek Bilgiler'deki "Kabul edilen veri seti" değeri (yoksa "Hiçbiri"). */
  public String kabulEdilenVeriSetiOkuma() {
    return bolum("Ek Bilgiler")
        .getByRole(AriaRole.ROW)
        .filter(
            new Locator.FilterOptions()
                .setHas(
                    page.getByRole(
                        AriaRole.ROWHEADER,
                        new Page.GetByRoleOptions()
                            .setName("Kabul edilen veri seti")
                            .setExact(true))))
        .getByRole(AriaRole.CELL)
        .innerText()
        .trim();
  }
  /** Talep detayına yorum yazar ve "Yorum ekle"ye tıklar. */
  public void yorumEkleme(String yorum) {
    page.locator("#field-request-comment").fill(yorum);
    page.locator("form:has(#field-request-comment) [data-bvakp-counter-submit]").click();
  }

  /** Verilen yorum talep detayında görünüyorsa true döner. */
  public boolean yorumGorunurMu(String yorum) {
    return gorunurMu(page.getByText(yorum).first());
  }

  /** "Talep karşılandı olarak kapatıldı." bildirimi görünüyorsa true döner. */
  public boolean karsilandiMesajiGorunurMu() {
    return gorunurMu(page.getByText("Talep karşılandı olarak kapatıldı."));
  }

  /** "Talep onaylandı ve yayınlandı." bildirimi görünüyorsa true döner. */
  public boolean onaylandiMesajiGorunurMu() {
    return gorunurMu(page.getByText("Talep onaylandı ve yayınlandı."));
  }
  /** "Talep reddedildi." bildirimi görünüyorsa true döner. */
  public boolean reddedildiMesajiGorunurMu() {
    return gorunurMu(page.getByText("Talep reddedildi."));
  }

  /** Talep detayından "Veri Taleplerine dön" ile listeye döner. */
  public void veriTaleplerineDonme() {
    page.locator(".bvakp-dr-back-label").click();
  }

  /** Veri Talepleri'nde durum sekmesine geçer: "Açık", "Kapanmış", "Tümü", "Onay bekliyor". */
  public void talepDurumSekmesiAcma(String sekmeAdi) {
    page.getByRole(
                    AriaRole.NAVIGATION,
                    new Page.GetByRoleOptions().setName("Talep durumu").setExact(true))
            .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(sekmeAdi).setExact(true))
            .click();
  }

  /** Veri Talepleri listesinde başlığı verilen talebin kartındaki durum etiketi. */
  public String talepKartiDurumuOkuma(String baslik) {
    return page.locator(".bvakp-dr-card")
            .filter(new Locator.FilterOptions().setHas(link(baslik)))
            .locator(".bvakp-history-kind")
            .innerText()
            .trim();
  }

  /** Talep detayında yönetici işlemleri (Reddet) görünmüyorsa true döner. */
  public boolean talepYonetimIslemleriGizliMi() {
    return gorunmuyorMu(buton(yoneticiIslemleri, "Reddet"));
  }

  // =====================================================================
  // Yardımcılar
  // =====================================================================

  /** "Anahtarlarınız" listesinde adı birebir eşleşen anahtar satırı. */
  private Locator anahtarSatiri(String anahtarAdi) {
    return anahtarSatirlari.filter(
        new Locator.FilterOptions()
            .setHas(page.getByText(anahtarAdi, new Page.GetByTextOptions().setExact(true))));
  }

  /** Görünen adı birebir eşleşen link. */
  private Locator link(String ad) {
    return page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(ad).setExact(true));
  }

  /** Verilen bölgede görünen adı birebir eşleşen düğme. */
  private static Locator buton(Locator bolge, String ad) {
    return bolge.getByRole(
        AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(ad).setExact(true));
  }
}
