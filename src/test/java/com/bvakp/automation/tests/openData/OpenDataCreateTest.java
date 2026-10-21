package com.bvakp.automation.tests.openData;

import com.bvakp.automation.base.AvpBaseTest;
import com.bvakp.automation.components.KaynakDuzenleModal;
import com.bvakp.automation.components.KaynakEkleModal;
import com.bvakp.automation.components.KaynakSilModal;
import com.bvakp.automation.flows.avpFlows.OpenDataCreateFlow;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.reporting.ReportManager;
import com.bvakp.automation.utils.ScreenshotUtil;
import com.bvakp.automation.utils.TestDataUtil;
import com.microsoft.playwright.Page;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.nio.file.Path;

/**
 * Açık Veri Portalı kaynak yönetimi, hazır sorgu ve veri seti oluşturma testleri.
 *
 * <p>Her test kendi kaynağını oluşturur ve kendi TestContext'ini kullanır;
 * testler birbirine bağımlı değildir ve paralel çalışmaya uygundur.
 * Ön koşul kaynakları OpenDataCreateFlow ile oluşturulur; kaynak oluşturmanın
 * kendisi kaynakOlusturma testinde adım adım doğrulanır.
 */
public class OpenDataCreateTest extends AvpBaseTest {

    private static final String KAYNAK_TABLOSU = "arac_bakim_is_yeri_sayisi";
    private static final String SORUMLU_BIRIM = "İşletme Dairesi Başkanlığı";

    /** Tek bir testin kullandığı Page, Page Object'ler ve flow. */
    private record TestContext(
            Page page,
            OpenDataPage openDataPage,
            KaynakEkleModal kaynakEkleModal,
            OpenDataCreateFlow createFlow) {
    }

    /**
     * Her test için AVP oturumunu hazırlar, Açık Veri Portalına geçer ve
     * testin kullanacağı nesneleri oluşturur.
     */
    private TestContext testHazirligi() {

        avpOturumuHazirlama();

        Page aktifPage = page();
        OpenDataPage openDataPage = new OpenDataPage(aktifPage);
        KaynakEkleModal kaynakEkleModal = new KaynakEkleModal(aktifPage);

        ReportManager.step("Açık Veri Portalı açılıyor.");
        openDataPage.acikVeriPortalinaGitme();

        return new TestContext(aktifPage, openDataPage, kaynakEkleModal, new OpenDataCreateFlow(aktifPage));
    }

    // =====================================================================
    // Kaynak işlemleri
    // =====================================================================

    /** Kaynak ekleme alanlarını, veri önizlemesini ve kaydedilen kaynağı adım adım doğrular. */
    @Test
    public void kaynakOlusturma() {

        TestContext context = testHazirligi();
        OpenDataPage openDataPage = context.openDataPage();
        KaynakEkleModal kaynakEkleModal = context.kaynakEkleModal();

        String kaynakAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");
        String kaynakAciklamasi = "Otomasyon kaynak oluşturma testi - " + kaynakAdi;

        ReportManager.info("Test verisi | Kaynak: " + kaynakAdi + " | Tablo: " + KAYNAK_TABLOSU);

        Assert.assertTrue(openDataPage.kaynakTabloSorguGoruntulendiMi(),
                "Kaynak Tablo / Sorgu bölümü görüntülenemedi.");

        ReportManager.step("Kaynak Ekle formu açılıyor.");
        openDataPage.kaynakEklemeEkraniniAcma();
        Assert.assertTrue(kaynakEkleModal.kaynakEkleFormuGoruntulendiMi(),
                "Kaynak Ekle formu görüntülenemedi.");

        ReportManager.step("Kaynak adı, tablo ve açıklama giriliyor.");

        kaynakEkleModal.kaynakAdiGirme(kaynakAdi);
        Assert.assertEquals(kaynakEkleModal.kaynakAdiAlma(), kaynakAdi,
                "Kaynak adı alanındaki değer beklenenle uyuşmuyor.");

        kaynakEkleModal.kaynakTablosuSecme(KAYNAK_TABLOSU);
        Assert.assertEquals(kaynakEkleModal.seciliTabloyuAlma(), KAYNAK_TABLOSU,
                "Seçilen kaynak tablosu beklenenle uyuşmuyor.");

        kaynakEkleModal.kaynakAciklamasiGirme(kaynakAciklamasi);
        Assert.assertEquals(kaynakEkleModal.kaynakAciklamasiAlma(), kaynakAciklamasi,
                "Kaynak açıklaması beklenenle uyuşmuyor.");

        Assert.assertTrue(kaynakEkleModal.veriOnizlemeGoruntulendiMi(),
                "Veri Önizleme bölümü görüntülenemedi.");

        ReportManager.step("Kaynak oluşturuluyor.");
        kaynakEkleModal.kaynakEkleme();
        Assert.assertTrue(kaynakEkleModal.kaynakEkleFormuKapandiMi(),
                "Kaynak oluşturma işleminden sonra form kapanmadı.");

        ReportManager.step("Oluşturulan kaynak listede doğrulanıyor.");
        Assert.assertTrue(openDataPage.kaynakListedeMi(kaynakAdi),
                "Oluşturulan kaynak listede bulunamadı: " + kaynakAdi);
        Assert.assertTrue(openDataPage.kaynakBilgileriDogruMu(kaynakAdi, kaynakAciklamasi),
                "Kaynak bilgileri oluşturulan değerlerle uyuşmuyor.");

        ReportManager.info("KAYNAK OLUŞTURMA BAŞARILI | " + kaynakAdi);
    }

    /** Oluşturulan kaynağın silme modalı üzerinden silindiğini doğrular. */
    @Test
    public void kaynakSilme() {

        TestContext context = testHazirligi();
        OpenDataPage openDataPage = context.openDataPage();

        String kaynakAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_silinecek_kaynak");
        context.createFlow().createSource(kaynakAdi, KAYNAK_TABLOSU, "Otomasyon silme testi - " + kaynakAdi);

        KaynakSilModal silModal = new KaynakSilModal(context.page());

        ReportManager.step("Kaynak siliniyor: " + kaynakAdi);
        openDataPage.kaynakSilmeButonunaTiklama(kaynakAdi);

        Assert.assertTrue(silModal.kaynakSilModalGoruntulendiMi(kaynakAdi),
                "Silme onay ekranında beklenen kaynak adı görüntülenemedi.");

        silModal.silmeOnaylama();

        Assert.assertTrue(silModal.kaynakSilModalKapandiMi(),
                "Silme işleminden sonra dialog kapanmadı.");
        Assert.assertTrue(openDataPage.kaynakListedeYokMu(kaynakAdi),
                "Silinen kaynak hâlâ listede görüntüleniyor: " + kaynakAdi);

        ReportManager.info("KAYNAK SİLME BAŞARILI | " + kaynakAdi);
    }

    /** Kaynağın Genel Bilgi ve Önizleme ekranlarını doğrular. */
    @Test
    public void kaynakGoruntuleme() {

        TestContext context = testHazirligi();
        OpenDataPage openDataPage = context.openDataPage();

        String kaynakAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_goruntuleme_kaynak");
        context.createFlow().createSource(kaynakAdi, KAYNAK_TABLOSU,
                "Otomasyon kaynak görüntüleme testi - " + kaynakAdi);

        ReportManager.step("Kaynak detayı açılıyor: " + kaynakAdi);
        openDataPage.kaynakGoruntulemeButonunaTiklama(kaynakAdi);

        ReportManager.step("Genel Bilgi sekmesi doğrulanıyor.");
        openDataPage.genelBilgiSekmesineTiklama();
        Assert.assertTrue(openDataPage.genelBilgiSayfasiGoruntulendiMi(),
                "Kaynak Genel Bilgi ekranı doğru şekilde görüntülenemedi.");

        ReportManager.step("Önizleme sekmesi doğrulanıyor.");
        openDataPage.onizlemeSekmesineTiklama();
        Assert.assertTrue(openDataPage.onizlemeSayfasiGoruntulendiMi(),
                "Kaynak Önizleme ekranı görüntülenemedi.");

        ReportManager.info("KAYNAK GÖRÜNTÜLEME BAŞARILI | " + kaynakAdi);
    }

    /** Kaynak adı ve açıklamasının düzenleme modalından güncellenebildiğini doğrular. */
    @Test
    public void kaynakDuzenleme() {

        TestContext context = testHazirligi();
        OpenDataPage openDataPage = context.openDataPage();

        String kaynakAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_duzenleme_kaynak");
        String aciklama = "Otomasyon kaynak düzenleme testi - " + kaynakAdi;
        String yeniKaynakAdi = kaynakAdi + "_duzenlendi";
        String yeniAciklama = "Otomasyon kaynak düzenleme testi güncellendi - " + yeniKaynakAdi;

        context.createFlow().createSource(kaynakAdi, KAYNAK_TABLOSU, aciklama);

        KaynakDuzenleModal duzenleModal = new KaynakDuzenleModal(context.page());

        ReportManager.step("Düzenleme ekranı açılıyor: " + kaynakAdi);
        openDataPage.kaynakDuzenlemeButonunaTiklama(kaynakAdi);

        Assert.assertTrue(duzenleModal.kaynakDuzenleModalGoruntulendiMi(),
                "Kaynak düzenleme ekranı görüntülenemedi.");
        Assert.assertEquals(duzenleModal.kaynakAdiAlma(), kaynakAdi,
                "Düzenleme ekranındaki kaynak adı hatalı.");
        Assert.assertEquals(duzenleModal.kaynakAciklamasiAlma(), aciklama,
                "Düzenleme ekranındaki açıklama hatalı.");

        ReportManager.step("Kaynak adı ve açıklaması güncelleniyor.");
        duzenleModal.adDegistirme(yeniKaynakAdi);
        duzenleModal.aciklamaDegistirme(yeniAciklama);

        Assert.assertEquals(duzenleModal.kaynakAdiAlma(), yeniKaynakAdi,
                "Yeni kaynak adı forma doğru yazılmadı.");
        Assert.assertEquals(duzenleModal.kaynakAciklamasiAlma(), yeniAciklama,
                "Yeni açıklama forma doğru yazılmadı.");

        duzenleModal.kaydetme();

        ReportManager.step("Güncellenen kaynak listede doğrulanıyor.");
        Assert.assertTrue(openDataPage.kaynakListedeMi(yeniKaynakAdi),
                "Güncellenen kaynak listede bulunamadı.");
        Assert.assertTrue(openDataPage.kaynakBilgileriDogruMu(yeniKaynakAdi, yeniAciklama),
                "Güncellenen kaynak bilgileri listede doğrulanamadı.");
        Assert.assertTrue(openDataPage.kaynakListedeYokMu(kaynakAdi),
                "Eski kaynak adı hâlâ listede görüntüleniyor.");

        ReportManager.info("KAYNAK DÜZENLEME BAŞARILI | " + yeniKaynakAdi);
    }

    /** Oluşturulan kaynağı pozitif kriterle, bulunmayan kaynağı negatif kriterle arar. */
    @Test
    public void kaynakArama() {

        TestContext context = testHazirligi();
        OpenDataPage openDataPage = context.openDataPage();

        String kaynakAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_arama_kaynak");
        String olmayanKriter = TestDataUtil.dinamikAdOlusturma("otomasyon_bulunmayan");

        context.createFlow().createSource(kaynakAdi, KAYNAK_TABLOSU, "Otomasyon arama testi - " + kaynakAdi);

        ReportManager.step("Pozitif arama: " + kaynakAdi);
        openDataPage.kaynakArama(kaynakAdi);
        Assert.assertTrue(openDataPage.kaynakAramaSonuclariUygunMu(kaynakAdi),
                "Aranan kaynak listede bulunamadı: " + kaynakAdi);
        ekranGoruntusuEkle(context, "Kaynak Arama - Pozitif Sonuç", "kaynak_arama_pozitif");

        ReportManager.step("Negatif arama: " + olmayanKriter);
        openDataPage.kaynakArama(olmayanKriter);
        Assert.assertTrue(openDataPage.kaynakBulunamadiMesajiGoruntulendiMi(),
                "Kaynak bulunamadı mesajı görüntülenemedi.");
        ekranGoruntusuEkle(context, "Kaynak Arama - Sonuç Bulunamadı", "kaynak_arama_negatif");

        ReportManager.info("KAYNAK ARAMA BAŞARILI");
    }

    // =====================================================================
    // Kaynak ekleme validasyonları
    // =====================================================================

    /** Ad ve Tablo boşken iki zorunlu alan hatasını doğrular. */
    @Test
    public void kaynakEklemeZorunluAlanKontrolu() {

        TestContext context = testHazirligi();
        KaynakEkleModal kaynakEkleModal = context.kaynakEkleModal();

        ReportManager.step("Ad ve Tablo boş bırakılarak Kaynak Ekle seçiliyor.");
        context.openDataPage().kaynakEklemeEkraniniAcma();
        Assert.assertTrue(kaynakEkleModal.kaynakEkleFormuGoruntulendiMi(),
                "Kaynak Ekle formu görüntülenemedi.");

        kaynakEkleModal.kaynakEkleme();

        Assert.assertTrue(kaynakEkleModal.kaynakAdiZorunluMesajiGoruntulendiMi(),
                "Kaynak adı zorunlu alan mesajı görüntülenmedi.");
        Assert.assertTrue(kaynakEkleModal.tabloZorunluMesajiGoruntulendiMi(),
                "Tablo zorunlu alan mesajı görüntülenmedi.");
        Assert.assertTrue(kaynakEkleModal.kaynakEkleFormuGoruntulendiMi(),
                "Validasyon hatasına rağmen form kapandı.");

        ReportManager.info("TÜM ZORUNLU ALAN KONTROLÜ BAŞARILI");
    }

    /** Ad dolu ve Tablo boşken yalnızca Tablo validasyonunu doğrular. */
    @Test
    public void kaynakEklemeTabloZorunluAlanKontrolu() {

        TestContext context = testHazirligi();
        KaynakEkleModal kaynakEkleModal = context.kaynakEkleModal();

        ReportManager.step("Yalnızca ad girilerek Kaynak Ekle seçiliyor.");
        context.openDataPage().kaynakEklemeEkraniniAcma();
        kaynakEkleModal.kaynakAdiGirme(TestDataUtil.dinamikAdOlusturma("otomasyon_zorunlu_tablo"));
        kaynakEkleModal.kaynakEkleme();

        // Önce beklenen mesaj doğrulanır; ardından diğer mesajın olmadığı kontrol edilir.
        Assert.assertTrue(kaynakEkleModal.tabloZorunluMesajiGoruntulendiMi(),
                "Tablo zorunlu alan mesajı görüntülenmedi.");
        Assert.assertFalse(kaynakEkleModal.kaynakAdiZorunluMesajiGoruntulendiMi(),
                "Kaynak adı dolu olmasına rağmen zorunlu alan mesajı görüntülendi.");
        Assert.assertTrue(kaynakEkleModal.kaynakEkleFormuGoruntulendiMi(),
                "Tablo alanı boş olmasına rağmen form kapandı.");

        ReportManager.info("TABLO ZORUNLU ALAN KONTROLÜ BAŞARILI");
    }

    /** Tablo seçili ve Ad boşken yalnızca Ad validasyonunu doğrular. */
    @Test
    public void kaynakEklemeAdZorunluAlanKontrolu() {

        TestContext context = testHazirligi();
        KaynakEkleModal kaynakEkleModal = context.kaynakEkleModal();

        ReportManager.step("Yalnızca tablo seçilerek Kaynak Ekle seçiliyor.");
        context.openDataPage().kaynakEklemeEkraniniAcma();
        kaynakEkleModal.kaynakTablosuSecme(KAYNAK_TABLOSU);
        kaynakEkleModal.kaynakEkleme();

        Assert.assertTrue(kaynakEkleModal.kaynakAdiZorunluMesajiGoruntulendiMi(),
                "Kaynak adı zorunlu alan mesajı görüntülenmedi.");
        Assert.assertFalse(kaynakEkleModal.tabloZorunluMesajiGoruntulendiMi(),
                "Tablo seçili olmasına rağmen Tablo zorunlu alan mesajı görüntülendi.");
        Assert.assertTrue(kaynakEkleModal.kaynakEkleFormuGoruntulendiMi(),
                "Kaynak adı boş olmasına rağmen form kapandı.");

        ReportManager.info("KAYNAK ADI ZORUNLU ALAN KONTROLÜ BAŞARILI");
    }

    // =====================================================================
    // Hazır sorgu
    // =====================================================================

    /** Hazır SELECT sorgusuyla kaynak oluşturur ve sonucu doğrular. */
    @Test
    public void hazirSorguIleKaynakOlusturma() {

        TestContext context = testHazirligi();
        OpenDataPage openDataPage = context.openDataPage();
        KaynakEkleModal kaynakEkleModal = context.kaynakEkleModal();

        String kaynakAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_sorgu_kaynak");
        String aciklama = "Otomasyon hazır sorgu kaynak testi - " + kaynakAdi;

        String sorgu = """
                SELECT
                    a.yil AS yil_yillik_hat_km,
                    a.bolge_kodu AS bolge_kodu_yillik_hat_km,
                    a.bolge_adi AS bolge_adi_yillik_hat_km,
                    b.hat_kodu AS hat_kodu_yillik_hat_kapasitesi,
                    b.hat_adi AS hat_adi_yillik_hat_kapasitesi
                FROM yillik_hat_km a
                LEFT JOIN yillik_hat_kapasitesi b
                    ON a.bolge_kodu = b.bolge_kodu
                    AND a.yil = b.yil;""";

        ReportManager.step("Hazır Sorgu ile kaynak formu dolduruluyor: " + kaynakAdi);
        openDataPage.kaynakEklemeEkraniniAcma();
        Assert.assertTrue(kaynakEkleModal.kaynakEkleFormuGoruntulendiMi(),
                "Kaynak Ekle formu görüntülenemedi.");

        kaynakEkleModal.hazirSorguyaGecme();

        kaynakEkleModal.kaynakAdiGirme(kaynakAdi);
        Assert.assertEquals(kaynakEkleModal.kaynakAdiAlma(), kaynakAdi,
                "Kaynak adı alanındaki değer beklenenle uyuşmuyor.");

        kaynakEkleModal.kaynakAciklamasiGirme(aciklama);
        Assert.assertEquals(kaynakEkleModal.kaynakAciklamasiAlma(), aciklama,
                "Kaynak açıklaması beklenenle uyuşmuyor.");

        ReportManager.step("SQL sorgusu giriliyor ve doğrulanıyor.");
        kaynakEkleModal.hazirSorguGirme(sorgu);
        kaynakEkleModal.hazirSorguDogrulama();

        Assert.assertTrue(kaynakEkleModal.hazirSorguKolonlariGoruntulendiMi(),
                "Sorgu doğrulaması sonrasında beklenen kolonlar görüntülenemedi.");
        Assert.assertTrue(kaynakEkleModal.veriOnizlemeGoruntulendiMi(),
                "Hazır sorgu sonrasında Veri Önizleme bölümü görüntülenemedi.");

        ReportManager.step("Hazır sorgu kaynağı oluşturuluyor.");
        kaynakEkleModal.kaynakEkleme();

        Assert.assertTrue(kaynakEkleModal.kaynakEkleFormuKapandiMi(),
                "Hazır sorgu kaynağı oluşturulduktan sonra form kapanmadı.");
        Assert.assertTrue(openDataPage.kaynakListedeMi(kaynakAdi),
                "Hazır sorguyla oluşturulan kaynak listede bulunamadı: " + kaynakAdi);
        Assert.assertTrue(openDataPage.kaynakBilgileriDogruMu(kaynakAdi, aciklama),
                "Hazır sorgu kaynağının ad/açıklama bilgileri uyuşmuyor.");

        ReportManager.info("HAZIR SORGU İLE KAYNAK OLUŞTURMA BAŞARILI | " + kaynakAdi);
    }

    /** SELECT dışındaki UPDATE sorgusunun sistem tarafından engellendiğini doğrular. */
    @Test
    public void hazirSorguGecersizDmlKomutlari() {

        TestContext context = testHazirligi();
        KaynakEkleModal kaynakEkleModal = context.kaynakEkleModal();

        String kaynakAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_update_kontrol");

        String sorgu = """
                UPDATE yillik_hat_km
                SET yil = yil
                WHERE 1 = 0;""";

        ReportManager.step("Hazır Sorgu alanına UPDATE sorgusu giriliyor: " + kaynakAdi);
        context.openDataPage().kaynakEklemeEkraniniAcma();
        Assert.assertTrue(kaynakEkleModal.kaynakEkleFormuGoruntulendiMi(),
                "Kaynak Ekle formu görüntülenemedi.");

        kaynakEkleModal.hazirSorguyaGecme();
        kaynakEkleModal.kaynakAdiGirme(kaynakAdi);
        kaynakEkleModal.kaynakAciklamasiGirme("Otomasyon UPDATE sorgu kontrolü - " + kaynakAdi);
        kaynakEkleModal.hazirSorguGirme(sorgu);
        kaynakEkleModal.hazirSorguDogrulama();

        ReportManager.step("UPDATE sorgusunun engellendiği doğrulanıyor.");
        Assert.assertTrue(kaynakEkleModal.kullaniciKaynakliHataGoruntulendiMi(),
                "Kullanıcı Kaynaklı Hata mesajı görüntülenmedi.");
        Assert.assertTrue(kaynakEkleModal.sadeceSelectSorgusuHatasiGoruntulendiMi(),
                "Yalnızca SELECT sorgularına izin verildiği mesajı görüntülenmedi.");
        Assert.assertTrue(kaynakEkleModal.readOnlySorguHataKoduGoruntulendiMi(),
                "OPEN_DATA.SOURCE.QUERY_NOT_READ_ONLY hata kodu görüntülenmedi.");

        ReportManager.info("UPDATE SORGUSU ENGELLEME BAŞARILI");
    }

    // =====================================================================
    // Veri seti
    // =====================================================================

    /** Maskeleme uygulanmış veri setini KVKK onay sürecine gönderir. */
    @Test
    public void veriSetiOnayaGonderme() {

        TestContext context = testHazirligi();
        OpenDataPage openDataPage = context.openDataPage();

        String kaynakAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_onay_kaynak");
        String veriSetiAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_veri_seti");
        String veriSetiAciklamasi = "Otomasyon KVKK onay veri seti - " + veriSetiAdi;

        context.createFlow().createSource(kaynakAdi, KAYNAK_TABLOSU,
                "Otomasyon veri seti onay kaynağı - " + kaynakAdi);

        ReportManager.step("Kaynak veri seti için seçiliyor: " + kaynakAdi);
        openDataPage.kaynakSecme(kaynakAdi);
        Assert.assertTrue(openDataPage.kaynakSeciliMi(kaynakAdi), "Kaynak seçimi gerçekleştirilemedi.");

        ReportManager.step("yil alanına Maskele kuralı uygulanıyor.");
        openDataPage.veriSetiIleriGitme();
        openDataPage.yilAlaninaMaskeleKuraliUygulama();

        ReportManager.step("Katalog bilgileri dolduruluyor: " + veriSetiAdi);
        openDataPage.veriSetiAdiGirme(veriSetiAdi);
        Assert.assertEquals(openDataPage.veriSetiAdiAlma(), veriSetiAdi,
                "Veri Seti Adı alanındaki değer beklenenle uyuşmuyor.");

        openDataPage.sorumluBirimSecme(SORUMLU_BIRIM);
        openDataPage.kategoriSecme("Altyapı & Şebeke");
        openDataPage.kullanimLisansiSecme("TCDD Taşımacılık Açık Veri Lisansı");

        openDataPage.veriSetiAciklamasiGirme(veriSetiAciklamasi);
        Assert.assertEquals(openDataPage.veriSetiAciklamasiAlma(), veriSetiAciklamasi,
                "Veri seti açıklaması beklenenle uyuşmuyor.");

        ReportManager.step("Veri seti KVKK onayına gönderiliyor.");
        openDataPage.veriSetiOnizleme();
        openDataPage.veriSetiOnayaGonderme();
        openDataPage.veriSetiOnayaGondermeyiOnaylama();

        Assert.assertTrue(openDataPage.veriSetiKvkkOnayinaGonderildiMi(),
                "Veri setinin KVKK onayına gönderilme başarı mesajı görüntülenmedi.");

        ReportManager.info("VERİ SETİ KVKK ONAYINA GÖNDERİLDİ | " + veriSetiAdi);
    }

    /** Veri Seti Adı, Sorumlu Birim ve Kategori zorunlu alan validasyonlarını doğrular. */
    @Test
    public void veriSetiZorunluAlanKontrolleri() {

        TestContext context = testHazirligi();
        OpenDataPage openDataPage = context.openDataPage();

        String kaynakAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_zorunlu_alan_kaynak");
        String veriSetiAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_zorunlu_veri_seti");

        SoftAssert softAssert = new SoftAssert();

        context.createFlow().createSource(kaynakAdi, KAYNAK_TABLOSU,
                "Otomasyon veri seti zorunlu alan testi - " + kaynakAdi);

        ReportManager.step("Kaynak seçiliyor ve katalog ekranına geçiliyor.");
        openDataPage.kaynakSecme(kaynakAdi);
        Assert.assertTrue(openDataPage.kaynakSeciliMi(kaynakAdi), "Kaynak seçilemedi.");
        openDataPage.veriSetiIleriGitme();

        ReportManager.step("Sorumlu Birim ve Kategori boşken Önizle seçiliyor.");
        openDataPage.veriSetiAdiGirme(veriSetiAdi);
        Assert.assertEquals(openDataPage.veriSetiAdiAlma(), veriSetiAdi,
                "Veri Seti Adı alanı doğru doldurulamadı.");
        openDataPage.veriSetiOnizleme();

        softAssert.assertTrue(openDataPage.sorumluBirimZorunluMesajiGoruntulendiMi(),
                "Sorumlu Birim boşken zorunlu alan mesajı görüntülenmedi.");
        softAssert.assertTrue(openDataPage.kategoriZorunluMesajiGoruntulendiMi(),
                "Kategori boşken zorunlu alan mesajı görüntülenmedi.");

        ReportManager.step("Sorumlu Birim ve Kategori seçiliyor, Veri Seti Adı boşken Önizle seçiliyor.");
        openDataPage.sorumluBirimSecme(SORUMLU_BIRIM);
        openDataPage.kategoriSecme("Yolcu Taşımacılığı");
        openDataPage.veriSetiAdiGirme("");
        Assert.assertEquals(openDataPage.veriSetiAdiAlma(), "", "Veri Seti Adı alanı boşaltılamadı.");
        openDataPage.veriSetiOnizleme();

        softAssert.assertTrue(openDataPage.veriSetiAdiZorunluMesajiGoruntulendiMi(),
                "Veri Seti Adı boşken zorunlu alan mesajı görüntülenmedi.");

        softAssert.assertAll();

        ReportManager.info("VERİ SETİ ZORUNLU ALAN KONTROLLERİ BAŞARILI");
    }

    /** Belirli bir kolona Maskele kuralı uygular ve önizlemedeki sonucu doğrular. */
    @Test
    public void veriSetiKolonMaskelemeKontrolu() {

        TestContext context = testHazirligi();
        OpenDataPage openDataPage = context.openDataPage();

        String kaynakAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_maskeleme_kaynak");
        String veriSetiAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_maskeli_veri_seti");
        String maskelenecekKolon = "bolge_adi";

        context.createFlow().createSource(kaynakAdi, KAYNAK_TABLOSU,
                "Otomasyon kolon maskeleme testi - " + kaynakAdi);

        ReportManager.step("Kaynak seçiliyor ve " + maskelenecekKolon + " kolonuna Maskele kuralı uygulanıyor.");
        openDataPage.kaynakSecme(kaynakAdi);
        Assert.assertTrue(openDataPage.kaynakSeciliMi(kaynakAdi),
                "Maskeleme testi için kaynak seçilemedi.");
        openDataPage.veriSetiIleriGitme();
        openDataPage.kolonaMaskelemeKuraliUygulama(maskelenecekKolon);

        ReportManager.step("Katalog bilgileri dolduruluyor ve önizlemeye geçiliyor.");
        openDataPage.veriSetiAdiGirme(veriSetiAdi);
        openDataPage.sorumluBirimSecme(SORUMLU_BIRIM);
        openDataPage.kategoriSecme("Yolcu Taşımacılığı");
        openDataPage.veriSetiOnizleme();

        ReportManager.step("Önizlemede maskeleme doğrulanıyor.");
        Assert.assertTrue(openDataPage.onizlemeKolonuGoruntulendiMi(maskelenecekKolon),
                "Maskelenen kolon önizlemede görüntülenmedi: " + maskelenecekKolon);
        Assert.assertTrue(openDataPage.maskelendiBilgisiGoruntulendiMi(),
                "Maskeleme uygulanan kolonda Maskelendi bilgisi görüntülenmedi.");
        Assert.assertTrue(openDataPage.maskelenmisVeriGoruntulendiMi(),
                "Anonimleştirilmiş önizlemede maskelenmiş veri bulunamadı.");

        ReportManager.info("KOLON MASKELEME BAŞARILI | Kolon: " + maskelenecekKolon);
    }

    // =====================================================================
    // Yardımcılar
    // =====================================================================

    /** Ara adımda ekran görüntüsü alıp rapora ekler; alınamazsa testi etkilemez. */
    private void ekranGoruntusuEkle(TestContext context, String baslik, String dosyaAdi) {
        Path screenshot = ScreenshotUtil.takeScreenshot(context.page(), dosyaAdi);
        if (screenshot != null) {
            ReportManager.attachScreenshot(baslik, screenshot);
        }
    }
}