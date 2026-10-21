package com.bvakp.automation.tests.openData;

import com.bvakp.automation.base.AvpBaseTest;
import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.flows.avpFlows.OpenDataCreateFlow;
import com.bvakp.automation.flows.avpFlows.OpenDataPublishFlow;
import com.bvakp.automation.flows.avpFlows.OpenDataSetupFlow;
import com.bvakp.automation.flows.avpFlows.OpenDataUpdateCalendarFlow;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.pages.openData.OpenDataPublishPage;
import com.bvakp.automation.pages.openData.OpenDataUpdateCalendarPage;
import com.bvakp.automation.reporting.ReportManager;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Güncelleme Takvimi sekmesi testleri.
 *
 * Salt okunur testler, @BeforeClass'ta bir kez hazırlanan ortak veri setini kullanır.
 * Veriyi değiştiren testler kendi veri setini oluşturur.
 */
public class OpenDataUpdateCalendarTest extends AvpBaseTest {

    /** @BeforeClass'ta yazılır, testler yalnızca okur. Yayında ve bir kez manuel güncellenmiş (v2). */
    private String hazirVeriSeti;

    private record TestContext(
            OpenDataUpdateCalendarPage calendarPage,
            OpenDataUpdateCalendarFlow calendarFlow,
            OpenDataPublishPage publishPage,
            OpenDataPublishFlow publishFlow,
            OpenDataSetupFlow setupFlow) {
    }

    // ------------------------------------------------------------------
    // Hazırlık
    // ------------------------------------------------------------------

    /** Oturumu açar, Açık Veri Portalına geçer ve test nesnelerini döndürür. */
    private TestContext nesneleriOlusturma() {

        avpOturumuHazirlama();
        new OpenDataPage(page()).acikVeriPortalinaGitme();

        OpenDataPublishPage publishPage = new OpenDataPublishPage(page());
        OpenDataPublishFlow publishFlow = new OpenDataPublishFlow(publishPage);
        OpenDataCreateFlow createFlow = new OpenDataCreateFlow(page());
        OpenDataUpdateCalendarPage calendarPage = new OpenDataUpdateCalendarPage(page());

        return new TestContext(
                calendarPage,
                new OpenDataUpdateCalendarFlow(calendarPage),
                publishPage,
                publishFlow,
                new OpenDataSetupFlow(createFlow, publishPage, publishFlow));
    }

    /** Takvim sekmesi açık olarak başlayan test hazırlığı. */
    private TestContext testHazirligi() {
        TestContext ctx = nesneleriOlusturma();
        ctx.calendarPage().guncellemeTakvimiSekmesineGecme();
        return ctx;
    }

    /**
     * Ortak veri seti: oluştur, yayınla, bir kez manuel güncelle (v2).
     * Sınıftaki testlerden önce bir kez çalışır. BaseTest'in her test öncesi
     * oturumu bu aşamada henüz açılmadığı için kendi Playwright oturumunu açıp kapatır.
     */
    @BeforeClass
    public void ortakVeriSetiHazirlama() {

        PlaywrightManager.initialize(authStateKullan());
        try {
            TestContext ctx = nesneleriOlusturma();

            ReportManager.info("Ortak veri seti hazırlanıyor.");
            hazirVeriSeti = ctx.setupFlow().yayindaVeriSetiOlusturma("otomasyon_takvim_ortak");

            ctx.publishFlow().manuallyUpdateDataSet(hazirVeriSeti);
            ctx.publishPage().waitForPortalStatus(hazirVeriSeti, "Aktarıldı");

            // Testler başlamadan geçmiş kaydı oluşmuş olsun
            ctx.calendarPage().gecmisteKayitBekleme(hazirVeriSeti);

            ReportManager.info("Ortak veri seti hazır: " + hazirVeriSeti);

        } finally {
            PlaywrightManager.close("ortakVeriSetiHazirlama", "HAZIRLIK");
        }
    }

    // ------------------------------------------------------------------
    // Sayfa genel yapısı (veri gerektirmez)
    // ------------------------------------------------------------------

    @Test
    public void sekmeGoruntuleme() {
        TestContext ctx = testHazirligi();

        ReportManager.info("Güncelleme Takvimi sekmesi kontrol ediliyor.");
        ctx.calendarPage().assertSekmeSecili();

        ReportManager.pass("Güncelleme Takvimi sekmesi görüntülendi.");
    }

    @Test
    public void donemSeciciCalisir() {
        TestContext ctx = testHazirligi();

        ReportManager.info("Dönem seçici kontrol ediliyor.");
        ctx.calendarPage().assertDonemSecili("30d");

        for (String donem : new String[]{"7d", "90d", "1y", "30d"}) {
            ctx.calendarFlow().donemSecme(donem);

            ctx.calendarPage().assertDonemSecili(donem);
            for (String diger : new String[]{"7d", "30d", "90d", "1y"}) {
                if (!diger.equals(donem)) {
                    ctx.calendarPage().assertDonemSeciliDegil(diger);
                }
            }
        }

        ReportManager.pass("Dönem seçici doğru çalışıyor.");
    }

    @Test
    public void ozetKartlariGoruntulenir() {
        TestContext ctx = testHazirligi();

        ReportManager.info("Özet kartları kontrol ediliyor.");
        for (String baslik : new String[]{"Başarılı Güncelleme", "Başarısız Güncelleme", "Önümüzdeki 7 Gün"}) {
            ctx.calendarPage().assertOzetKartiSayisal(baslik);
        }
        ctx.calendarPage().assertOzetKartiDolu("Ortalama Süre");

        ReportManager.pass("Özet kartları doğru görüntüleniyor.");
    }

    @Test
    public void ozetKartiBugunkuGuncellemeyiIcerir() {
        TestContext ctx = testHazirligi();

        ReportManager.info("Özet kartı bugünkü güncellemeyi içeriyor mu kontrol ediliyor.");
        ctx.calendarPage().assertOzetKartiEnAz1("Başarılı Güncelleme");

        ReportManager.pass("Özet kartı bugünkü güncellemeyi içeriyor.");
    }

    // ------------------------------------------------------------------
    // Takvim (ortak veri, salt okunur)
    // ------------------------------------------------------------------

    @Test
    public void takvimBugunuBasariliGuncellemeIleGosterir() {
        TestContext ctx = testHazirligi();

        int bugun = LocalDate.now(ZoneId.of("Europe/Istanbul")).getDayOfMonth();

        ReportManager.info("Veri seti: " + hazirVeriSeti);
        ctx.calendarPage().assertBugunBasariliGuncellemeVar(bugun);

        ReportManager.pass("Takvim bugünü başarılı güncelleme ile gösteriyor.");
    }

    @Test
    public void gunListesindeKayitGorunur() {
        TestContext ctx = testHazirligi();

        ReportManager.info("Veri seti: " + hazirVeriSeti);
        Assert.assertTrue(ctx.calendarPage().gunDetayindaKayitVarMi(hazirVeriSeti),
                "Seçili günün listesinde güncelleme kaydı yok: " + hazirVeriSeti);

        ReportManager.pass("Seçili günün listesinde güncelleme kaydı görünüyor.");
    }

    // ------------------------------------------------------------------
    // Güncelleme Planı (ortak veri, salt okunur)
    // ------------------------------------------------------------------

    @Test
    public void planTablosundaVeriSetiGorunur() {
        TestContext ctx = testHazirligi();

        ReportManager.info("Veri seti: " + hazirVeriSeti);
        ctx.calendarFlow().planAramaYapma(hazirVeriSeti);

        Assert.assertTrue(ctx.calendarPage().planListesindeVarMi(hazirVeriSeti),
                "Veri seti plan tablosunda görünmüyor.");

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(ctx.calendarPage().planSikligiAlma(hazirVeriSeti), "Manuel",
                "Plan sıklığı Manuel değil.");
        soft.assertEquals(ctx.calendarPage().planSonGuncellemeSurumuAlma(hazirVeriSeti), "v2",
                "Plan tablosundaki sürüm v2 değil.");
        soft.assertAll();

        ReportManager.pass("Veri seti plan tablosunda doğru bilgilerle görünüyor.");
    }

    // ------------------------------------------------------------------
    // İşlem Geçmişi (ortak veri, salt okunur)
    // ------------------------------------------------------------------

    @Test
    public void gecmisKaydiDogruBilgilerleOlusur() {
        TestContext ctx = testHazirligi();

        ReportManager.info("Veri seti: " + hazirVeriSeti);
        ctx.calendarFlow().gecmisiFiltrele(hazirVeriSeti);

        Assert.assertTrue(ctx.calendarPage().gecmisteVarMi(hazirVeriSeti),
                "Güncelleme geçmişte görünmüyor.");

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(ctx.calendarPage().gecmisSonucuAlma(hazirVeriSeti), "Başarılı",
                "Geçmiş sonucu Başarılı değil.");
        soft.assertEquals(ctx.calendarPage().gecmisTetikleyenAlma(hazirVeriSeti), "Manuel",
                "Tetikleyen Manuel değil.");
        soft.assertEquals(ctx.calendarPage().gecmisSurumuAlma(hazirVeriSeti), "v2",
                "Geçmişteki sürüm v2 değil.");
        soft.assertTrue(ctx.calendarPage().gecmisKayitSayisiAlma(hazirVeriSeti).matches("\\d+"),
                "Geçmişteki kayıt sayısı sayısal değil.");
        soft.assertAll();

        ReportManager.pass("Güncelleme geçmişi kaydı doğru bilgilerle oluşmuş.");
    }

    @Test
    public void gecmisSonucFiltresiCalisir() {
        TestContext ctx = testHazirligi();

        ReportManager.info("Veri seti: " + hazirVeriSeti);
        ctx.calendarFlow().gecmisiFiltrele(hazirVeriSeti);

        ctx.calendarFlow().gecmisSonucFiltresiSecme("SUCCEEDED");
        Assert.assertTrue(ctx.calendarPage().gecmisteVarMi(hazirVeriSeti),
                "Başarılı filtresinde kayıt görünmüyor.");

        ctx.calendarFlow().gecmisSonucFiltresiSecme("FAILED");
        ctx.calendarPage().assertGecmisteKayitYok(hazirVeriSeti);

        ctx.calendarFlow().gecmisSonucFiltresiSecme("ALL");
        Assert.assertTrue(ctx.calendarPage().gecmisteVarMi(hazirVeriSeti),
                "Tümü filtresinde kayıt görünmüyor.");

        ReportManager.pass("Geçmiş sonuç filtresi doğru çalışıyor.");
    }

    // ------------------------------------------------------------------
    // Güncelleme Ayrıntısı çekmecesi (ortak veri, salt okunur)
    // ------------------------------------------------------------------

    @Test
    public void detayCekmecesiBilgileriDogru() {
        TestContext ctx = testHazirligi();

        ReportManager.info("Veri seti: " + hazirVeriSeti);
        ctx.calendarFlow().gunKaydiniAcma(hazirVeriSeti);

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(ctx.calendarPage().detayVeriSetiAdiAlma(), hazirVeriSeti,
                "Çekmecede farklı veri seti görüntüleniyor.");
        soft.assertEquals(ctx.calendarPage().detaySonucAlma(), "Başarılı",
                "Güncelleme sonucu Başarılı değil.");
        soft.assertTrue(ctx.calendarPage().detayAlanDegeriAlma("Tetikleyen").startsWith("Manuel"),
                "Tetikleyen Manuel değil.");
        soft.assertEquals(ctx.calendarPage().detayAlanDegeriAlma("Oluşan sürüm"), "v2",
                "Oluşan sürüm v2 değil.");
        soft.assertTrue(ctx.calendarPage().detayAlanDegeriAlma("İşlenen kayıt").matches("\\d+"),
                "İşlenen kayıt sayısal değil.");
        soft.assertEquals(ctx.calendarPage().detayAlanDegeriAlma("Deneme"), "1",
                "Deneme sayısı 1 değil.");

        String akis = ctx.calendarPage().detayAkisMetniAlma();
        soft.assertTrue(akis.contains("Tamamlandı"), "Akışta 'Tamamlandı' adımı yok.");
        soft.assertTrue(akis.contains("Aktarıldı"), "Akışta portala 'Aktarıldı' adımı yok.");
        soft.assertAll();

        ReportManager.pass("Güncelleme ayrıntısı bilgileri doğru.");
    }

    @Test
    public void detayVeriSetineGitDogruSayfayiAcar() {
        TestContext ctx = testHazirligi();

        ReportManager.info("Veri seti: " + hazirVeriSeti);
        ctx.calendarFlow().gunKaydiniAcma(hazirVeriSeti);
        ctx.calendarFlow().detaydanVeriSetineGitme();

        ctx.calendarPage().assertDetayBaslik(hazirVeriSeti);

        ReportManager.pass("Veri setine git doğru detay sayfasını açıyor.");
    }

    @Test
    public void detaySayfasindaGuncellemeYeniVersiyonOlusturmus() {
        TestContext ctx = testHazirligi();

        ReportManager.info("Veri seti: " + hazirVeriSeti);
        ctx.calendarFlow().gunKaydiniAcma(hazirVeriSeti);
        ctx.calendarFlow().detaydanVeriSetineGitme();

        ctx.calendarPage().assertDetayBaslik(hazirVeriSeti);

        ctx.calendarFlow().detaySekmesineGecme("Versiyonlar");

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(ctx.calendarPage().versiyonDurumuAlma("v2"), "Aktif", "v2 Aktif değil.");
        soft.assertEquals(ctx.calendarPage().versiyonDurumuAlma("v1"), "Arşiv", "v1 Arşiv durumuna geçmedi.");
        soft.assertAll();

        ReportManager.pass("Güncelleme yeni versiyon oluşturmuş: v2 Aktif, v1 Arşiv.");
    }

    @Test
    public void detayCekmecesiKapatilabilir() {
        TestContext ctx = testHazirligi();

        ReportManager.info("Veri seti: " + hazirVeriSeti);
        ctx.calendarFlow().gunKaydiniAcma(hazirVeriSeti);
        ctx.calendarFlow().detayiKapatma();
        ctx.calendarPage().assertDetayCekmecesiKapali();

        ReportManager.pass("Güncelleme ayrıntısı çekmecesi kapatıldı.");
    }

    // ------------------------------------------------------------------
    // Veriyi değiştiren test: kendi veri setini kullanır
    // ------------------------------------------------------------------

    @Test
    public void manuelGuncellemeGecmiseYansir() {

        TestContext ctx = nesneleriOlusturma();

        String dataSetName = ctx.setupFlow().yayindaVeriSetiOlusturma("otomasyon_takvim_manuel");
        ReportManager.info("Veri seti: " + dataSetName);

        ctx.calendarPage().guncellemeTakvimiSekmesineGecme();
        ctx.calendarFlow().planAramaYapma(dataSetName);
        Assert.assertTrue(ctx.calendarPage().planListesindeVarMi(dataSetName),
                "Veri seti plan tablosunda görünmüyor.");
        int oncekiSurum = ctx.calendarPage().surumNumarasi(
                ctx.calendarPage().planSonGuncellemeSurumuAlma(dataSetName));

        ctx.publishFlow().manuallyUpdateDataSet(dataSetName);
        ctx.publishPage().waitForPortalStatus(dataSetName, "Aktarıldı");

        ctx.calendarPage().gecmisteKayitBekleme(dataSetName);

        Assert.assertTrue(ctx.calendarPage().gecmisteVarMi(dataSetName),
                "Güncelleme geçmişte görünmüyor.");

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(ctx.calendarPage().gecmisSonucuAlma(dataSetName), "Başarılı",
                "Geçmiş kaydı başarılı görünmüyor.");
        soft.assertEquals(ctx.calendarPage().gecmisTetikleyenAlma(dataSetName), "Manuel",
                "Tetikleyen Manuel görünmüyor.");
        soft.assertEquals(
                ctx.calendarPage().surumNumarasi(ctx.calendarPage().gecmisSurumuAlma(dataSetName)),
                oncekiSurum + 1,
                "Geçmişteki sürüm 1 artmadı.");
        soft.assertAll();

        ctx.calendarFlow().planAramaYapma(dataSetName);
        Assert.assertTrue(ctx.calendarPage().planListesindeVarMi(dataSetName),
                "Veri seti plan tablosunda görünmüyor.");
        Assert.assertEquals(
                ctx.calendarPage().surumNumarasi(ctx.calendarPage().planSonGuncellemeSurumuAlma(dataSetName)),
                oncekiSurum + 1,
                "Plan tablosundaki sürüm güncellenmedi.");

        ReportManager.pass("Manuel güncelleme geçmişe ve plan tablosuna yansıdı.");
    }
}