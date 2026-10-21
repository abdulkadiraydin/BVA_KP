package com.bvakp.automation.pages.publicportal;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Kamuya açık portalda veri seti detay sayfası (/dataset/{ad}).
 *
 * <p>Değerler okunur, doğrulama testte yapılır.
 */
public class PublicPortalDataSetPage extends BasePage {

    private static final Pattern MASKELENEN_ALANLAR = Pattern.compile("Maskelenen alanlar:\\s*(.+?)\\.?$");
    private static final Pattern KAYNAK_ID = Pattern.compile("/resource/([^/?#]+)");

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
        this.ilkYayinNotu = page.locator(
                ".bvakp-history-row:has(.bvakp-history-kind.is-first_publish) .bvakp-history-note");
    }

    /** Detay sayfasını adres adıyla (slug) doğrudan açar; aramayı test etmeyen testler içindir. */
    public void veriSetiSayfasiniAcma(String adresAdi) {
        page.navigate(ConfigManager.get("public.data.url").replaceAll("/+$", "") + "/dataset/" + adresAdi);
    }

    /** Başlığı verilen adla birebir aynı olan detay sayfası açıldıysa true. */
    public boolean veriSetiSayfasiGoruntulendiMi(String veriSetiAdi) {
        return gorunurMu(page.locator("main.bvakp-detail").getByRole(AriaRole.HEADING,
                new Locator.GetByRoleOptions().setName(veriSetiAdi).setExact(true).setLevel(1)));
    }

    /** URL'deki ad (slug); API'de package_show için kullanılır. */
    public String adresAdiOkuma() {
        String yol = page.url().replaceAll("[?#].*$", "");
        return yol.substring(yol.lastIndexOf('/') + 1);
    }

    public String kategoriOkuma() {
        return kategori.textContent().trim();
    }

    public String aciklamaOkuma() {
        return aciklama.textContent().trim();
    }

    public int satirSayisiOkuma() {
        return ozetSayisiOkuma("satır");
    }

    public int kolonSayisiOkuma() {
        return ozetSayisiOkuma("kolon");
    }

    /** KVKK notundaki "Maskelenen alanlar: a, b." listesi; not yoksa boş liste. */
    public List<String> maskelenenAlanlariOkuma() {

        if (!gorunurMu(kvkkNotu, 2_000)) {
            return List.of();
        }

        Matcher eslesme = MASKELENEN_ALANLAR.matcher(kvkkNotu.textContent().trim());
        if (!eslesme.find()) {
            return List.of();
        }
        return Arrays.stream(eslesme.group(1).split(",")).map(String::trim).toList();
    }

    /** "Veri ve Kaynaklar" listesi: format → indirme linki (sayfadaki sırayla). */
    public Map<String, String> indirmeLinkleriniOkuma() {

        Map<String, String> linkler = new LinkedHashMap<>();
        for (Locator kaynak : kaynaklar.all()) {
            linkler.put(kaynak.locator(".bvakp-fmt").textContent().trim(),
                    kaynak.locator(".bvakp-resource-actions a").getAttribute("href"));
        }
        return linkler;
    }

    /** Önizle linkindeki kaynak (resource) id'si; DataStore API'de kullanılır. */
    public String kaynakIdOkuma() {
        Matcher eslesme = KAYNAK_ID.matcher(onizleLinki.getAttribute("href"));
        if (!eslesme.find()) {
            throw new IllegalStateException("Önizle linkinde kaynak id'si bulunamadı.");
        }
        return eslesme.group(1);
    }

    /** "Ek Bilgiler" tablosu: Yayımcı, Sorumlu Birim, İlk Yayın Tarihi... */
    public Map<String, String> ekBilgileriOkuma() {

        Map<String, String> bilgiler = new LinkedHashMap<>();
        for (Locator satir : ekBilgiSatirlari.all()) {
            bilgiler.put(satir.locator("th").textContent().trim(), satir.locator("td").textContent().trim());
        }
        return bilgiler;
    }

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