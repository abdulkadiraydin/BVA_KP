package com.bvakp.automation.tests.openData;

import com.bvakp.automation.base.AvpBaseTest;
import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.flows.OpenDataCreateFlow;
import com.bvakp.automation.flows.OpenDataPublishFlow;
import com.bvakp.automation.flows.OpenDataSetupFlow;
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
        new OpenDataPage(page()).clickAcikVeriPortali();

        OpenDataPublishPage publishPage = new OpenDataPublishPage(page());
        OpenDataPublishFlow publishFlow = new OpenDataPublishFlow(publishPage);
        OpenDataCreateFlow createFlow = new OpenDataCreateFlow(page());

        return new TestContext(
                new OpenDataUpdateCalendarPage(page()),
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
     * @BeforeMethod'dan önce çalıştığı için kendi Playwright oturumunu açıp kapatır.
     */
    @BeforeClass
    public void ortakVeriSetiHazirlama() {

        PlaywrightManager.initialize(authStateKullan());
        try {
            TestContext ctx = nesneleriOlusturma();

            hazirVeriSeti = ctx.setupFlow().yayindaVeriSetiOlusturma("otomasyon_takvim_ortak");

            ctx.publishPage().clickPublishTab();
            ctx.publishFlow().manuallyUpdateDataSet(hazirVeriSeti);
            ctx.publishPage().waitForPortalStatus(hazirVeriSeti, "Aktarıldı");

            // Testler başlamadan geçmiş kaydı oluşmuş olsun
            ctx.calendarPage().gecmisteKayitBekleme(hazirVeriSeti, 60_000);

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

        Assert.assertTrue(
                ctx.calendarPage().guncellemeTakvimiSekmesiSeciliMi(),
                "Güncelleme Takvimi sekmesi seçili değil.");
    }

    @Test
    public void donemSeciciCalisir() {
        TestContext ctx = testHazirligi();

        Assert.assertTrue(ctx.calendarPage().donemSeciliMi("30d"),
                "Varsayılan dönem Son 30 gün değil.");

        for (String donem : new String[]{"7d", "90d", "1y", "30d"}) {
            ReportManager.step("Dönem seçiliyor: " + donem);
            ctx.calendarPage().donemSecme(donem);

            SoftAssert soft = new SoftAssert();
            soft.assertTrue(ctx.calendarPage().donemSeciliMi(donem),
                    donem + " dönemi seçili görünmüyor.");
            for (String diger : new String[]{"7d", "30d", "90d", "1y"}) {
                if (!diger.equals(donem)) {
                    soft.assertFalse(ctx.calendarPage().donemSeciliMi(diger),
                            donem + " seçiliyken " + diger + " de seçili görünüyor.");
                }
            }
            soft.assertAll();
        }
    }

    @Test
    public void ozetKartlariGoruntulenir() {
        TestContext ctx = testHazirligi();

        SoftAssert soft = new SoftAssert();
        for (String baslik : new String[]{"Başarılı Güncelleme", "Başarısız Güncelleme", "Önümüzdeki 7 Gün"}) {
            soft.assertTrue(ctx.calendarPage().ozetKartiDegeriAlma(baslik).matches("\\d+"),
                    baslik + " kartı sayısal değer göstermiyor.");
        }
        soft.assertFalse(ctx.calendarPage().ozetKartiDegeriAlma("Ortalama Süre").isBlank(),
                "Ortalama Süre kartı boş.");
        soft.assertAll();
    }

    @Test
    public void ozetKartiBugunkuGuncellemeyiIcerir() {
        TestContext ctx = testHazirligi();

        int basarili = Integer.parseInt(ctx.calendarPage().ozetKartiDegeriAlma("Başarılı Güncelleme"));
        Assert.assertTrue(basarili >= 1,
                "Son 30 günde başarılı güncelleme sayısı en az 1 olmalı. Görünen: " + basarili);
    }

    // ------------------------------------------------------------------
    // Takvim (ortak veri, salt okunur)
    // ------------------------------------------------------------------

    @Test
    public void takvimBugunuBasariliGuncellemeIleGosterir() {
        TestContext ctx = testHazirligi();

        int bugun = LocalDate.now(ZoneId.of("Europe/Istanbul")).getDayOfMonth();

        Assert.assertTrue(ctx.calendarPage().takvimGunuBasariliSayisi(bugun) >= 1,
                "Takvimde bugün için başarılı güncelleme görünmüyor. Özet: "
                        + ctx.calendarPage().takvimGunuOzetiAlma(bugun));
    }

    @Test
    public void gunListesindeKayitGorunur() {
        TestContext ctx = testHazirligi();

        Assert.assertTrue(ctx.calendarPage().gunDetayindaKayitVarMi(hazirVeriSeti),
                "Seçili günün listesinde güncelleme kaydı yok: " + hazirVeriSeti);
    }

    // ------------------------------------------------------------------
    // Güncelleme Planı (ortak veri, salt okunur)
    // ------------------------------------------------------------------

    @Test
    public void planTablosundaVeriSetiGorunur() {
        TestContext ctx = testHazirligi();

        ctx.calendarPage().planAramaYapma(hazirVeriSeti);

        Assert.assertTrue(ctx.calendarPage().planListesindeVarMi(hazirVeriSeti),
                "Veri seti plan tablosunda görünmüyor.");

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(ctx.calendarPage().planSikligiAlma(hazirVeriSeti), "Manuel",
                "Plan sıklığı Manuel değil.");
        soft.assertEquals(ctx.calendarPage().planSonGuncellemeSurumuAlma(hazirVeriSeti), "v2",
                "Plan tablosundaki sürüm v2 değil.");
        soft.assertAll();
    }

    // ------------------------------------------------------------------
    // İşlem Geçmişi (ortak veri, salt okunur)
    // ------------------------------------------------------------------

    @Test
    public void gecmisKaydiDogruBilgilerleOlusur() {
        TestContext ctx = testHazirligi();

        ctx.calendarPage().gecmisVeriSetiFiltreleme(hazirVeriSeti);

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
    }

    @Test
    public void gecmisSonucFiltresiCalisir() {
        TestContext ctx = testHazirligi();

        ctx.calendarPage().gecmisVeriSetiFiltreleme(hazirVeriSeti);

        ReportManager.step("Başarılı filtresinde kayıt görünmeli.");
        ctx.calendarPage().gecmisSonucFiltresiSecme("SUCCEEDED");
        Assert.assertTrue(ctx.calendarPage().gecmisteVarMi(hazirVeriSeti),
                "Başarılı filtresinde kayıt görünmüyor.");

        ReportManager.step("Başarısız filtresinde kayıt görünmemeli.");
        ctx.calendarPage().gecmisSonucFiltresiSecme("FAILED");
        Assert.assertTrue(ctx.calendarPage().gecmisteKayitYokMu(hazirVeriSeti),
                "Başarısız filtresinde başarılı kayıt görünüyor.");

        ReportManager.step("Tümü filtresinde kayıt tekrar görünmeli.");
        ctx.calendarPage().gecmisSonucFiltresiSecme("ALL");
        Assert.assertTrue(ctx.calendarPage().gecmisteVarMi(hazirVeriSeti),
                "Tümü filtresinde kayıt görünmüyor.");
    }

    // ------------------------------------------------------------------
    // Güncelleme Ayrıntısı çekmecesi (ortak veri, salt okunur)
    // ------------------------------------------------------------------

    @Test
    public void detayCekmecesiBilgileriDogru() {
        TestContext ctx = testHazirligi();

        ctx.calendarPage().gunListesindenKayitAcma(hazirVeriSeti);

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
    }

    @Test
    public void detayVeriSetineGitDogruSayfayiAcar() {
        TestContext ctx = testHazirligi();

        ctx.calendarPage().gunListesindenKayitAcma(hazirVeriSeti);

        ReportManager.step("Veri setine git tıklanıyor.");
        ctx.calendarPage().detaydaVeriSetineGitTiklama();

        Assert.assertEquals(ctx.calendarPage().detayBaslikAlma(), hazirVeriSeti,
                "Doğru veri setinin detay sayfası açılmadı.");
    }

    @Test
    public void detaySayfasindaGuncellemeYeniVersiyonOlusturmus() {
        TestContext ctx = testHazirligi();

        ctx.calendarPage().gunListesindenKayitAcma(hazirVeriSeti);
        ctx.calendarPage().detaydaVeriSetineGitTiklama();

        Assert.assertEquals(ctx.calendarPage().detayBaslikAlma(), hazirVeriSeti,
                "Doğru veri setinin detay sayfası açılmadı.");

        ctx.calendarPage().detaySekmesineGecme("Versiyonlar");

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(ctx.calendarPage().versiyonDurumuAlma("v2"), "Aktif", "v2 Aktif değil.");
        soft.assertEquals(ctx.calendarPage().versiyonDurumuAlma("v1"), "Arşiv", "v1 Arşiv durumuna geçmedi.");
        soft.assertAll();
    }

    @Test
    public void detayCekmecesiKapatilabilir() {
        TestContext ctx = testHazirligi();

        ctx.calendarPage().gunListesindenKayitAcma(hazirVeriSeti);
        ctx.calendarPage().detayiKapatma();

        Assert.assertTrue(ctx.calendarPage().detayCekmecesiKapaliMi(),
                "Kapat sonrası detay çekmecesi hâlâ açık.");
    }

    // ------------------------------------------------------------------
    // Veriyi değiştiren test: kendi veri setini kullanır
    // ------------------------------------------------------------------

    @Test
    public void manuelGuncellemeGecmiseYansir() {

        TestContext ctx = nesneleriOlusturma();

        String dataSetName = ctx.setupFlow().yayindaVeriSetiOlusturma("otomasyon_takvim_manuel");

        ReportManager.step("Güncelleme öncesi sürüm takvim sayfasından okunuyor.");
        ctx.calendarPage().guncellemeTakvimiSekmesineGecme();
        ctx.calendarPage().planAramaYapma(dataSetName);
        Assert.assertTrue(ctx.calendarPage().planListesindeVarMi(dataSetName),
                "Veri seti plan tablosunda görünmüyor.");
        int oncekiSurum = ctx.calendarPage().surumNumarasi(
                ctx.calendarPage().planSonGuncellemeSurumuAlma(dataSetName));

        ReportManager.step("Veri Seti Yönetimi: manuel güncelleme yapılıyor.");
        ctx.publishPage().clickPublishTab();
        ctx.publishFlow().manuallyUpdateDataSet(dataSetName);
        ctx.publishPage().waitForPortalStatus(dataSetName, "Aktarıldı");

        ReportManager.step("Güncelleme Takvimi: işlem geçmişi doğrulanıyor.");
        ctx.calendarPage().gecmisteKayitBekleme(dataSetName, 60_000);
        ctx.calendarPage().gecmisVeriSetiFiltreleme(dataSetName);

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

        ctx.calendarPage().planAramaYapma(dataSetName);
        Assert.assertTrue(ctx.calendarPage().planListesindeVarMi(dataSetName),
                "Veri seti plan tablosunda görünmüyor.");
        Assert.assertEquals(
                ctx.calendarPage().surumNumarasi(ctx.calendarPage().planSonGuncellemeSurumuAlma(dataSetName)),
                oncekiSurum + 1,
                "Plan tablosundaki sürüm güncellenmedi.");
    }
}