package com.bvakp.automation.pages.publicportal;

import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.pages.BasePage;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Kamuya açık portalın arama ve sonuç listesi (/dataset).
 *
 * <p>Arama bulanık eşleşir (benzer adlı onlarca sonuç gelir); bu yüzden kart her zaman
 * başlığı birebir aynı olana göre seçilir, ilk sonuca göre değil.
 */
public class PublicPortalSearchPage extends BasePage {

    private final Locator aramaKutusu;
    private final Locator sonucSayisi;
    private final Locator sonucKartlari;

    public PublicPortalSearchPage(Page page) {
        super(page);
        // Ana sayfada ortadaki arama kutusu, diğer sayfalarda üst menüdeki kutu görünür.
        this.aramaKutusu = page.locator("input[name='q']:visible").first();
        this.sonucSayisi = page.locator(".bvakp-results-count strong");
        this.sonucKartlari = page.locator("li.bvakp-card");
    }

    /** Config'teki public.data.url adresini açar. */
    public void portaliAcma() {
        page.navigate(ConfigManager.get("public.data.url"));
        ReportManager.info("Kamuya açık portal açıldı");
    }

    /** Sayfadaki arama kutusuyla arar ve sonuç sayfasının gelmesini bekler. */
    public void veriSetiArama(String aramaMetni) {

        String beklenenParametre = "q=" + URLEncoder.encode(aramaMetni, StandardCharsets.UTF_8);

        aramaKutusu.fill(aramaMetni);
        aramaKutusu.press("Enter");
        page.waitForURL(url -> url.contains(beklenenParametre));

        ReportManager.info("Portalda arandı: " + aramaMetni + " | Sonuç: " + sonucSayisiOkuma());
    }

    /** "51 veri seti bulundu" satırındaki sayı; sonuç yoksa 0. */
    public int sonucSayisiOkuma() {
        if (!gorunurMu(sonucSayisi, 5_000)) {
            return 0;
        }
        return Integer.parseInt(sonucSayisi.innerText().replaceAll("\\D", ""));
    }

    public boolean veriSetiSonuclardaMi(String veriSetiAdi) {
        return gorunurMu(kart(veriSetiAdi));
    }

    /** Sonuç kartındaki değerleri okur. */
    public VeriSetiKarti veriSetiKartiOkuma(String veriSetiAdi) {

        Locator kart = kart(veriSetiAdi);

        return new VeriSetiKarti(
                kart.locator(".bvakp-card-cat").textContent().trim(),
                kart.locator(".bvakp-card-notes").textContent().trim(),
                kart.locator(".bvakp-formats .bvakp-fmt").allTextContents().stream().map(String::trim).toList(),
                kartSayisiOkuma(kart, "satır"),
                kartSayisiOkuma(kart, "kolon"));
    }

    /** Kartın başlığına tıklayıp veri seti detayını açar. */
    public void veriSetiAcma(String veriSetiAdi) {
        kart(veriSetiAdi).locator(".bvakp-card-title a").click();
        ReportManager.info("Veri seti detayı açıldı: " + veriSetiAdi);
    }

    /** Başlığı birebir eşleşen kart; "..._873" ararken "..._8731" seçilmez. */
    private Locator kart(String veriSetiAdi) {
        return sonucKartlari.filter(new Locator.FilterOptions().setHas(
                page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions()
                        .setName(veriSetiAdi)
                        .setExact(true))));
    }

    /** "49 satır", "11 kolon" gibi alt bilgilerden sayıyı alır. */
    private static int kartSayisiOkuma(Locator kart, String birim) {
        String metin = kart.locator(".bvakp-card-meta")
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
            String kategori,
            String aciklama,
            List<String> formatlar,
            int satirSayisi,
            int kolonSayisi) {
    }
}