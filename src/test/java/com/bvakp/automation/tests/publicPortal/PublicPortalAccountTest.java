package com.bvakp.automation.tests.publicPortal;

import com.bvakp.automation.api.core.ApiYanit;
import com.bvakp.automation.api.publicportal.PublicPortalApi;
import com.bvakp.automation.base.BaseTest;
import com.bvakp.automation.core.auth.PortalRol;
import com.bvakp.automation.pages.publicPortal.PublicPortalAccountPage;
import com.bvakp.automation.pages.publicPortal.PublicPortalLoginPage;
import com.bvakp.automation.reporting.ReportManager;
import com.bvakp.automation.utils.TestDataUtil;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Kamuya açık portalda Hesabım testleri: veri talebi, talep yönetimi ve API anahtarları.
 *
 * <p>Onay testleri dışındaki bütün testler talep rolüyle ({@link PortalRol#TALEP}) çalışır; onay
 * testlerinde yönetici rolüne ({@link PortalRol#ONAY}) geçilir. Her test kendi verisini oluşturur (talep başlıkları ve
 * anahtar adları "otomasyon_" ile başlar). Oluşturulan API anahtarları test sonunda iptal edilir;
 * anahtar değeri hiçbir zaman log'a ve rapora yazılmaz.
 */
public class PublicPortalAccountTest extends BaseTest {

  private static final String TALEP_AMACI = "Otomasyon testi kapsamında oluşturulan talep.";
  private static final String TALEP_KATEGORISI = "Ulaşım";
  private static final String TALEP_ALINDI = "Alındı";

  /** Tek bir testin kullandığı sayfa nesneleri. */
  private record TestContext(PublicPortalLoginPage loginPage, PublicPortalAccountPage hesapPage) {}

  /** Talep kullanıcısıyla giriş yapar ve Hesabım sayfasını açar. */
  private TestContext hesabimAcikHazirlik() {

    PublicPortalLoginPage loginPage = new PublicPortalLoginPage(page());
    PublicPortalAccountPage hesapPage = new PublicPortalAccountPage(page());
    String kullanici = PortalRol.TALEP.kullaniciAdi();

    ReportManager.step("Açık portala giriş yapılıyor: " + kullanici);
    loginPage.rolIleGirisYapma(PortalRol.TALEP);
    Assert.assertTrue(loginPage.girisYapildiMi(kullanici), "Ön koşul: giriş yapılamadı.");

    hesapPage.hesabimAcma(kullanici);
    Assert.assertTrue(hesapPage.hesabimAcikMi(kullanici), "Ön koşul: Hesabım açılmadı.");

    return new TestContext(loginPage, hesapPage);
  }

  // =====================================================================
  // Veri talebi
  // =====================================================================

  /** Oluşturulan talebin Taleplerim'de "Alındı" durumunda ve herkese açık göründüğünü doğrular. */
  @Test
  public void talepOlusturma() {

    TestContext ctx = hesabimAcikHazirlik();
    String baslik = TestDataUtil.dinamikAdOlusturma("otomasyon_talep");

    talepGonderme(ctx, baslik, true);
    Assert.assertTrue(
        ctx.hesapPage().talepAlindiMesajiGorunurMu(), "\"Talebiniz alındı\" mesajı görünmedi.");

    ctx.hesapPage().sekmeAcma("Taleplerim");
    Assert.assertTrue(
        ctx.hesapPage().talepListedeMi(baslik), "Talep Taleplerim'de bulunamadı: " + baslik);
    Assert.assertEquals(
        ctx.hesapPage().talepDurumuOkuma(baslik), TALEP_ALINDI, "Talep durumu farklı.");
    Assert.assertTrue(
        ctx.hesapPage().talepBilgisiOkuma(baslik).contains("Herkese açık"),
        "Talep herkese açık görünmüyor: " + ctx.hesapPage().talepBilgisiOkuma(baslik));

    ReportManager.pass("Talep oluşturuldu ve Taleplerim'de görünüyor: " + baslik);
  }

  /** Başlık boşken talep formunun gönderilmediğini doğrular. */
  @Test
  public void talepBasligiZorunludur() {

    TestContext ctx = hesabimAcikHazirlik();

    ctx.hesapPage().talepFormunuAcma();
    ctx.hesapPage().talepBilgileriniGirme("", TALEP_AMACI, null, true);
    ctx.hesapPage().talebiGonderme();

    Assert.assertTrue(
        ctx.hesapPage().talepBasligiEksikMi(), "Başlık zorunlu alan olarak işaretlenmedi.");
    Assert.assertTrue(ctx.hesapPage().talepFormuAcikMi(), "Başlık boşken form gönderildi.");

    ReportManager.pass("Başlık boşken talep gönderilmedi.");
  }

  /** Görünürlük senaryoları: senaryo adı, "herkese açık" işaretli mi, Taleplerim'deki metin. */
  @DataProvider(name = "talepGorunurlukleri")
  public Object[][] talepGorunurlukleri() {
    return new Object[][] {
      {"Herkese açık", true, "Herkese açık"},
      {"Gizli", false, "Yalnız size görünür"}
    };
  }

  /** "Herkese açık" seçiminin Taleplerim'de doğru görünürlükle gösterildiğini doğrular. */
  @Test(dataProvider = "talepGorunurlukleri")
  public void talepGorunurlugu(String senaryo, boolean herkeseAcik, String beklenenMetin) {

    TestContext ctx = hesabimAcikHazirlik();
    String baslik = TestDataUtil.dinamikAdOlusturma("otomasyon_talep");

    talepGonderme(ctx, baslik, herkeseAcik);
    ctx.hesapPage().sekmeAcma("Taleplerim");

    Assert.assertTrue(
        ctx.hesapPage().talepListedeMi(baslik), "Talep Taleplerim'de bulunamadı: " + baslik);
    String bilgi = ctx.hesapPage().talepBilgisiOkuma(baslik);
    Assert.assertTrue(
        bilgi.contains(beklenenMetin),
        senaryo + ": görünürlük \"" + beklenenMetin + "\" olmalıydı | " + bilgi);

    ReportManager.pass("Talep görünürlüğü doğru: " + senaryo);
  }

  /** Vazgeç ile kapatılan formdan talep oluşmadığını doğrular. */
  @Test
  public void talepVazgecme() {

    TestContext ctx = hesabimAcikHazirlik();
    String baslik = TestDataUtil.dinamikAdOlusturma("otomasyon_talep");

    ctx.hesapPage().talepFormunuAcma();
    ctx.hesapPage().talepBilgileriniGirme(baslik, TALEP_AMACI, TALEP_KATEGORISI, true);
    ctx.hesapPage().talepVazgecme();

    Assert.assertTrue(ctx.hesapPage().talepFormuKapandiMi(), "Vazgeç'ten sonra form kapanmadı.");
    ctx.hesapPage().sekmeAcma("Taleplerim");
    Assert.assertTrue(
        ctx.hesapPage().talepListedeYokMu(baslik), "Vazgeçilen talep oluşturulmuş: " + baslik);

    ReportManager.pass("Vazgeçilen talep oluşturulmadı.");
  }

  /** Başlıktaki HTML etiketlerinin işlenmeden düz metin olarak gösterildiğini doğrular. */
  @Test
  public void talepBasligindakiHtmlIslenmez() {

    TestContext ctx = hesabimAcikHazirlik();
    String isaret = TestDataUtil.dinamikAdOlusturma("otomasyon_talep_html");
    String hamBaslik = "<b>" + isaret + "</b>";

    talepGonderme(ctx, hamBaslik, true);
    ctx.hesapPage().sekmeAcma("Taleplerim");

    Assert.assertTrue(
        ctx.hesapPage().talepBasligiDuzMetinMi(isaret, hamBaslik),
        "Talep başlığındaki HTML işlendi ya da talep bulunamadı: " + isaret);

    ReportManager.pass("Talep başlığındaki HTML düz metin olarak gösterildi.");
  }

  /** Talep formunu açar, doldurur ve gönderir. */
  private void talepGonderme(TestContext ctx, String baslik, boolean herkeseAcik) {
    ReportManager.step("Veri talebi oluşturuluyor: " + baslik);
    ctx.hesapPage().talepFormunuAcma();
    Assert.assertTrue(ctx.hesapPage().talepFormuAcikMi(), "Ön koşul: talep formu açılmadı.");
    ctx.hesapPage().talepBilgileriniGirme(baslik, TALEP_AMACI, TALEP_KATEGORISI, herkeseAcik);
    ctx.hesapPage().talebiGonderme();
  }

  // =====================================================================
  // Talep yönetimi (onay kullanıcısı gelince yazılacak)
  //
  // Akış: rolIleGirisYapma(TALEP) → talep oluştur → rolIleGirisYapma(ONAY) → yonetimeGitme()
  // → Veri Talepleri → onayla/reddet → rolIleGirisYapma(TALEP) → Taleplerim'de durum.
  // =====================================================================

  /** Yöneticinin talebi onayladığını ve kullanıcının durumu gördüğünü doğrular. */
  @Test
  public void talepOnaylama() {
    throw new SkipException("Talep onay ekranı henüz belli değil; onay kullanıcısı bekleniyor.");
  }

  /** Yöneticinin talebi reddettiğini ve kullanıcının durumu gördüğünü doğrular. */
  @Test
  public void talepReddetme() {
    throw new SkipException("Talep red ekranı henüz belli değil; onay kullanıcısı bekleniyor.");
  }

  // =====================================================================
  // API anahtarları
  // =====================================================================

  /** Oluşturulan anahtarın değerinin gösterildiğini ve listede göründüğünü doğrular. */
  @Test
  public void apiAnahtariOlusturma() {

    TestContext ctx = hesabimAcikHazirlik();
    String anahtarAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_anahtar");

    try {
      anahtarOlusturma(ctx, anahtarAdi);
      Assert.assertTrue(
          ctx.hesapPage().anahtarListedeMi(anahtarAdi), "Anahtar listede yok: " + anahtarAdi);

      ReportManager.pass("API anahtarı oluşturuldu: " + anahtarAdi);
    } finally {
      anahtarIptalEtme(ctx, anahtarAdi);
    }
  }

  /** Anahtar değerinin sayfa yenilendikten sonra bir daha gösterilmediğini doğrular. */
  @Test
  public void apiAnahtariYalnizBirKezGosterilir() {

    TestContext ctx = hesabimAcikHazirlik();
    String anahtarAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_anahtar");

    try {
      anahtarOlusturma(ctx, anahtarAdi);
      ctx.hesapPage().sayfayiYenileme();

      Assert.assertFalse(
          ctx.hesapPage().anahtarDegeriGorunuyorMu(),
          "Anahtar değeri sayfa yenilendikten sonra hâlâ görünüyor.");
      Assert.assertTrue(
          ctx.hesapPage().anahtarListedeMi(anahtarAdi), "Anahtar listede yok: " + anahtarAdi);

      ReportManager.pass("Anahtar değeri yalnız oluşturulduğu anda gösterildi.");
    } finally {
      anahtarIptalEtme(ctx, anahtarAdi);
    }
  }

  /** Oluşturulan anahtarla yetki gerektiren API çağrısının başarılı olduğunu doğrular. */
  @Test
  public void apiAnahtariIleApiCalisir() {

    TestContext ctx = hesabimAcikHazirlik();
    String anahtarAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_anahtar");

    try {
      String anahtar = anahtarOlusturma(ctx, anahtarAdi);

      ApiYanit yanit =
          PublicPortalApi.portaldan()
              .anahtarIleAnahtarListesiGetir(anahtar, PortalRol.TALEP.kullaniciAdi());
      Assert.assertTrue(
          PublicPortalApi.apiBasariliMi(yanit),
          "Anahtarla API çağrısı başarısız | Durum: " + yanit.durumKodu());

      ReportManager.pass("API anahtarıyla yetkili çağrı başarılı.");
    } finally {
      anahtarIptalEtme(ctx, anahtarAdi);
    }
  }

  /** İptal edilen anahtarla API çağrısının reddedildiğini doğrular. */
  @Test
  public void iptalEdilenApiAnahtariCalismaz() {

    TestContext ctx = hesabimAcikHazirlik();
    String anahtarAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_anahtar");
    String kullanici = PortalRol.TALEP.kullaniciAdi();
    PublicPortalApi api = PublicPortalApi.portaldan();

    String anahtar = anahtarOlusturma(ctx, anahtarAdi);
    Assert.assertTrue(
        PublicPortalApi.apiBasariliMi(api.anahtarIleAnahtarListesiGetir(anahtar, kullanici)),
        "Ön koşul: anahtar iptal edilmeden önce çalışmadı.");

    ReportManager.step("Anahtar iptal ediliyor: " + anahtarAdi);
    ctx.hesapPage().anahtarIptalEtme(anahtarAdi);
    Assert.assertTrue(
        ctx.hesapPage().anahtarListedeYokMu(anahtarAdi),
        "İptal edilen anahtar listede duruyor: " + anahtarAdi);

    ApiYanit yanit = api.anahtarIleAnahtarListesiGetir(anahtar, kullanici);
    Assert.assertFalse(
        PublicPortalApi.apiBasariliMi(yanit),
        "İptal edilen anahtarla API çağrısı başarılı oldu | Durum: " + yanit.durumKodu());

    ReportManager.pass("İptal edilen anahtarla API çağrısı reddedildi: " + yanit.durumKodu());
  }

  /** API Anahtarları sekmesinde anahtar oluşturur ve değerini döndürür (değer loglanmaz). */
  private String anahtarOlusturma(TestContext ctx, String anahtarAdi) {
    ReportManager.step("API anahtarı oluşturuluyor: " + anahtarAdi);
    ctx.hesapPage().sekmeAcma("API Anahtarları");
    ctx.hesapPage().anahtarOlusturma(anahtarAdi);

    String anahtar = ctx.hesapPage().anahtarDegeriOkuma();
    Assert.assertFalse(anahtar.isEmpty(), "Ön koşul: oluşturulan anahtarın değeri gösterilmedi.");
    return anahtar;
  }

  /** Test sonunda anahtar hâlâ listedeyse iptal eder; temizlik hatası testi bozmaz. */
  private void anahtarIptalEtme(TestContext ctx, String anahtarAdi) {
    try {
      ctx.hesapPage().sekmeAcma("API Anahtarları");
      if (ctx.hesapPage().anahtarListedeMi(anahtarAdi)) {
        ctx.hesapPage().anahtarIptalEtme(anahtarAdi);
        ReportManager.info("Temizlik: API anahtarı iptal edildi: " + anahtarAdi);
      }
    } catch (RuntimeException e) {
      ReportManager.warn("Temizlik: API anahtarı iptal edilemedi: " + anahtarAdi);
    }
  }
}
