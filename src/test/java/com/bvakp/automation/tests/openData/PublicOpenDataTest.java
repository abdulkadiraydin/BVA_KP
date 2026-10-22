package com.bvakp.automation.tests.openData;

import com.bvakp.automation.api.core.ApiYanit;
import com.bvakp.automation.api.publicportal.DatastoreSonuc;
import com.bvakp.automation.api.publicportal.PublicPortalApi;
import com.bvakp.automation.base.ApiTesti;
import com.bvakp.automation.base.AvpBaseTest;
import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.flows.avpFlows.OpenDataCreateFlow;
import com.bvakp.automation.flows.avpFlows.OpenDataPublishFlow;
import com.bvakp.automation.flows.avpFlows.OpenDataSetupFlow;
import com.bvakp.automation.model.KolonTanimi;
import com.bvakp.automation.model.VeriSetiTanimi;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.pages.openData.OpenDataPublishPage;
import com.bvakp.automation.pages.publicportal.PublicPortalDataSetPage;
import com.bvakp.automation.pages.publicportal.PublicPortalResourcePage;
import com.bvakp.automation.pages.publicportal.PublicPortalSearchPage;
import com.bvakp.automation.reporting.ReportManager;
import com.fasterxml.jackson.databind.JsonNode;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Kapalı portalda yayınlanan veri setinin kamuya açık portalda (UI ve API) doğru
 * göründüğünü doğrular.
 *
 * <p>Beklenen değerler, @BeforeClass'ta yayın sırasında ekranlardan toplanan
 * {@link VeriSetiTanimi}'dir. Testler ortak veri setini yalnızca okur; paralel çalışabilir.
 * {@link ApiTesti} işaretli testler tarayıcı açmaz; sayfa nesnesi kullanmamalıdır.
 */
public class PublicOpenDataTest extends AvpBaseTest {

    private static final int PORTAL_INDEKS_BEKLEME_SN = 120;
    private static final int API_KAYIT_LIMITI = 1_000;

    /** @BeforeClass'ta yazılır, testler yalnızca okur. */
    private VeriSetiTanimi tanim;
    private String adresAdi;
    private String kaynakId;

    private record TestContext(
            PublicPortalSearchPage aramaPage,
            PublicPortalDataSetPage veriSetiPage,
            PublicPortalResourcePage kaynakPage,
            PublicPortalApi api) {
    }

    // ------------------------------------------------------------------
    // Hazırlık
    // ------------------------------------------------------------------

    /**
     * Ortak veri seti: oluştur, yayınla, portalda indekslenmesini bekle.
     * Kendi Playwright oturumunu açıp kapatır (bkz. OpenDataUpdateCalendarTest).
     */
    @BeforeClass
    public void ortakVeriSetiHazirlama() {

        PlaywrightManager.initialize(authStateKullan());
        boolean basarili = false;

        try {
            avpOturumuHazirlama();
            new OpenDataPage(page()).acikVeriPortalinaGitme();

            OpenDataPublishPage publishPage = new OpenDataPublishPage(page());
            OpenDataPublishFlow publishFlow = new OpenDataPublishFlow(publishPage);
            OpenDataSetupFlow setupFlow = new OpenDataSetupFlow(
                    new OpenDataCreateFlow(page()), publishPage, publishFlow);

            tanim = setupFlow.yayindaVeriSetiTanimiOlusturma("otomasyon_public_portal");

            JsonNode paket = PublicPortalApi.portaldan()
                    .paketHazirOlanaKadarBekleme(tanim.ad(), PORTAL_INDEKS_BEKLEME_SN);
            adresAdi = paket.path("name").asText();
            kaynakId = PublicPortalApi.datastoreKaynakId(paket).orElseThrow();

            ReportManager.info("Ortak veri seti portalda | " + tanim.ad() + " | " + adresAdi + " | " + kaynakId);
            basarili = true;

        } finally {
            PlaywrightManager.close("ortakVeriSetiHazirlama", basarili ? "HAZIRLIK" : "HATALI");
        }
    }

    private TestContext testHazirligi() {
        return new TestContext(
                new PublicPortalSearchPage(page()),
                new PublicPortalDataSetPage(page()),
                new PublicPortalResourcePage(page()),
                PublicPortalApi.portaldan());
    }

    /** Veri seti detay sayfası açık olarak başlayan test hazırlığı. */
    private TestContext detaySayfasiAcikHazirlik() {
        TestContext ctx = testHazirligi();
        ctx.veriSetiPage().veriSetiSayfasiniAcma(adresAdi);
        Assert.assertTrue(ctx.veriSetiPage().veriSetiSayfasiGoruntulendiMi(tanim.ad()),
                "Ön koşul: veri seti detay sayfası açılamadı: " + tanim.ad());
        return ctx;
    }

    /** Kaynak (önizleme) sayfası açık olarak başlayan test hazırlığı. */
    private TestContext kaynakSayfasiAcikHazirlik() {
        TestContext ctx = detaySayfasiAcikHazirlik();
        ctx.veriSetiPage().onizlemeAcma();
        Assert.assertTrue(ctx.kaynakPage().kaynakSayfasiGoruntulendiMi(tanim.ad()),
                "Ön koşul: kaynak sayfası açılamadı: " + tanim.ad());
        return ctx;
    }

    /** Kart ve kaynak sayfasındaki format çipleri: yayın formatları + Data API açıksa "API". */
    private List<String> beklenenFormatCipleri() {
        List<String> cipler = new ArrayList<>(tanim.yayin().formatlar());
        if (tanim.yayin().dataApiAcik()) {
            cipler.add("API");
        }
        return cipler;
    }

    // ------------------------------------------------------------------
    // Arama ve detay (UI)
    // ------------------------------------------------------------------

    @Test
    public void aramaSonucundaVeriSetiKartiGoruntulenir() {
        TestContext ctx = testHazirligi();

        ctx.aramaPage().portaliAcma();
        ctx.aramaPage().veriSetiArama(tanim.ad());

        Assert.assertTrue(ctx.aramaPage().veriSetiSonuclardaMi(tanim.ad()),
                "Veri seti arama sonuçlarında bulunamadı: " + tanim.ad());

        PublicPortalSearchPage.VeriSetiKarti kart = ctx.aramaPage().veriSetiKartiOkuma(tanim.ad());
        SoftAssert soft = new SoftAssert();
        soft.assertEquals(kart.kategori(), tanim.kategori(), "Kart kategorisi");
        soft.assertEquals(kart.aciklama(), tanim.aciklama(), "Kart açıklaması");
        soft.assertEquals(Set.copyOf(kart.formatlar()), Set.copyOf(beklenenFormatCipleri()), "Kart formatları");
        soft.assertEquals(kart.kolonSayisi(), tanim.kolonAdlari().size(), "Kart kolon sayısı");
        soft.assertAll();

        ctx.aramaPage().veriSetiAcma(tanim.ad());
        Assert.assertTrue(ctx.veriSetiPage().veriSetiSayfasiGoruntulendiMi(tanim.ad()),
                "Karttan veri seti detayı açılamadı.");

        ReportManager.pass("Veri seti aramada bulundu ve kart bilgileri doğru.");
    }

    @Test
    public void detaySayfasiBilgileriDogrudur() {
        TestContext ctx = detaySayfasiAcikHazirlik();
        PublicPortalDataSetPage veriSeti = ctx.veriSetiPage();

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(veriSeti.kategoriOkuma(), tanim.kategori(), "Kategori");
        soft.assertEquals(veriSeti.aciklamaOkuma(), tanim.aciklama(), "Açıklama");
        soft.assertEquals(veriSeti.kolonSayisiOkuma(), tanim.kolonAdlari().size(), "Kolon sayısı");
        soft.assertEquals(veriSeti.ekBilgileriOkuma().get("Sorumlu Birim"), tanim.sorumluBirim(), "Sorumlu Birim");
        soft.assertEquals(veriSeti.lisansOkuma(), tanim.kullanimLisansi(), "Lisans");
        soft.assertEquals(veriSeti.ilkYayinNotuOkuma(), tanim.yayin().yayinAciklamasi(), "İlk yayın notu");
        soft.assertAll();

        ReportManager.pass("Detay sayfası bilgileri kapalı portaldaki değerlerle aynı.");
    }

    @Test
    public void maskelenenAlanlarNotuDogrudur() {
        TestContext ctx = detaySayfasiAcikHazirlik();

        Assert.assertEquals(ctx.veriSetiPage().maskelenenAlanlariOkuma(), tanim.maskelenenKolonlar(),
                "KVKK notundaki maskelenen alanlar farklı.");

        ReportManager.pass("Maskelenen alanlar notu doğru: " + tanim.maskelenenKolonlar());
    }

    @Test
    public void indirmeLinkleriCalisir() {
        TestContext ctx = detaySayfasiAcikHazirlik();

        Map<String, String> linkler = ctx.veriSetiPage().indirmeLinkleriniOkuma();
        Assert.assertEquals(Set.copyOf(linkler.keySet()), Set.copyOf(tanim.yayin().formatlar()),
                "İndirilebilir formatlar yayın ayarlarıyla aynı değil.");

        SoftAssert soft = new SoftAssert();
        linkler.forEach((format, link) -> {
            ApiYanit yanit = ctx.api().dosyaIndirme(link);
            soft.assertEquals(yanit.durumKodu(), 200, format + " indirme durum kodu");
            soft.assertFalse(yanit.govde() == null || yanit.govde().isBlank(), format + " dosyası boş");

            if ("CSV".equals(format) && yanit.basariliMi()) {
                String baslikSatiri = yanit.govde().lines().findFirst().orElse("");
                for (String kolon : tanim.kolonAdlari()) {
                    soft.assertTrue(baslikSatiri.contains(kolon), "CSV başlığında kolon yok: " + kolon);
                }
            }
        });
        soft.assertAll();

        ReportManager.pass("İndirme linkleri çalışıyor: " + linkler.keySet());
    }

    // ------------------------------------------------------------------
    // Kaynak sayfası (UI)
    // ------------------------------------------------------------------

    @Test
    public void veriSozluguVeApiBolumuDogrudur() {
        TestContext ctx = kaynakSayfasiAcikHazirlik();
        PublicPortalResourcePage kaynak = ctx.kaynakPage();

        kaynak.sekmeAcma("Veri Sözlüğü");

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(List.copyOf(kaynak.veriSozluguOkuma().keySet()), tanim.kolonAdlari(), "Veri sözlüğü alanları");
        soft.assertEquals(List.copyOf(kaynak.apiAlanlariniOkuma().keySet()), tanim.kolonAdlari(), "API alanları");
        soft.assertEquals(kaynak.apiKaynakIdOkuma(), kaynakId, "API resource_id");
        soft.assertTrue(kaynak.apiAdresiOkuma().endsWith("/api/3/action/datastore_search"),
                "API adresi beklenen uç nokta değil: " + kaynak.apiAdresiOkuma());
        soft.assertEquals(Set.copyOf(kaynak.formatlariOkuma()), Set.copyOf(beklenenFormatCipleri()), "Kaynak formatları");
        soft.assertAll();

        ReportManager.pass("Veri sözlüğü ve API bölümü kolonlarla aynı.");
    }

    @Test
    public void kayitSayilariApiIleTutarlidir() {
        TestContext ctx = kaynakSayfasiAcikHazirlik();

        int apiToplam = ctx.api().kayitlariGetir(kaynakId, 1).toplam();
        Assert.assertTrue(apiToplam > 0, "API'de kayıt yok.");

        int kaynakToplam = ctx.kaynakPage().toplamKayitSayisiOkuma();

        ctx.veriSetiPage().veriSetiSayfasiniAcma(adresAdi);
        int detaySatir = ctx.veriSetiPage().satirSayisiOkuma();

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(kaynakToplam, apiToplam, "Kaynak sayfası toplam kayıt");
        soft.assertEquals(detaySatir, apiToplam, "Detay sayfası satır sayısı");
        soft.assertAll();

        ReportManager.pass("Kayıt sayıları tutarlı: " + apiToplam);
    }

    // ------------------------------------------------------------------
    // API
    // ------------------------------------------------------------------

    @ApiTesti
    @Test
    public void apiPaketBilgileriDogrudur() {
        PublicPortalApi api = PublicPortalApi.portaldan();

        ApiYanit yanit = api.paketGetir(adresAdi);
        Assert.assertTrue(PublicPortalApi.apiBasariliMi(yanit),
                "package_show başarısız | Durum: " + yanit.durumKodu() + " | " + yanit.kisaGovde());

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(yanit.alan("/result/title").asText(), tanim.ad(), "title");
        soft.assertEquals(yanit.alan("/result/notes").asText(), tanim.aciklama(), "notes");
        soft.assertEquals(yanit.alan("/result/license_title").asText(), tanim.kullanimLisansi(), "license_title");
        soft.assertEquals(yanit.alan("/result/groups/0/title").asText(), tanim.kategori(), "groups[0].title");
        soft.assertAll();

        ReportManager.pass("API paket bilgileri doğru.");
    }

    @ApiTesti
    @Test
    public void apiKayitlariMaskelidir() {
        PublicPortalApi api = PublicPortalApi.portaldan();

        DatastoreSonuc sonuc = api.kayitlariGetir(kaynakId, API_KAYIT_LIMITI);
        Assert.assertEquals(sonuc.alanAdlari(), tanim.kolonAdlari(), "API alanları kolonlarla aynı değil.");

        SoftAssert soft = new SoftAssert();
        for (String kolon : tanim.maskelenenKolonlar()) {
            List<String> acikDegerler = sonuc.degerler(kolon).stream()
                    .filter(deger -> deger == null || !deger.contains("*"))
                    .toList();
            soft.assertTrue(acikDegerler.isEmpty(), kolon + " kolonunda maskelenmemiş değer var: " + acikDegerler);
        }
        soft.assertAll();

        ReportManager.pass("Maskeli kolonların bütün değerleri maskeli: " + tanim.maskelenenKolonlar());
    }

    @ApiTesti
    @Test
    public void apiKayitlariOnizlemeIleAynidir() {
        PublicPortalApi api = PublicPortalApi.portaldan();

        List<Map<String, String>> onizleme = tanim.onizlemeKayitlari();
        Assert.assertFalse(onizleme.isEmpty(), "Ön koşul: kapalı portal önizlemesi okunamadı.");

        // Önizleme ile API'nin sıralaması farklı; her önizleme satırı API kayıtları içinde aranır.
        // Eşleşen kayıt listeden çıkarılır, böylece tekrar eden satırlar da doğru sayılır.
        List<Map<String, String>> apiSatirlari = new ArrayList<>(
                api.kayitlariGetir(kaynakId, API_KAYIT_LIMITI).kayitlar().stream()
                        .map(this::maskesizKolonlar)
                        .toList());

        List<Map<String, String>> bulunamayanlar = new ArrayList<>();
        for (Map<String, String> satir : onizleme) {
            Map<String, String> beklenen = maskesizKolonlar(satir);
            if (!apiSatirlari.remove(beklenen)) {
                bulunamayanlar.add(beklenen);
            }
        }

        Assert.assertTrue(bulunamayanlar.isEmpty(), "Önizlemedeki bu satırlar API'de yok: " + bulunamayanlar);

        ReportManager.pass("Önizlemedeki " + onizleme.size() + " kaydın tamamı API'de var.");
    }

    /** Satırın yalnızca maskesiz, yayına dahil kolonları (karşılaştırma için). */
    private Map<String, String> maskesizKolonlar(Map<String, String> satir) {
        Map<String, String> sonuc = new LinkedHashMap<>();
        for (String kolon : tanim.kolonAdlari()) {
            if (!tanim.maskelenenKolonlar().contains(kolon)) {
                sonuc.put(kolon, satir.get(kolon));
            }
        }
        return sonuc;
    }

    /**
     * Bilinen bulgu: kaynaktaki integer/date gibi tipler portalda "text" oluyor.
     * Düzeltilene kadar kırmızı kalması beklenir. Maskeli kolonlar metne dönüştüğü için hariçtir.
     */
    @ApiTesti
    @Test(description = "Kolon tipleri portalda korunur (bilinen bulgu: hepsi text)")
    public void kolonTipleriKorunur() {
        PublicPortalApi api = PublicPortalApi.portaldan();

        Map<String, String> apiTipleri = api.kayitlariGetir(kaynakId, 1).alanlar();

        List<String> kaybolanTipler = new ArrayList<>();
        for (KolonTanimi kolon : tanim.kolonlar()) {
            if (!kolon.dahil() || kolon.maskeliMi() || metinTipiMi(kolon.veriTipi())) {
                continue;
            }
            if ("text".equals(apiTipleri.get(kolon.ad()))) {
                kaybolanTipler.add(kolon.ad() + " (" + kolon.veriTipi() + " → text)");
            }
        }

        Assert.assertTrue(kaybolanTipler.isEmpty(), "Portalda tipi kaybolan kolonlar: " + kaybolanTipler);

        ReportManager.pass("Kolon tipleri portalda korunuyor.");
    }

    private static boolean metinTipiMi(String veriTipi) {
        String tip = veriTipi == null ? "" : veriTipi.toLowerCase();
        return tip.contains("char") || tip.contains("text") || tip.contains("string");
    }
}