package com.bvakp.automation.pages.publicPortal;

import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Kamuya açık portalda arama: ana sayfadaki arama kutusu ve sonuç listesi (/dataset).
 *
 * <p>Arama bulanık eşleşir (benzer adlı onlarca sonuç gelir); bu yüzden kart her zaman başlığı
 * birebir aynı olana göre seçilir, ilk sonuca göre değil.
 */
public class PublicPortalSearchPage extends PublicPortalBasePage {

  private final Locator aramaKutusu;
  private final Locator araButonu;
  private final Locator sonucSayisi;

  public PublicPortalSearchPage(Page page) {
    super(page);
    // Ana sayfadaki arama: kutunun erişilebilir adı "Ara" (üst menüdeki kutunun adı farklıdır).
    this.aramaKutusu =
        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Ara").setExact(true));
    this.araButonu =
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Ara").setExact(true));
    this.sonucSayisi = page.getByText(Pattern.compile("veri seti bulundu"));
  }

  /** Portalın ana sayfasını açar. */
  public void portaliAcma() {
    adresAcma("/");
    ReportManager.info("Kamuya açık portal açıldı");
  }

  /** Ana sayfadaki arama kutusuna yazıp "Ara"ya basar ve sonuç sayfasının gelmesini bekler. */
  public void veriSetiArama(String aramaMetni) {

    String beklenenParametre = "q=" + URLEncoder.encode(aramaMetni, StandardCharsets.UTF_8);

    aramaKutusu.fill(aramaMetni);
    araButonu.click();
    page.waitForURL(url -> url.contains(beklenenParametre));

    ReportManager.info("Portalda arandı: " + aramaMetni + " | Sonuç: " + sonucSayisiOkuma());
  }

  /** "5 veri seti bulundu" satırındaki sayı; satır görünmezse 0. */
  public int sonucSayisiOkuma() {
    if (!gorunurMu(sonucSayisi, 5_000)) {
      return 0;
    }
    // Metin "5 veri seti bulundu · “arama”"; arama metnindeki rakamlar karışmasın diye yalnız
    // baştaki sayı alınır (binlik nokta olabilir).
    String sayi = sonucSayisi.innerText().trim().split(" ")[0];
    return Integer.parseInt(sayi.replace(".", ""));
  }

  /** Arama sonuçlarında veri seti kartı varsa true döner. */
  public boolean veriSetiSonuclardaMi(String veriSetiAdi) {
    return gorunurMu(kart(veriSetiAdi));
  }

  /** Sonuç kartındaki değerleri okur. */
  public VeriSetiKarti veriSetiKartiOkuma(String veriSetiAdi) {

    Locator kart = kart(veriSetiAdi);

    return new VeriSetiKarti(
        kart.locator(".bvakp-card-cat").textContent().trim(),
        kart.locator(".bvakp-card-notes").textContent().trim(),
        kart.locator(".bvakp-formats .bvakp-fmt").allTextContents().stream()
            .map(String::trim)
            .toList(),
        kartSayisiOkuma(kart, "satır"),
        kartSayisiOkuma(kart, "kolon"));
  }

  /** Kartın başlığındaki linke tıklayıp veri seti detayını açar. */
  public void veriSetiAcma(String veriSetiAdi) {
    kart(veriSetiAdi)
        .getByRole(
            AriaRole.LINK, new Locator.GetByRoleOptions().setName(veriSetiAdi).setExact(true))
        .click();
    ReportManager.info("Veri seti detayı açıldı: " + veriSetiAdi);
  }

  /**
   * Başlığı birebir eşleşen sonuç kartı; "..._873" ararken "..._8731" seçilmez. Sayfadaki diğer
   * liste öğelerinde (filtreler, menü) başlık olmadığı için yalnız sonuç kartı eşleşir.
   */
  private Locator kart(String veriSetiAdi) {
    return page.getByRole(AriaRole.LISTITEM)
        .filter(
            new Locator.FilterOptions()
                .setHas(
                    page.getByRole(
                        AriaRole.HEADING,
                        new Page.GetByRoleOptions()
                            .setName(veriSetiAdi)
                            .setExact(true)
                            .setLevel(3))));
  }

  /** "3.546 satır", "57 kolon" gibi alt bilgilerden sayıyı alır. */
  private static int kartSayisiOkuma(Locator kart, String birim) {
    String metin =
        kart.locator(".bvakp-card-meta")
            .filter(new Locator.FilterOptions().setHasText(birim))
            .innerText();
    return Integer.parseInt(metin.replaceAll("\\D", ""));
  }

  /**
   * Sonuç kartında görünen değerler.
   *
   * @param formatlar kartta görünen format çipleri (Data API açıksa "API" da gelir)
   */
  public record VeriSetiKarti(
      String kategori, String aciklama, List<String> formatlar, int satirSayisi, int kolonSayisi) {}
}
