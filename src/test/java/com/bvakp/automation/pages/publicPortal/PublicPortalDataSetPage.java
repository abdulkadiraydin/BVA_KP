package com.bvakp.automation.pages.publicPortal;

import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.pages.BasePage;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Kamuya açık portalda veri seti detay sayfası (/dataset/{ad}).
 *
 * <p>Değerler okunur, doğrulama testte yapılır.
 */
public class PublicPortalDataSetPage extends BasePage {

  private static final String MASKELENEN_ALANLAR_ETIKETI = "Maskelenen alanlar:";
  private final Locator kategori;
  private final Locator aciklama;
  private final Locator ozetBilgiler;
  private final Locator kvkkNotu;
  private final Locator kaynaklar;
  private final Locator onizleLinki;
  private final Locator ekBilgiSatirlari;
  private final Locator lisans;
  private final Locator ilkYayinNotu;

  public PublicPortalDataSetPage(Page page) {
    super(page);
    this.kategori = page.locator("main.bvakp-detail .bvakp-chip-cat");
    this.aciklama = page.locator(".bvakp-about .bvakp-prose");
    this.ozetBilgiler = page.locator(".bvakp-about-facts span");
    this.kvkkNotu = page.locator(".bvakp-about-privacy");
    this.kaynaklar = page.locator("li.bvakp-resource");
    this.onizleLinki = page.locator(".bvakp-panel-head a[href*='/resource/']");
    this.ekBilgiSatirlari = page.locator(".bvakp-info-table tr");
    this.lisans = page.locator(".bvakp-side-license span");
    this.ilkYayinNotu =
        page.locator(
            ".bvakp-history-row:has(.bvakp-history-kind.is-first_publish) .bvakp-history-note");
  }

  /** Veri setinin detay sayfasını adres adıyla (/dataset/<ad>) açar. */
  public void veriSetiSayfasiniAcma(String adresAdi) {
    page.navigate(tabanAdres() + "/dataset/" + adresAdi);
  }

  /** public.data.url; sondaki "/" işaretleri atılır (adres config'te "/" ile bitebilir). */
  private static String tabanAdres() {
    String adres = ConfigManager.get("public.data.url");
    while (adres.endsWith("/")) {
      adres = adres.substring(0, adres.length() - 1);
    }
    return adres;
  }

  /** Başlığı verilen adla birebir aynı olan detay sayfası açıldıysa true. */
  public boolean veriSetiSayfasiGoruntulendiMi(String veriSetiAdi) {
    return gorunurMu(
        page.locator("main.bvakp-detail")
            .getByRole(
                AriaRole.HEADING,
                new Locator.GetByRoleOptions().setName(veriSetiAdi).setExact(true).setLevel(1)));
  }

  /** Kategori metnini döndürür. */
  public String kategoriOkuma() {
    return kategori.textContent().trim();
  }

  /** Açıklama metnini döndürür. */
  public String aciklamaOkuma() {
    return aciklama.textContent().trim();
  }

  /** Özet satırındaki kayıt (satır) sayısını döndürür. */
  public int satirSayisiOkuma() {
    return ozetSayisiOkuma("satır");
  }

  /** Özet satırındaki kolon sayısını döndürür. */
  public int kolonSayisiOkuma() {
    return ozetSayisiOkuma("kolon");
  }

  /** KVKK notundaki "Maskelenen alanlar: a, b." listesi; not yoksa boş liste. */
  public List<String> maskelenenAlanlariOkuma() {

    if (!gorunurMu(kvkkNotu)) {
      return List.of();
    }

    String not = kvkkNotu.textContent().trim();
    int baslangic = not.indexOf(MASKELENEN_ALANLAR_ETIKETI);
    if (baslangic < 0) {
      return List.of();
    }

    String liste = not.substring(baslangic + MASKELENEN_ALANLAR_ETIKETI.length()).trim();
    if (liste.endsWith(".")) {
      liste = liste.substring(0, liste.length() - 1);
    }

    return Arrays.stream(liste.split(","))
        .map(String::trim)
        .filter(alan -> !alan.isEmpty())
        .toList();
  }

  /** "Veri ve Kaynaklar" listesi: format → indirme linki (sayfadaki sırayla). */
  public Map<String, String> indirmeLinkleriniOkuma() {

    Map<String, String> linkler = new LinkedHashMap<>();
    for (Locator kaynak : kaynaklar.all()) {
      linkler.put(
          kaynak.locator(".bvakp-fmt").textContent().trim(),
          kaynak.locator(".bvakp-resource-actions a").getAttribute("href"));
    }
    return linkler;
  }

  /** "Ek Bilgiler" tablosu: Yayımcı, Sorumlu Birim, İlk Yayın Tarihi... */
  public Map<String, String> ekBilgileriOkuma() {

    Map<String, String> bilgiler = new LinkedHashMap<>();
    for (Locator satir : ekBilgiSatirlari.all()) {
      bilgiler.put(
          satir.locator("th").textContent().trim(), satir.locator("td").textContent().trim());
    }
    return bilgiler;
  }

  /** Lisans adını döndürür. */
  public String lisansOkuma() {
    return lisans.textContent().trim();
  }

  /** Değişiklik Geçmişi'ndeki "İlk yayın" satırının notu (yayın açıklaması). */
  public String ilkYayinNotuOkuma() {
    return ilkYayinNotu.textContent().trim();
  }

  /** "Önizle" ile kaynak sayfasını (tablo, veri sözlüğü, API) açar. */
  public void onizlemeAcma() {
    onizleLinki.click();
    ReportManager.info("Kaynak önizleme sayfası açıldı");
  }

  /** Özet satırındaki "<strong>49</strong> satır" gibi değerden sayıyı alır. */
  private int ozetSayisiOkuma(String birim) {
    String metin = ozetBilgiler.filter(new Locator.FilterOptions().setHasText(birim)).innerText();
    return Integer.parseInt(metin.replaceAll("\\D", ""));
  }
}
