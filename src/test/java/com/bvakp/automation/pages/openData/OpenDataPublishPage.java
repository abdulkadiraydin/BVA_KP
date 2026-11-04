package com.bvakp.automation.pages.openData;

import com.bvakp.automation.model.YayinAyarlari;
import com.bvakp.automation.pages.BasePage;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Açık Veri Portalı "Veri Seti Yönetimi" sekmesi: veri seti listesi, satır işlemleri (KVKK
 * onay/red, yayınla, arşivle, sil, güncelle, düzenle) ve onay popup'ları.
 *
 * <p>Statü, portal aktarımı ve versiyon gibi asenkron değişiklikler, liste belirli aralıklarla
 * yenilenerek beklenir (bkz. {@link #listedeBekle}). Bekleme metotları true/false döner; doğrulama
 * testte yapılır.
 */
public class OpenDataPublishPage extends BasePage {

  private static final int SATIR_TIMEOUT_MS = 10_000;
  private static final int ARAMA_SONUCU_TIMEOUT_MS = 10_000;

  private static final int STATU_TIMEOUT_MS = 120_000;
  private static final int STATU_YENILEME_ARALIGI_MS = 3_000;
  private static final int PORTAL_AKTARIM_TIMEOUT_MS = 180_000;
  private static final int PORTAL_YENILEME_ARALIGI_MS = 5_000;
  private static final int VERSIYON_TIMEOUT_MS = 120_000;
  private static final int VERSIYON_YENILEME_ARALIGI_MS = 3_000;
  private static final int SILINME_TIMEOUT_MS = 60_000;
  private static final int SILINME_YENILEME_ARALIGI_MS = 3_000;

  private static final String KAYIT_BULUNAMADI = "Kayıt bulunamadı";
  private static final String DATA_API_ETIKETI = "Data API";

  private final Locator publishTab;
  private final Locator aramaAlani;
  private final Locator kayitBulunamadiMesaji;

  /** Satır işlemlerinden sonra açılan onay popup'ı. */
  private final Locator popup;

  private final Locator popupAciklamaInput;

  public OpenDataPublishPage(Page page) {

    super(page);

    this.publishTab =
        page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Veri Seti Yönetimi"));
    this.aramaAlani = page.getByPlaceholder("Veri seti ara");
    this.kayitBulunamadiMesaji = page.getByText(KAYIT_BULUNAMADI);

    this.popup = page.getByRole(AriaRole.DIALOG);
    this.popupAciklamaInput = popup.locator("textarea[name='text']");
  }

  // =====================================================================
  // Liste ve arama
  // =====================================================================

  /** Veri Seti Yönetimi sekmesine geçer. */
  public void clickPublishTab() {
    publishTab.click();
  }

  /** Veri seti adının tam eşleştiği satır. */
  public Locator getDataSetRow(String dataSetName) {
    return page.locator("[role='row']")
        .filter(
            new Locator.FilterOptions()
                .setHas(
                    page.locator("[role='gridcell'][data-field='name']")
                        .getByText(dataSetName, new Locator.GetByTextOptions().setExact(true))));
  }

  /**
   * Arama alanına veri seti adını yazar ve sonucun (eşleşen satır veya "Kayıt bulunamadı")
   * gelmesini bekler.
   */
  public void searchDataSet(String dataSetName) {

    aramaAlani.fill(dataSetName);

    try {
      getDataSetRow(dataSetName)
          .or(kayitBulunamadiMesaji)
          .first()
          .waitFor(new Locator.WaitForOptions().setTimeout(ARAMA_SONUCU_TIMEOUT_MS));
    } catch (PlaywrightException e) {
      // Sonuç gelmediyse çağıran metot kendi kontrolünde anlamlı hatayı verir.
    }
  }

  /** Arama alanı veri seti adını içeriyor ve tabloda tam eşleşen satır varsa true. */
  public boolean isDataSetNameMatching(String dataSetName) {

    String aramaDegeri = aramaAlani.inputValue().trim();

    if (!dataSetName.equals(aramaDegeri)) {
      ReportManager.warn(
          "Arama alanı eşleşmiyor. Beklenen: " + dataSetName + " | Gerçek: " + aramaDegeri);
      return false;
    }

    return gorunurMu(getDataSetRow(dataSetName), SATIR_TIMEOUT_MS);
  }

  // =====================================================================
  // Satır işlemleri
  // =====================================================================

  /** Veri setinin satırındaki Görüntüle butonuna tıklar. */
  public void clickGoruntule(String dataSetName) {
    satirButonunaTikla(dataSetName, "Görüntüle");
  }

  /** Veri setinin satırındaki KVKK Onayla butonuna tıklar. */
  public void clickKvkkOnayla(String dataSetName) {
    satirButonunaTikla(dataSetName, "KVKK Onayla");
  }

  /** Veri setinin satırındaki KVKK Reddet butonuna tıklar. */
  public void clickKvkkReddet(String dataSetName) {
    satirButonunaTikla(dataSetName, "KVKK Reddet");
  }

  /** Veri setinin satırındaki Yayınla butonuna tıklar. */
  public void clickYayinla(String dataSetName) {
    satirButonunaTikla(dataSetName, "Yayınla");
  }

  /** Veri setinin satırındaki Yayını Reddet butonuna tıklar. */
  public void clickYayinReddet(String dataSetName) {
    satirButonunaTikla(dataSetName, "Yayını Reddet");
  }

  /** Veri setinin satırındaki Arşivle butonuna tıklar. */
  public void clickArsivle(String dataSetName) {
    satirButonunaTikla(dataSetName, "Arşivle");
  }

  /** Veri setinin satırındaki Sil butonuna tıklar. */
  public void clickSil(String dataSetName) {
    satirButonunaTikla(dataSetName, "Sil");
  }

  /** Veri setinin satırındaki Güncelle butonuna tıklar. */
  public void clickGuncelle(String dataSetName) {
    satirButonunaTikla(dataSetName, "Güncelle");
  }

  /** Veri setinin satırındaki Düzenle butonuna tıklar. */
  public void clickDuzenle(String dataSetName) {
    satirButonunaTikla(dataSetName, "Düzenle");
  }

  /** Veri setini arar, satırın görünmesini bekler ve satırdaki butona tıklar. */
  private void satirButonunaTikla(String dataSetName, String butonAdi) {

    searchDataSet(dataSetName);

    Locator satir = getDataSetRow(dataSetName);
    if (!gorunurMu(satir, SATIR_TIMEOUT_MS)) {
      throw new IllegalStateException(
          "Veri seti listede bulunamadı: "
              + dataSetName
              + " ('"
              + butonAdi
              + "' işlemi yapılamadı)");
    }

    satir.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(butonAdi)).click();
  }

  // =====================================================================
  // Onay popup'ları
  // =====================================================================

  /** KVKK onay penceresindeki açıklamayı doldurur. */
  public void enterKvkkAciklama(String aciklama) {
    popupAciklamaInput.fill(aciklama);
  }

  /** KVKK red penceresindeki açıklamayı doldurur. */
  public void enterKvkkRedAciklama(String aciklama) {
    popupAciklamaInput.fill(aciklama);
  }

  /** Yayınla penceresindeki açıklamayı doldurur. */
  public void enterYayinAciklama(String aciklama) {
    popupAciklamaInput.fill(aciklama);
  }

  /** Yayın red penceresindeki açıklamayı doldurur. */
  public void enterYayinRedAciklama(String aciklama) {
    popupAciklamaInput.fill(aciklama);
  }

  /** Arşivle penceresindeki açıklamayı doldurur. */
  public void enterArsivAciklama(String aciklama) {
    popupAciklamaInput.fill(aciklama);
  }

  /** KVKK onay penceresinde Onayla'ya tıklar. */
  public void confirmKvkkOnay() {
    popupButonunaTikla("Onayla");
  }

  /** KVKK red penceresinde Reddet'e tıklar. */
  public void confirmKvkkRed() {
    popupButonunaTikla("Reddet");
  }

  /** Yayınla penceresinde Yayınla'ya tıklar. */
  public void confirmYayinla() {
    popupButonunaTikla("Yayınla");
  }

  /** Yayın red penceresinde Reddet'e tıklar. */
  public void confirmYayinRed() {
    popupButonunaTikla("Reddet");
  }

  /** Arşivle penceresinde Arşivle'ye tıklar. */
  public void confirmArsivle() {
    popupButonunaTikla("Arşivle");
  }

  /** Sil penceresinde Sil'e tıklar. */
  public void confirmSil() {
    popupButonunaTikla("Sil");
  }

  /** Güncelle penceresinde Güncelle'ye tıklar. */
  public void confirmGuncelle() {
    popupButonunaTikla("Güncelle");
  }

  /** Düzenleme sonrası çıkan pencerede Onaya Gönder'e tıklar. */
  public void confirmDegisiklikOnayaGonder() {
    popupButonunaTikla("Onaya Gönder");
  }

  /** KVKK onay penceresini İptal ile kapatır. */
  public void cancelKvkkOnay() {
    popupButonunaTikla("İptal");
  }

  /** KVKK red penceresini İptal ile kapatır. */
  public void cancelKvkkRed() {
    popupButonunaTikla("İptal");
  }

  /** Arşivle penceresini İptal ile kapatır. */
  public void cancelArsivle() {
    popupButonunaTikla("İptal");
  }

  /** Sil penceresini İptal ile kapatır. */
  public void cancelSil() {
    popupButonunaTikla("İptal");
  }

  /** Popup içindeki butona tıklar; arka plandaki aynı adlı satır butonlarıyla karışmaz. */
  private void popupButonunaTikla(String butonAdi) {
    popup
        .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(butonAdi).setExact(true))
        .click();
  }

  // =====================================================================
  // Düzenleme ekranı
  // =====================================================================

  /** Adı verilen açılır listeden seçeneği seçer. */
  public void selectDropdown(String dropdownName, String option) {
    page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName(dropdownName)).click();
    page.locator("li[role='option']")
        .filter(new Locator.FilterOptions().setHasText(option))
        .click();
  }

  /** Düzenleme ekranındaki açıklamayı doldurur. */
  public void enterDuzenlemeAciklama(String aciklama) {
    page.locator("textarea[name='description']").fill(aciklama);
  }

  /** Düzenleme ekranında Kaydet'e tıklar. */
  public void clickDuzenlemeKaydet() {
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Kaydet")).click();
  }

  /** Düzenleme ekranındaki "Onaya Gönder" butonu (onay popup'ını açar). */
  public void clickDegisiklikOnayaGonder() {
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Onaya Gönder"))
        .first()
        .click();
  }

  /** Düzenleme ekranında görünen sorumlu birim, kategori ve açıklama. */
  public record DuzenlemeAlanlari(String sorumluBirim, String kategori, String aciklama) {}

  /** Düzenleme ekranındaki sorumlu birim, kategori ve açıklamayı okur. */
  public DuzenlemeAlanlari duzenlemeAlanlariniOkuma() {
    return new DuzenlemeAlanlari(
        page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Sorumlu Birim"))
            .innerText(),
        page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Kategori"))
            .innerText(),
        page.locator("textarea[name='description']").inputValue());
  }

  /** Detay ekranında Geri Dön'e tıklar. */
  public void clickGeriDon() {
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Geri Dön")).click();
  }

  // =====================================================================
  // Ekran kontrolleri
  // =====================================================================

  /** Veri seti detay ekranı ("Genel Bilgi" sekmesi ve veri seti adı) açıldıysa true döner. */
  public boolean detayEkraniAcikMi(String dataSetName) {
    Locator genelBilgi =
        page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Genel Bilgi"));
    Locator veriSetiAdi = page.getByText(dataSetName, new Page.GetByTextOptions().setExact(true));
    return gorunurMu(genelBilgi) && gorunurMu(veriSetiAdi);
  }

  /** Detay ekranında etiketi verilen alan (ör. "Kategori") görünüyorsa true döner. */
  public boolean detayAlaniGorunurMu(String etiket) {
    return gorunurMu(
        page.locator("span")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^" + etiket + "$")))
            .first());
  }

  /** Veri Seti Yönetimi başlığı görünüyorsa true döner. */
  public boolean veriSetiYonetimiAcikMi() {
    return gorunurMu(
        page.getByRole(
            AriaRole.HEADING, new Page.GetByRoleOptions().setName("Veri Seti Yönetimi")));
  }

  // =====================================================================
  // Asenkron değişiklikleri bekleme (liste yenilenerek)
  // =====================================================================

  /** Veri setinin statüsü beklenen değere ulaşırsa true; süre dolarsa false döner. */
  public boolean durumaGectiMi(String dataSetName, String beklenenDurum) {
    return listedeBekle(
        dataSetName,
        "state",
        Locator::innerText,
        beklenenDurum::equals,
        "Statü: " + beklenenDurum,
        STATU_TIMEOUT_MS,
        STATU_YENILEME_ARALIGI_MS);
  }

  /** Veri setinin portal aktarım durumu beklenen değere ulaşırsa true döner (ör. "Aktarıldı"). */
  public boolean portalDurumunaGectiMi(String dataSetName, String beklenenDurum) {
    return listedeBekle(
        dataSetName,
        "ckanSyncStatus",
        Locator::innerText,
        beklenenDurum::equals,
        "Portal aktarımı: " + beklenenDurum,
        PORTAL_AKTARIM_TIMEOUT_MS,
        PORTAL_YENILEME_ARALIGI_MS);
  }

  /** Veri setinin versiyonu beklenen numaraya ulaşırsa true döner. */
  public boolean versiyonaGectiMi(String dataSetName, int beklenenVersiyon) {
    return listedeBekle(
        dataSetName,
        "currentVersionNo",
        hucre -> hucre.getAttribute("title"),
        versiyon ->
            versiyon != null
                && String.valueOf(beklenenVersiyon).equals(versiyon.replace("v", "").trim()),
        "Versiyon: v" + beklenenVersiyon,
        VERSIYON_TIMEOUT_MS,
        VERSIYON_YENILEME_ARALIGI_MS);
  }

  /** Veri setini arar ve listedeki güncel versiyon numarasını döndürür. */
  public String getCurrentVersion(String dataSetName) {

    searchDataSet(dataSetName);

    Locator versiyonHucresi =
        getDataSetRow(dataSetName).locator("[role='gridcell'][data-field='currentVersionNo']");
    String versiyon = versiyonHucresi.getAttribute("title");

    ReportManager.info("Veri seti versiyonu okundu: " + versiyon);
    return versiyon;
  }

  /** Silinen veri seti listeden kalkarsa ("Kayıt bulunamadı") true; süre dolarsa false döner. */
  public boolean listedenKalktiMi(String dataSetName) {

    ReportManager.info("Veri setinin listeden kalkması bekleniyor: " + dataSetName);

    long bitis = System.currentTimeMillis() + SILINME_TIMEOUT_MS;

    while (System.currentTimeMillis() < bitis) {
      listeyiYenileVeAra(dataSetName);

      if (gorunurMu(kayitBulunamadiMesaji, 3_000)) {
        ReportManager.info("Veri seti listeden kalktı: " + dataSetName);
        return true;
      }

      page.waitForTimeout(SILINME_YENILEME_ARALIGI_MS);
    }

    ReportManager.warn(
        "Veri seti " + SILINME_TIMEOUT_MS / 1000 + " sn içinde listeden kalkmadı: " + dataSetName);
    return false;
  }

  /**
   * Listeyi belirli aralıklarla yenileyip veri setinin bir sütununu okur; değer koşulu sağlayınca
   * true döner. Süre dolarsa son görülen değeri loga yazar ve false döner.
   *
   * <p>Aralık bilinçlidir: bekleme olmadan yenilemek sunucuyu gereksiz yere yorar ve paralel koşuda
   * diğer testleri yavaşlatır.
   */
  private boolean listedeBekle(
      String dataSetName,
      String sutun,
      Function<Locator, String> degerOku,
      Predicate<String> kosul,
      String beklenenAciklama,
      int timeoutMs,
      int yenilemeAraligiMs) {

    ReportManager.info("Bekleniyor | " + dataSetName + " | " + beklenenAciklama);

    long bitis = System.currentTimeMillis() + timeoutMs;
    String sonDeger = "veri seti listede bulunamadı";

    while (System.currentTimeMillis() < bitis) {

      listeyiYenileVeAra(dataSetName);

      Locator hucre =
          getDataSetRow(dataSetName).locator("[role='gridcell'][data-field='" + sutun + "']");

      if (hucre.count() > 0) {
        String deger = degerOku.apply(hucre.first());
        sonDeger = deger == null ? "boş" : deger.trim();

        if (kosul.test(sonDeger)) {
          ReportManager.info("Doğrulandı | " + dataSetName + " | " + beklenenAciklama);
          return true;
        }
      }

      page.waitForTimeout(yenilemeAraligiMs);
    }

    ReportManager.warn(
        "Beklenen değere "
            + timeoutMs / 1000
            + " sn içinde ulaşılamadı | "
            + dataSetName
            + " | Beklenen: "
            + beklenenAciklama
            + " | Son görülen: "
            + sonDeger);
    return false;
  }

  /** Sayfayı yenileyip sekmeye döner ve veri setini tekrar arar. */
  private void listeyiYenileVeAra(String dataSetName) {
    page.reload();
    clickPublishTab();
    searchDataSet(dataSetName);
  }

  /**
   * "Veri Setini Yayınla" penceresindeki ayarları okur: işaretli formatlar, Data API, yayın
   * zamanlaması ve portaldaki KVKK notu.
   *
   * <p>Yayınla butonuna basılmadan önce çağrılmalıdır; okunan değerler kamuya açık portalda
   * beklenen formatlar ve KVKK notu olarak kullanılır.
   *
   * @param yayinAciklamasi pencereye girilen açıklama (portaldaki değişiklik geçmişinde görünür)
   */
  public YayinAyarlari yayinAyarlariniOkuma(String yayinAciklamasi) {

    Locator onayKutulari =
        popup
            .locator("label")
            .filter(new Locator.FilterOptions().setHas(page.locator("input[type='checkbox']")));
    onayKutulari.first().waitFor();

    List<String> formatlar = new ArrayList<>();
    boolean dataApiAcik = false;

    for (int i = 0; i < onayKutulari.count(); i++) {
      Locator etiket = onayKutulari.nth(i);
      String ad = etiket.innerText().trim();
      boolean isaretli = etiket.locator("input[type='checkbox']").isChecked();

      if (ad.startsWith(DATA_API_ETIKETI)) {
        dataApiAcik = isaretli;
      } else if (isaretli) {
        formatlar.add(ad);
      }
    }

    String zamanlama =
        popup.locator("input[name='publishTiming'][value='scheduled']").isChecked()
            ? "scheduled"
            : "now";

    String kvkkNotu = popup.locator("textarea[name='privacyNote']").inputValue().trim();

    return new YayinAyarlari(formatlar, dataApiAcik, zamanlama, kvkkNotu, yayinAciklamasi);
  }
}
