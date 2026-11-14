package com.bvakp.automation.tests.openData;

import com.bvakp.automation.api.publicportal.PublicPortalApi;
import com.bvakp.automation.base.AvpBaseTest;
import com.bvakp.automation.core.auth.PortalRol;
import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.flows.avpFlows.OpenDataCreateFlow;
import com.bvakp.automation.flows.avpFlows.OpenDataPublishFlow;
import com.bvakp.automation.flows.avpFlows.OpenDataSetupFlow;
import com.bvakp.automation.flows.avpFlows.OpenDataUpdateCalendarFlow;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.pages.openData.OpenDataPublishPage;
import com.bvakp.automation.pages.openData.OpenDataUpdateCalendarPage;
import com.bvakp.automation.reporting.ReportManager;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

/**
 * Güncelleme Takvimi sekmesi testleri.
 *
 * <p>Bütün testler, @BeforeClass'ta bir kez hazırlanan ortak veri setini kullanır (yayında ve bir
 * kez manuel güncellenmiş, v2). Manuel güncellemenin geçmişe, plana ve detay ekranına yansıması bu
 * veri üzerinden doğrulanır.
 *
 * <p>Testler ortak veri setini yalnızca okur; bu sayede paralel çalışabilirler. Bu sınıfa veriyi
 * değiştiren bir test eklenecekse kendi veri setini oluşturmalıdır.
 */
public class OpenDataUpdateCalendarTest extends AvpBaseTest {

  /** Türkçe biçimli tam sayı: "42", "1.234", "174.579". */
  private static final String SAYI_DESENI = "\\d{1,3}(\\.\\d{3})*";

  /** Dönem seçicideki dönemler, kısadan uzuna. */
  private static final List<String> DONEMLER = List.of("7d", "30d", "90d", "1y");

  /** Portal verisinin hazır olması için en fazla beklenecek süre. */
  private static final int PORTAL_BEKLEME_SN = 60;

  /**
   * @BeforeClass'ta yazılır, testler yalnızca okur. Yayında ve bir kez manuel güncellenmiş (v2).
   */
  private String hazirVeriSeti;

  /**
   * Ortak veri setinin kamuya açık portaldaki (API) toplam kayıt sayısı; @BeforeClass'ta okunur.
   */
  private int portalKayitSayisi;

  /** Tek bir testin kullandığı takvim, yayın ve hazırlık nesneleri. */
  private record TestContext(
      OpenDataUpdateCalendarPage calendarPage,
      OpenDataUpdateCalendarFlow calendarFlow,
      OpenDataPublishPage publishPage,
      OpenDataPublishFlow publishFlow,
      OpenDataSetupFlow setupFlow) {}

  // ------------------------------------------------------------------
  // Hazırlık
  // ------------------------------------------------------------------

  /** Oturumu açar, Açık Veri Portalına geçer ve test nesnelerini döndürür. */
  private TestContext nesneleriOlusturma() {

    avpOturumuHazirlama(PortalRol.HAZIRLAYAN);
    new OpenDataPage(page()).acikVeriPortalinaGitme();

    OpenDataPublishPage publishPage = new OpenDataPublishPage(page());
    OpenDataPublishFlow publishFlow = new OpenDataPublishFlow(publishPage);
    OpenDataUpdateCalendarPage calendarPage = new OpenDataUpdateCalendarPage(page());

    return new TestContext(
        calendarPage,
        new OpenDataUpdateCalendarFlow(calendarPage),
        publishPage,
        publishFlow,
        new OpenDataSetupFlow(new OpenDataCreateFlow(page()), publishPage, publishFlow));
  }

  /** Takvim sekmesi açık olarak başlayan test hazırlığı. */
  private TestContext testHazirligi() {
    TestContext ctx = nesneleriOlusturma();
    ctx.calendarPage().takvimSekmesineGecme();
    return ctx;
  }

  /**
   * Ortak veri seti: oluştur, yayınla, bir kez manuel güncelle (v2). Sınıftaki testlerden önce bir
   * kez çalışır. BaseTest'in her test öncesi oturumu bu aşamada henüz açılmadığı için kendi
   * Playwright oturumunu açıp kapatır.
   */
  @BeforeClass
  public void ortakVeriSetiHazirlama() {

    PlaywrightManager.initialize(authStateKullan());
    boolean basarili = false;

    try {
      TestContext ctx = nesneleriOlusturma();

      hazirVeriSeti = ctx.setupFlow().yayindaVeriSetiOlusturma("otomasyon_takvim_ortak");

      ctx.publishFlow().manuallyUpdateDataSet(hazirVeriSeti);

      // Salt okunur testler, güncelleme tamamen bitmiş haliyle çalışsın.
      Assert.assertTrue(
          ctx.calendarPage().gecmisteSonucaUlastiMi(hazirVeriSeti, "Başarılı"),
          "Ön koşul: manuel güncelleme geçmişte Başarılı olmadı.");

      // Takvimdeki kayıt sayıları portaldaki gerçek kayıt sayısıyla karşılaştırılır.
      PublicPortalApi api = PublicPortalApi.portaldan();
      JsonNode paket = api.paketHazirOlanaKadarBekleme(hazirVeriSeti, PORTAL_BEKLEME_SN);
      String kaynakId = PublicPortalApi.datastoreKaynakId(paket).orElseThrow();
      portalKayitSayisi = portalKayitSayisiniBekleme(api, kaynakId);

      ReportManager.info(
          "Ortak veri seti hazır: "
              + hazirVeriSeti
              + " | Portal kayıt sayısı: "
              + portalKayitSayisi);
      basarili = true;

    } finally {
      // Hazırlık patlarsa trace/video saklansın diye statü HATALI olarak verilir.
      PlaywrightManager.close("ortakVeriSetiHazirlama", basarili ? "HAZIRLIK" : "HATALI");
    }
  }

  /**
   * Portaldaki kayıt sayısını döndürür. Manuel güncellemeden sonra portal veriyi yeniden yüklerken
   * toplam bir süre 0 görünür; bu yüzden sayı dolana kadar 5 saniyede bir tekrar sorulur.
   */
  private static int portalKayitSayisiniBekleme(PublicPortalApi api, String kaynakId) {
    long bitis = System.currentTimeMillis() + PORTAL_BEKLEME_SN * 1_000L;
    int toplam = api.kayitlariGetir(kaynakId, 1).toplam();
    while (toplam == 0 && System.currentTimeMillis() < bitis) {
      try {
        Thread.sleep(5_000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
      toplam = api.kayitlariGetir(kaynakId, 1).toplam();
    }
    Assert.assertTrue(
        toplam > 0, "Ön koşul: portalda kayıtlar " + PORTAL_BEKLEME_SN + " sn içinde yüklenmedi.");
    return toplam;
  }

  // ------------------------------------------------------------------
  // Sayfa genel yapısı
  // ------------------------------------------------------------------

  /** Güncelleme Takvimi sekmesinin açıldığını ve seçili olduğunu doğrular. */
  @Test
  public void sekmeGoruntuleme() {
    TestContext ctx = testHazirligi();

    Assert.assertTrue(
        ctx.calendarPage().sekmeSeciliMi(), "Güncelleme Takvimi sekmesi seçili değil.");

    ReportManager.pass("Güncelleme Takvimi sekmesi görüntülendi.");
  }

  /**
   * Dönem seçicide her seçimin tek başına seçili olduğunu ve verileri değiştirdiğini doğrular:
   * dönem uzadıkça Başarılı Güncelleme sayısı azalamaz; son 7 günde bugünkü güncelleme olduğu için
   * en az 1 olmalıdır.
   */
  @Test
  public void donemSeciciCalisir() {
    TestContext ctx = testHazirligi();

    Assert.assertTrue(ctx.calendarPage().donemSeciliMi("30d"), "Varsayılan dönem 30 gün değil.");

    List<Integer> basarililar = new ArrayList<>();

    for (String donem : DONEMLER) {
      ctx.calendarFlow().donemSecme(donem);

      Assert.assertTrue(ctx.calendarPage().donemSeciliMi(donem), donem + " seçili değil.");
      for (String diger : DONEMLER) {
        if (!diger.equals(donem)) {
          Assert.assertTrue(
              ctx.calendarPage().donemSeciliDegilMi(diger),
              donem + " seçiliyken " + diger + " de seçili görünüyor.");
        }
      }

      Assert.assertTrue(
          ctx.calendarPage().ozetKartiSayisalMi("Başarılı Güncelleme"),
          donem + ": Başarılı Güncelleme sayısal değil.");
      basarililar.add(ctx.calendarPage().ozetKartiSayisiAlma("Başarılı Güncelleme"));
    }

    ReportManager.info("Başarılı Güncelleme (7g, 30g, 90g, 1y): " + basarililar);

    Assert.assertTrue(basarililar.get(0) >= 1, "Son 7 günde başarılı güncelleme görünmüyor.");
    for (int i = 1; i < basarililar.size(); i++) {
      Assert.assertTrue(
          basarililar.get(i) >= basarililar.get(i - 1),
          "Dönem uzadığı halde Başarılı Güncelleme azaldı: " + basarililar);
    }

    ReportManager.pass("Dönem seçici doğru çalışıyor: " + basarililar);
  }

  /** Özet kartlarının sayısal değerlerle, Ortalama Süre kartının dolu görüntülendiğini doğrular. */
  @Test
  public void ozetKartlariGoruntulenir() {
    TestContext ctx = testHazirligi();

    for (String baslik :
        new String[] {"Başarılı Güncelleme", "Başarısız Güncelleme", "Önümüzdeki 7 Gün"}) {
      Assert.assertTrue(
          ctx.calendarPage().ozetKartiSayisalMi(baslik), baslik + " kartı sayısal değil.");
    }
    Assert.assertTrue(
        ctx.calendarPage().ozetKartiDoluMu("Ortalama Süre"), "Ortalama Süre kartı boş.");

    ReportManager.pass("Özet kartları doğru görüntüleniyor.");
  }

  /** Başarılı Güncelleme kartının bugünkü manuel güncellemeyi saydığını (en az 1) doğrular. */
  @Test
  public void ozetKartiBugunuIcerir() {
    TestContext ctx = testHazirligi();

    Assert.assertTrue(
        ctx.calendarPage().ozetKartiSayisalMi("Başarılı Güncelleme"),
        "Başarılı Güncelleme kartı sayısal değil.");
    Assert.assertTrue(
        ctx.calendarPage().ozetKartiSayisiAlma("Başarılı Güncelleme") >= 1,
        "Başarılı Güncelleme kartı bugünkü güncellemeyi saymıyor.");

    ReportManager.pass("Özet kartı bugünkü güncellemeyi içeriyor.");
  }

  // ------------------------------------------------------------------
  // Takvim
  // ------------------------------------------------------------------

  /** Takvimde bugünün başarılı güncelleme ile işaretlendiğini doğrular. */
  @Test
  public void takvimBugunuBasariliGosterir() {
    TestContext ctx = testHazirligi();

    int bugun = LocalDate.now(ZoneId.of("Europe/Istanbul")).getDayOfMonth();

    ReportManager.info("Veri seti: " + hazirVeriSeti);
    Assert.assertTrue(
        ctx.calendarPage().gunBasariliMi(bugun),
        "Takvimde bugün (" + bugun + ") başarılı güncelleme ile işaretli değil.");

    ReportManager.pass("Takvim bugünü başarılı güncelleme ile gösteriyor.");
  }

  /** Seçili günün listesinde ortak veri setinin güncelleme kaydının göründüğünü doğrular. */
  @Test
  public void gunListesindeKayitGorunur() {
    TestContext ctx = testHazirligi();

    ReportManager.info("Veri seti: " + hazirVeriSeti);
    Assert.assertTrue(
        ctx.calendarPage().gunDetayindaKayitVarMi(hazirVeriSeti),
        "Seçili günün listesinde güncelleme kaydı yok: " + hazirVeriSeti);

    ReportManager.pass("Seçili günün listesinde güncelleme kaydı görünüyor.");
  }

  // ------------------------------------------------------------------
  // Güncelleme Planı
  // ------------------------------------------------------------------

  /** Güncelleme Planında veri setinin Manuel sıklık ve v2 sürümle göründüğünü doğrular. */
  @Test
  public void planTablosundaVeriSetiGorunur() {
    TestContext ctx = testHazirligi();

    ReportManager.info("Veri seti: " + hazirVeriSeti);
    ctx.calendarFlow().planAramaYapma(hazirVeriSeti);

    Assert.assertTrue(
        ctx.calendarPage().planListesindeVarMi(hazirVeriSeti),
        "Veri seti plan tablosunda görünmüyor.");

    SoftAssert soft = new SoftAssert();
    soft.assertEquals(
        ctx.calendarPage().planSikligiAlma(hazirVeriSeti), "Manuel", "Plan sıklığı Manuel değil.");
    soft.assertEquals(
        ctx.calendarPage().planSonGuncellemeSurumuAlma(hazirVeriSeti),
        "v2",
        "Plan tablosundaki sürüm v2 değil (manuel güncelleme plana yansımadı).");
    soft.assertAll();

    ReportManager.pass("Veri seti plan tablosunda doğru bilgilerle görünüyor.");
  }

  // ------------------------------------------------------------------
  // İşlem Geçmişi
  // ------------------------------------------------------------------

  /**
   * İşlem Geçmişinde güncelleme kaydının Başarılı, Manuel, v2 ve portaldaki kayıt sayısıyla
   * oluştuğunu doğrular.
   */
  @Test
  public void gecmisKaydiDogruOlusur() {
    TestContext ctx = testHazirligi();

    ReportManager.info("Veri seti: " + hazirVeriSeti);
    ctx.calendarFlow().gecmisiFiltrele(hazirVeriSeti);

    Assert.assertTrue(
        ctx.calendarPage().gecmisteVarMi(hazirVeriSeti), "Güncelleme geçmişte görünmüyor.");

    SoftAssert soft = new SoftAssert();
    soft.assertEquals(
        ctx.calendarPage().gecmisSonucuAlma(hazirVeriSeti),
        "Başarılı",
        "Geçmiş sonucu Başarılı değil.");
    soft.assertEquals(
        ctx.calendarPage().gecmisTetikleyenAlma(hazirVeriSeti),
        "Manuel",
        "Tetikleyen Manuel değil.");
    soft.assertEquals(
        ctx.calendarPage().gecmisSurumuAlma(hazirVeriSeti),
        "v2",
        "Geçmişteki sürüm v2 değil (manuel güncelleme sürümü artırmadı).");
    String kayitSayisi = ctx.calendarPage().gecmisKayitSayisiAlma(hazirVeriSeti);
    soft.assertTrue(
        kayitSayisi.matches(SAYI_DESENI),
        "Geçmişteki kayıt sayısı sayısal değil: [" + kayitSayisi + "]");
    soft.assertEquals(
        sayiyaCevir(kayitSayisi),
        portalKayitSayisi,
        "Geçmişteki kayıt sayısı portaldaki kayıt sayısından farklı.");
    soft.assertAll();

    ReportManager.pass("Güncelleme geçmişi kaydı doğru bilgilerle oluşmuş.");
  }

  /**
   * İşlem Geçmişi sonuç filtresinin (Başarılı, Başarısız, Tümü) kayıtları doğru süzdüğünü doğrular.
   */
  @Test
  public void gecmisSonucFiltresiCalisir() {
    TestContext ctx = testHazirligi();

    ReportManager.info("Veri seti: " + hazirVeriSeti);
    ctx.calendarFlow().gecmisiFiltrele(hazirVeriSeti);

    ctx.calendarFlow().gecmisSonucFiltresiSecme("SUCCEEDED");
    Assert.assertTrue(
        ctx.calendarPage().gecmisteVarMi(hazirVeriSeti), "Başarılı filtresinde kayıt görünmüyor.");

    ctx.calendarFlow().gecmisSonucFiltresiSecme("FAILED");
    Assert.assertTrue(
        ctx.calendarPage().gecmisteYokMu(hazirVeriSeti),
        "Başarısız filtresinde başarılı güncelleme kaydı görünüyor.");

    ctx.calendarFlow().gecmisSonucFiltresiSecme("ALL");
    Assert.assertTrue(
        ctx.calendarPage().gecmisteVarMi(hazirVeriSeti), "Tümü filtresinde kayıt görünmüyor.");

    ReportManager.pass("Geçmiş sonuç filtresi doğru çalışıyor.");
  }

  // ------------------------------------------------------------------
  // Güncelleme Ayrıntısı çekmecesi
  // ------------------------------------------------------------------

  /**
   * Güncelleme Ayrıntısı çekmecesindeki sonuç, tetikleyen, sürüm, işlenen kayıt (portaldaki kayıt
   * sayısıyla) ve akış bilgilerini doğrular.
   */
  @Test
  public void detayCekmecesiBilgileriDogru() {
    TestContext ctx = testHazirligi();

    ReportManager.info("Veri seti: " + hazirVeriSeti);
    ctx.calendarFlow().gunKaydiniAcma(hazirVeriSeti);

    SoftAssert soft = new SoftAssert();
    soft.assertEquals(
        ctx.calendarPage().detayVeriSetiAdiAlma(),
        hazirVeriSeti,
        "Çekmecede farklı veri seti görüntüleniyor.");
    soft.assertEquals(
        ctx.calendarPage().detaySonucAlma(), "Başarılı", "Güncelleme sonucu Başarılı değil.");
    soft.assertTrue(
        ctx.calendarPage().detayAlanDegeriAlma("Tetikleyen").startsWith("Manuel"),
        "Tetikleyen Manuel değil.");
    soft.assertEquals(
        ctx.calendarPage().detayAlanDegeriAlma("Oluşan sürüm"), "v2", "Oluşan sürüm v2 değil.");
    String islenenKayit = ctx.calendarPage().detayAlanDegeriAlma("İşlenen kayıt");
    soft.assertTrue(
        islenenKayit.matches(SAYI_DESENI), "İşlenen kayıt sayısal değil: [" + islenenKayit + "]");
    soft.assertEquals(
        sayiyaCevir(islenenKayit),
        portalKayitSayisi,
        "İşlenen kayıt sayısı portaldaki kayıt sayısından farklı.");
    soft.assertEquals(
        ctx.calendarPage().detayAlanDegeriAlma("Deneme"), "1", "Deneme sayısı 1 değil.");

    String akis = ctx.calendarPage().detayAkisMetniAlma();
    soft.assertTrue(akis.contains("Tamamlandı"), "Akışta 'Tamamlandı' adımı yok.");
    soft.assertTrue(akis.contains("Aktarıldı"), "Akışta portala 'Aktarıldı' adımı yok.");
    soft.assertAll();

    ReportManager.pass("Güncelleme ayrıntısı bilgileri doğru.");
  }

  /**
   * Ayrıntı çekmecesindeki Veri setine git bağlantısının doğru detay sayfasını açtığını doğrular.
   */
  @Test
  public void veriSetineGitDogruSayfa() {
    TestContext ctx = testHazirligi();

    ReportManager.info("Veri seti: " + hazirVeriSeti);
    ctx.calendarFlow().gunKaydiniAcma(hazirVeriSeti);
    ctx.calendarFlow().detaydanVeriSetineGitme();

    Assert.assertTrue(
        ctx.calendarPage().detayBasligiDogruMu(hazirVeriSeti),
        "Veri setine git farklı bir sayfa açtı: " + hazirVeriSeti);

    ReportManager.pass("Veri setine git doğru detay sayfasını açıyor.");
  }

  /** Veri seti detayının Versiyonlar sekmesinde v2'nin Aktif, v1'in Arşiv olduğunu doğrular. */
  @Test
  public void detaydaYeniVersiyonVar() {
    TestContext ctx = testHazirligi();

    ReportManager.info("Veri seti: " + hazirVeriSeti);
    ctx.calendarFlow().gunKaydiniAcma(hazirVeriSeti);
    ctx.calendarFlow().detaydanVeriSetineGitme();

    Assert.assertTrue(
        ctx.calendarPage().detayBasligiDogruMu(hazirVeriSeti),
        "Veri setine git farklı bir sayfa açtı: " + hazirVeriSeti);

    ctx.calendarFlow().detaySekmesineGecme("Versiyonlar");

    SoftAssert soft = new SoftAssert();
    soft.assertEquals(ctx.calendarPage().versiyonDurumuAlma("v2"), "Aktif", "v2 Aktif değil.");
    soft.assertEquals(
        ctx.calendarPage().versiyonDurumuAlma("v1"), "Arşiv", "v1 Arşiv durumuna geçmedi.");
    soft.assertAll();

    ReportManager.pass("Güncelleme yeni versiyon oluşturmuş: v2 Aktif, v1 Arşiv.");
  }

  /** Güncelleme Ayrıntısı çekmecesinin kapatılabildiğini doğrular. */
  @Test
  public void detayCekmecesiKapatilabilir() {
    TestContext ctx = testHazirligi();

    ReportManager.info("Veri seti: " + hazirVeriSeti);
    ctx.calendarFlow().gunKaydiniAcma(hazirVeriSeti);
    ctx.calendarFlow().detayiKapatma();
    Assert.assertTrue(
        ctx.calendarPage().detayCekmecesiKapaliMi(), "Güncelleme ayrıntısı çekmecesi kapanmadı.");

    ReportManager.pass("Güncelleme ayrıntısı çekmecesi kapatıldı.");
  }

  // ------------------------------------------------------------------
  // Yardımcılar
  // ------------------------------------------------------------------

  /** Ekrandaki Türkçe biçimli sayıyı çevirir: "3.529" → 3529. */
  private static int sayiyaCevir(String metin) {
    return Integer.parseInt(metin.trim().replace(".", ""));
  }
}
