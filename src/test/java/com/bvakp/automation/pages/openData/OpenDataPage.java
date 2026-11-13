package com.bvakp.automation.pages.openData;

import com.bvakp.automation.model.KolonTanimi;
import com.bvakp.automation.pages.BasePage;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Açık Veri Portalı (kapalı portal) kaynak listesi ve veri seti oluşturma ekranı. */
public class OpenDataPage extends BasePage {

  public OpenDataPage(Page page) {

    super(page);
  }

  // =========================
  // Kaynak Oluşturma
  // =========================

  /** Sol menüden Açık Veri Portalı ekranına geçer. */
  public void acikVeriPortalinaGitme() {

    page.getByRole(
            AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Açık Veri Portalı").setExact(true))
        .click();
  }

  /** Kaynak detayında Kaynak Tablo / Sorgu bölümü görünüyorsa true döner. */
  public boolean kaynakTabloSorguGoruntulendiMi() {
    return gorunurMu(
        page.getByText("Kaynak Tablo / Sorgu", new Page.GetByTextOptions().setExact(true)));
  }

  /** Kaynak Ekle ekranını açar. */
  public void kaynakEklemeEkraniniAcma() {

    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Kaynak Ekle")).click();
  }

  /** Kaynak listede görünüyorsa true döner. */
  public boolean kaynakListedeMi(String kaynakAdi) {
    return gorunurMu(kaynakSatiri(kaynakAdi));
  }

  /**
   * Kaynak satırında oluşturulan ad ve açıklamanın birlikte görüntülendiğini kontrol eder.
   *
   * @param kaynakAdi kaynak adı
   * @param kaynakAciklamasi kaynak açıklaması
   * @return bilgiler doğruysa true
   */
  public boolean kaynakBilgileriDogruMu(String kaynakAdi, String kaynakAciklamasi) {

    Locator satir = kaynakSatiri(kaynakAdi);
    if (!gorunurMu(satir)) {
      return false;
    }

    String satirMetni = satir.innerText();
    return satirMetni.contains(kaynakAdi) && satirMetni.contains(kaynakAciklamasi);
  }

  /**
   * Belirtilen kaynak satırındaki Sil butonuna tıklar.
   *
   * <p>Güvenlik amacıyla yalnızca otomasyon tarafından oluşturulan kaynakların silinmesine izin
   * verilir.
   *
   * @param kaynakAdi silinecek kaynak adı
   */
  public void kaynakSilmeButonunaTiklama(String kaynakAdi) {

    if (!kaynakAdi.startsWith("otomasyon_")) {

      throw new IllegalArgumentException(
          "Otomasyon tarafından oluşturulmayan kaynak silinemez: " + kaynakAdi);
    }

    kaynakSatiri(kaynakAdi)
        .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Sil").setExact(true))
        .click();
  }

  /** Kaynağın satırındaki Görüntüle butonuna tıklar. */
  public void kaynakGoruntulemeyeTiklama(String kaynakAdi) {

    kaynakSatiri(kaynakAdi).getByLabel("Görüntüle").click();
  }

  /** Genel Bilgi sekmesine geçer. */
  public void genelBilgiSekmesineTiklama() {

    page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Genel Bilgi").setExact(true))
        .click();
  }

  /**
   * Kaynak listede görünmüyorsa true döner (silinme veya ad değişikliği sonrası). Kayıt zaten yoksa
   * beklemeden döner.
   */
  public boolean kaynakListedeYokMu(String kaynakAdi) {
    return gorunmuyorMu(kaynakSatiri(kaynakAdi));
  }

  /** Kaynak detayında Kaynak Bilgileri ve Kayıt Bilgileri başlıkları görünüyorsa true döner. */
  public boolean genelBilgiSayfasiAcikMi() {
    Locator kaynakBilgileri =
        page.getByRole(
            AriaRole.HEADING,
            new Page.GetByRoleOptions().setName("Kaynak Bilgileri").setExact(true));
    Locator kayitBilgileri =
        page.getByRole(
            AriaRole.HEADING,
            new Page.GetByRoleOptions().setName("Kayıt Bilgileri").setExact(true));

    return gorunurMu(kaynakBilgileri) && gorunurMu(kayitBilgileri);
  }

  /** Önizleme sekmesine geçer. */
  public void onizlemeSekmesineTiklama() {

    page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Önizleme").setExact(true))
        .click();
  }

  /** Önizleme sekmesinin açıklama metni ve en az bir veri hücresi görünüyorsa true döner. */
  public boolean onizlemeSayfasiGoruntulendiMi() {

    Locator aciklama =
        page.getByText(
            "Kaynaktan gelen ham veridir; anonimleştirme kuralları henüz uygulanmamıştır.",
            new Page.GetByTextOptions().setExact(true));

    return gorunurMu(aciklama) && gorunurMu(page.locator("td").first());
  }

  /**
   * Belirtilen kaynak satırındaki Düzenle butonuna tıklar.
   *
   * @param kaynakAdi düzenlenecek kaynak adı
   */
  public void kaynakDuzenlemeButonunaTiklama(String kaynakAdi) {

    kaynakSatiri(kaynakAdi).getByLabel("Düzenle").click();
  }

  /**
   * Kaynak ara alanına verilen kriteri yazar ve arama işlemini başlatır.
   *
   * @param aramaKriteri aranacak kaynak değeri
   */
  public void kaynakArama(String aramaKriteri) {

    Locator kaynakAramaAlani = page.getByPlaceholder("Kaynak ara");

    kaynakAramaAlani.fill(aramaKriteri);

    kaynakAramaAlani.press("Enter");
  }

  /**
   * Kaynak araması sonrasında arama kriterini içeren en az bir görünür kaynak kaydının yüklenmesini
   * bekler ve sonucu doğrular.
   *
   * @param aramaKriteri aranacak kaynak kriteri
   * @return kriteri içeren görünür kaynak varsa true
   */
  public boolean kaynakAramaSonuclariUygunMu(String aramaKriteri) {

    Locator eslesenKaynaklar =
        page.getByText(aramaKriteri, new Page.GetByTextOptions().setExact(false));

    try {

      /*
       * Arama sonrasında tablo asenkron yüklendiği için
       * ilk eşleşen kaydın görünür olması beklenir.
       */
      eslesenKaynaklar
          .first()
          .waitFor(
              new Locator.WaitForOptions()
                  .setState(WaitForSelectorState.VISIBLE)
                  .setTimeout(15000));

    } catch (PlaywrightException e) {

      ReportManager.info(
          "Arama kriterini içeren görünür kaynak yüklenemedi" + " | Kriter: " + aramaKriteri);

      return false;
    }

    int gorunenKayitSayisi = 0;

    for (int i = 0; i < eslesenKaynaklar.count(); i++) {

      Locator kaynak = eslesenKaynaklar.nth(i);

      if (!kaynak.isVisible()) {
        continue;
      }

      String kaynakMetni = kaynak.innerText().trim();

      if (kaynakMetni.toLowerCase().contains(aramaKriteri.trim().toLowerCase())) {

        gorunenKayitSayisi++;

        ReportManager.info("Arama kriteriyle eşleşen kaynak bulundu" + " | Kaynak: " + kaynakMetni);
      }
    }

    ReportManager.info("Arama kriteriyle eşleşen görünür kaynak sayısı: " + gorunenKayitSayisi);

    return gorunenKayitSayisi > 0;
  }

  /** "Henüz kaynak eklenmemiş" mesajı görünüyorsa true döner. */
  public boolean kaynakBulunamadiMesajiVarMi() {
    return gorunurMu(
        page.getByText(
            "Henüz kaynak eklenmemiş. \"Kaynak Ekle\" ile başlayın.",
            new Page.GetByTextOptions().setExact(true)),
        10_000);
  }

  /** Verilen kaynak kaydını Veri Seti Oluşturma ekranında seçer. */
  public void kaynakSecme(String kaynakAdi) {

    kaynakSatiri(kaynakAdi).getByLabel("Seçim").check();
  }

  /** Kaynak kaydının seçili olduğunu kontrol eder. */
  public boolean kaynakSeciliMi(String kaynakAdi) {

    return kaynakSatiri(kaynakAdi).getByLabel("Seçim").isChecked();
  }

  /** Veri Seti Oluşturma ekranında sonraki adıma geçer. */
  public void veriSetiIleriGitme() {

    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("İleri").setExact(true))
        .click();
  }

  /** Veri seti adını girer. */
  public void veriSetiAdiGirme(String veriSetiAdi) {

    page.getByLabel("Veri Seti Adı *").fill(veriSetiAdi);
  }

  /** Veri seti adı alanındaki değeri döndürür. */
  public String veriSetiAdiAlma() {

    return page.getByLabel("Veri Seti Adı *").inputValue();
  }

  /** Sorumlu birimi seçer. */
  public void sorumluBirimSecme(String sorumluBirim) {
    page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Sorumlu Birim")).click();

    page.locator("[role='listbox'] [role='option']")
        .filter(new Locator.FilterOptions().setHasText(sorumluBirim))
        .click();
  }

  /** Veri seti kategorisini seçer. */
  public void kategoriSecme(String kategori) {

    page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Kategori")).click();

    page.locator("[role='listbox'] [role='option']")
        .filter(new Locator.FilterOptions().setHasText(kategori))
        .click();
  }

  /** Kullanım lisansını seçer. */
  public void kullanimLisansiSecme(String lisans) {
    page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Kullanım Lisansı"))
        .click();

    page.locator("[role='listbox'] [role='option']")
        .filter(new Locator.FilterOptions().setHasText(lisans))
        .click();
  }

  /** Veri seti açıklamasını girer. */
  public void veriSetiAciklamasiGirme(String aciklama) {

    page.getByLabel("Açıklama").fill(aciklama);
  }

  /** Veri seti açıklamasını döndürür. */
  public String veriSetiAciklamasiAlma() {

    return page.getByLabel("Açıklama").inputValue();
  }

  /** Veri seti önizleme ekranına geçer. */
  public void veriSetiOnizleme() {

    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Önizle").setExact(true))
        .click();
  }

  /** Veri setini onaya gönderir. */
  public void veriSetiOnayaGonderme() {

    page.getByRole(
            AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Onaya Gönder").setExact(true))
        .click();
  }

  /** Onaya gönderme işlemindeki ikinci onay butonuna tıklar. */
  public void onayaGondermeyiOnaylama() {

    page.getByRole(
            AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Onaya Gönder").setExact(true))
        .click();
  }

  /** "Veri seti oluşturuldu ve KVKK..." bildirimi görünüyorsa true döner. */
  public boolean kvkkOnayinaGonderildiMi() {
    return gorunurMu(
        page.getByText(
            "Veri seti oluşturuldu ve KVKK", new Page.GetByTextOptions().setExact(false)));
  }

  /** Veri seti adı zorunlu alan mesajı görünüyorsa true döner. */
  public boolean veriSetiAdiZorunluMesajiVarMi() {
    return gorunurMu(
        page.getByText("Veri seti adı zorunludur.", new Page.GetByTextOptions().setExact(true)),
        10_000);
  }

  /** Sorumlu birim zorunlu alan mesajı görünüyorsa true döner. */
  public boolean sorumluBirimZorunluMesajiVarMi() {
    return gorunurMu(
        page.getByText("Sorumlu birim zorunludur.", new Page.GetByTextOptions().setExact(true)),
        10_000);
  }

  /** Kategori zorunlu alan mesajı görünüyorsa true döner. */
  public boolean kategoriZorunluMesajiVarMi() {
    return gorunurMu(
        page.getByText("Kategori zorunludur.", new Page.GetByTextOptions().setExact(true)), 10_000);
  }

  public void kolonaMaskelemeKuraliUygulama(String kolonAdi) {

    Locator kolonSatiri = page.locator("[role='row'][data-id='" + kolonAdi + "']");
    kolonSayfasinaGitme(kolonSatiri);

    kolonSatiri.locator("[data-field='__rule'] [role='combobox']").click();

    page.locator("li[role='option'][data-value='MASK']").click();
  }

  /** Kolon tablosunu baştan başlayarak, verilen satır görünene kadar sayfa sayfa gezer. */
  private void kolonSayfasinaGitme(Locator kolonSatiri) {
    page.locator("[role='grid'] [role='row'][data-id]").first().waitFor();

    // Birinci sayfaya döner.
    while (sayfaDugmesi(true).isEnabled() && kolonSatiri.count() == 0) {
      sayfaDegistirme(sayfaDugmesi(true));
    }
    // Satır bulunana ya da son sayfaya gelinene kadar ilerler.
    while (kolonSatiri.count() == 0 && sayfaDugmesi(false).isEnabled()) {
      sayfaDegistirme(sayfaDugmesi(false));
    }
  }

  /** Kolon tablosunun önceki (true) ya da sonraki (false) sayfa düğmesi. */
  private Locator sayfaDugmesi(boolean onceki) {
    Locator dugmeler =
        page.locator(".MuiDataGrid-footerContainer .MuiTablePagination-actions button");
    return onceki ? dugmeler.first() : dugmeler.last();
  }

  /** Düğmeye basar ve tablonun ilk satırı değişene kadar bekler. */
  private void sayfaDegistirme(Locator dugme) {
    String ilkSatir =
        page.locator("[role='grid'] [role='row'][data-id]").first().getAttribute("data-id");
    dugme.click();
    page.locator("[role='grid'] [role='row'][data-id='" + ilkSatir + "']")
        .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.DETACHED));
  }

  /** Önizlemede verilen kolon adı görünüyorsa true döner. */
  public boolean onizlemeKolonuGoruntulendiMi(String kolonAdi) {
    return gorunurMu(page.getByText(kolonAdi, new Page.GetByTextOptions().setExact(true)));
  }

  /** Önizlemede "Maskelendi" etiketi görünüyorsa true döner. */
  public boolean maskelendiBilgisiVarMi() {
    return gorunurMu(page.getByText("Maskelendi", new Page.GetByTextOptions().setExact(true)));
  }

  /**
   * Anonimleştirilmiş önizleme tablosunda yıldız karakterleriyle maskelenmiş veri bulunup
   * bulunmadığını kontrol eder.
   */
  public boolean maskelenmisVeriGoruntulendiMi() {

    Locator hucreler = page.getByRole(AriaRole.CELL);

    int hucreSayisi = hucreler.count();

    for (int i = 0; i < hucreSayisi; i++) {

      Locator hucre = hucreler.nth(i);

      if (!hucre.isVisible()) {
        continue;
      }

      String hucreMetni = hucre.innerText().trim();

      /*
       * Maskelenmiş değerlerde en az
       * dört yıldız bulunması beklenir.
       */
      if (hucreMetni.contains("****")) {

        ReportManager.info("Maskelenmiş veri bulundu" + " | Değer: " + hucreMetni);

        return true;
      }
    }

    return false;
  }

  /**
   * Kaynak adı birebir eşleşen satır. Beklemez: tıklama ve okuma metotları kendileri bekler,
   * görünürlük kontrolleri gorunurMu / gorunmuyorMu ile yapılır.
   */
  private Locator kaynakSatiri(String kaynakAdi) {

    Locator kaynakAdiLocator =
        page.getByText(kaynakAdi, new Page.GetByTextOptions().setExact(true));

    return page.getByRole(AriaRole.ROW)
        .filter(new Locator.FilterOptions().setHas(kaynakAdiLocator));
  }

  /**
   * "Alan Seçimi ve Anonimleştirme" ekranındaki kolon tablosunu okur: kolon adı, veri tipi,
   * anonimleştirme kuralı ve yayına dahil olup olmadığı.
   *
   * <p>Kurallar uygulandıktan sonra çağrılmalıdır; okunan değerler kamuya açık portaldaki veri
   * sözlüğü, API alanları ve maskeleme kontrolleri için beklenen değerdir.
   */
  public List<KolonTanimi> alanTanimlariniOkuma() {

    Locator satirlar = page.locator("[role='grid'] [role='row'][data-id]");
    satirlar.first().waitFor();

    Map<String, KolonTanimi> kolonlar = new LinkedHashMap<>();

    while (true) {
      for (Locator satir : satirlar.all()) {
        String ad = satir.getAttribute("data-id");
        if (!kolonlar.containsKey(ad)) {
          Locator kuralHucresi = satir.locator("[data-field='__rule']");
          kolonlar.put(
              ad,
              new KolonTanimi(
                  ad,
                  satir.locator("[data-field='dataType']").innerText().trim(),
                  kuralHucresi.locator("input[aria-hidden='true']").inputValue(),
                  kuralHucresi.locator("[role='combobox']").innerText().trim(),
                  satir.locator("[data-field='__included'] input[type='checkbox']").isChecked()));
        }
      }

      if (!sayfaDugmesi(false).isEnabled()) {
        break;
      }
      sayfaDegistirme(sayfaDugmesi(false));
    }

    ReportManager.info("Okunan kolon sayısı: " + kolonlar.size());
    return new ArrayList<>(kolonlar.values());
  }

  /**
   * "Önizleme ve KVKK Kontrolü" ekranındaki anonimleştirilmiş önizleme tablosunu (ilk 10 kayıt)
   * kolon adı → değer eşlemesi olarak okur.
   */
  public List<Map<String, String>> onizlemeKayitlariniOkuma() {

    Locator tablo = page.locator("table:has(thead)");
    tablo.locator("tbody tr").first().waitFor();

    List<String> basliklar =
        tablo.locator("thead th p").allInnerTexts().stream().map(String::trim).toList();

    Locator satirlar = tablo.locator("tbody tr");
    List<Map<String, String>> kayitlar = new ArrayList<>();

    for (int i = 0; i < satirlar.count(); i++) {
      List<String> hucreler = satirlar.nth(i).locator("td").allInnerTexts();

      Map<String, String> kayit = new LinkedHashMap<>();
      for (int j = 0; j < basliklar.size() && j < hucreler.size(); j++) {
        kayit.put(basliklar.get(j), hucreler.get(j).trim());
      }
      kayitlar.add(kayit);
    }

    return kayitlar;
  }
}
