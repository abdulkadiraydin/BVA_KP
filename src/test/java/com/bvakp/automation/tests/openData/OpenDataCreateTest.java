package com.bvakp.automation.tests.openData;

import com.bvakp.automation.base.AvpBaseTest;
import com.bvakp.automation.companents.KaynakDuzenleModal;
import com.bvakp.automation.companents.KaynakEkleModal;
import com.bvakp.automation.companents.KaynakSilModal;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.reporting.ReportManager;
import com.bvakp.automation.utils.ScreenshotUtil;
import com.bvakp.automation.utils.TestDataUtil;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.nio.file.Path;

/**
 * Açık Veri Portalı kaynak yönetimi, hazır sorgu ve veri seti oluşturma testleri.
 * Her test kendi ön koşul verisini oluşturur; ayrı bir Flow katmanı kullanılmaz.
 */
public class OpenDataCreateTest extends AvpBaseTest {

    private static final String KAYNAK_TABLOSU = "arac_bakim_is_yeri_sayisi";
    private OpenDataPage openDataPage;
    private KaynakEkleModal kaynakEkleModal;

    /**
     * Her @Test içinden çağrılarak sayfa nesnelerini ve AVP oturumunu hazırlar.
     * Login ve geçiş loglarının ilgili Allure testine eklenmesi için @BeforeMethod
     * yerine test gövdesinden çağrılır. BaseTest.setUp() Page'i önceden açar.
     */
    private void testHazirligi() {
        openDataPage = new OpenDataPage(page);
        kaynakEkleModal = new KaynakEkleModal(page);
        avpOturumuHazirlama();
        ReportManager.step("Açık Veri Portalına geçiliyor.");
        openDataPage.acikVeriPortalinaGitme();
    }

    /**
     * Başka bir işlevi test etmek için gereken kaynağı oluşturur ve görünürlüğünü doğrular.
     * Kaynak oluşturmanın kendisinin sınandığı testte bu yardımcı metot kullanılmaz.
     */
    private void testKaynagiOlusturma(String kaynakAdi, String tabloAdi, String aciklama) {
        ReportManager.step("Test ön koşulu: kaynak oluşturuluyor | " + kaynakAdi);
        openDataPage.kaynakEklemeEkraniniAcma();
        Assert.assertTrue(kaynakEkleModal.kaynakEkleFormuGoruntulendiMi(),
                "Ön koşul: Kaynak Ekle formu açılamadı.");

        ReportManager.step("Kaynak adı giriliyor: " + kaynakAdi);
        kaynakEkleModal.kaynakAdiGirme(kaynakAdi);
        ReportManager.step("Kaynak tablosu seçiliyor: " + tabloAdi);
        kaynakEkleModal.kaynakTablosuSecme(tabloAdi);
        ReportManager.step("Kaynak açıklaması giriliyor.");
        kaynakEkleModal.kaynakAciklamasiGirme(aciklama);
        Assert.assertTrue(kaynakEkleModal.veriOnizlemeGoruntulendiMi(),
                "Ön koşul: Veri Önizleme görüntülenemedi.");

        ReportManager.step("Kaynak ekleme işlemi tamamlanıyor.");
        kaynakEkleModal.kaynakEkleme();
        Assert.assertTrue(kaynakEkleModal.kaynakEkleFormuKapandiMi(),
                "Ön koşul: Kaynak oluşturulduktan sonra form kapanmadı.");
        Assert.assertTrue(openDataPage.kaynakListedeMi(kaynakAdi),
                "Ön koşul: Kaynak listede bulunamadı: " + kaynakAdi);
        ReportManager.info("Test kaynağı oluşturuldu | " + kaynakAdi);
    }

    /**
     * Kaynak ekleme alanlarını, veri önizlemesini ve kalıcı kaydı adım adım doğrular.
     */
    @Test(priority = 1)
    public void kaynakOlusturma() {
        testHazirligi();
        String kaynakAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");
        String kaynakAciklamasi = "Otomasyon kaynak oluşturma testi - " + kaynakAdi;
        ReportManager.info("KAYNAK OLUŞTURMA TEST VERİSİ | Kaynak: " + kaynakAdi
                + " | Tablo: " + KAYNAK_TABLOSU);

        ReportManager.step("Kaynak Tablo / Sorgu bölümü kontrol ediliyor.");
        Assert.assertTrue(openDataPage.kaynakTabloSorguGoruntulendiMi(),
                "Kaynak Tablo / Sorgu bölümü görüntülenemedi.");

        ReportManager.step("Kaynak Ekle formu açılıyor.");
        openDataPage.kaynakEklemeEkraniniAcma();
        Assert.assertTrue(kaynakEkleModal.kaynakEkleFormuGoruntulendiMi(),
                "Kaynak Ekle formu görüntülenemedi.");

        ReportManager.step("Kaynak adı giriliyor: " + kaynakAdi);
        kaynakEkleModal.kaynakAdiGirme(kaynakAdi);
        Assert.assertEquals(kaynakEkleModal.kaynakAdiAlma(), kaynakAdi,
                "Kaynak adı alanındaki değer beklenen değerle uyuşmuyor.");

        ReportManager.step("Kaynak tablosu seçiliyor: " + KAYNAK_TABLOSU);
        kaynakEkleModal.kaynakTablosuSecme(KAYNAK_TABLOSU);
        Assert.assertEquals(kaynakEkleModal.seciliTabloyuAlma(), KAYNAK_TABLOSU,
                "Seçilen kaynak tablosu beklenen değerle uyuşmuyor.");

        ReportManager.step("Kaynak açıklaması giriliyor.");
        kaynakEkleModal.kaynakAciklamasiGirme(kaynakAciklamasi);
        Assert.assertEquals(kaynakEkleModal.kaynakAciklamasiAlma(), kaynakAciklamasi,
                "Kaynak açıklaması beklenen değerle uyuşmuyor.");

        ReportManager.step("Veri Önizleme bölümü doğrulanıyor.");
        Assert.assertTrue(kaynakEkleModal.veriOnizlemeGoruntulendiMi(),
                "Veri Önizleme bölümü görüntülenemedi.");

        ReportManager.step("Kaynak oluşturuluyor ve formun kapandığı doğrulanıyor.");
        kaynakEkleModal.kaynakEkleme();
        Assert.assertTrue(kaynakEkleModal.kaynakEkleFormuKapandiMi(),
                "Kaynak oluşturma işleminden sonra form kapanmadı.");

        ReportManager.step("Oluşturulan kaynak ve açıklaması listede doğrulanıyor.");
        Assert.assertTrue(openDataPage.kaynakListedeMi(kaynakAdi),
                "Oluşturulan kaynak listede bulunamadı: " + kaynakAdi);
        Assert.assertTrue(openDataPage.kaynakBilgileriDogruMu(kaynakAdi, kaynakAciklamasi),
                "Kaynak bilgileri oluşturulan değerlerle uyuşmuyor.");
        ReportManager.info("KAYNAK OLUŞTURMA BAŞARILI | Kaynak: " + kaynakAdi);
    }

    /**
     * Yalnızca bu testin oluşturduğu kaynağın silme modalı üzerinden silindiğini doğrular.
     */
    @Test(priority = 2)
    public void kaynakSilme() {
        testHazirligi();
        String kaynakAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_silinecek_kaynak");
        String aciklama = "Otomasyon silme testi - " + kaynakAdi;
        testKaynagiOlusturma(kaynakAdi, KAYNAK_TABLOSU, aciklama);
        KaynakSilModal silModal = new KaynakSilModal(page);

        ReportManager.step("Kaynağın Sil butonuna tıklanıyor: " + kaynakAdi);
        openDataPage.kaynakSilmeButonunaTiklama(kaynakAdi);

        ReportManager.step("Silme modalında doğru kaynak adı doğrulanıyor.");
        Assert.assertTrue(silModal.kaynakSilModalGoruntulendiMi(kaynakAdi),
                "Silme onay ekranında beklenen kaynak adı görüntülenemedi.");

        ReportManager.step("Kaynak silme işlemi onaylanıyor.");
        silModal.silmeOnaylama();
        Assert.assertTrue(silModal.kaynakSilModalKapandiMi(),
                "Silme işleminden sonra dialog kapanmadı.");
        Assert.assertTrue(openDataPage.kaynakListedenSilindiMi(kaynakAdi),
                "Silinen kaynak hâlâ listede görüntüleniyor: " + kaynakAdi);
        ReportManager.info("KAYNAK SİLME BAŞARILI | Kaynak: " + kaynakAdi);
    }

    /**
     * Dinamik kaynak oluşturarak Genel Bilgi ve Önizleme ekranlarını doğrular.
     * Ortamda önceden var olan başka bir kayda bağımlı değildir.
     */
    @Test(priority = 3)
    public void kaynakGoruntuleme() {
        testHazirligi();
        String kaynakAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_goruntuleme_kaynak");
        String aciklama = "Otomasyon kaynak görüntüleme testi - " + kaynakAdi;
        testKaynagiOlusturma(kaynakAdi, KAYNAK_TABLOSU, aciklama);

        ReportManager.step("Test kaynağının Görüntüle butonuna tıklanıyor: " + kaynakAdi);
        openDataPage.kaynakGoruntulemeButonunaTiklama(kaynakAdi);

        ReportManager.step("Genel Bilgi sekmesindeki kaynak ve kayıt bilgileri doğrulanıyor.");
        openDataPage.genelBilgiSekmesineTiklama();
        Assert.assertTrue(openDataPage.genelBilgiSayfasiGoruntulendiMi(),
                "Kaynak Genel Bilgi ekranı doğru şekilde görüntülenemedi.");

        ReportManager.step("Önizleme sekmesindeki ham veri doğrulanıyor.");
        openDataPage.onizlemeSekmesineTiklama();
        Assert.assertTrue(openDataPage.onizlemeSayfasiGoruntulendiMi(),
                "Kaynak Önizleme ekranı görüntülenemedi.");
        ReportManager.info("KAYNAK GÖRÜNTÜLEME VE ÖNİZLEME KONTROLÜ BAŞARILI");
    }

    /**
     * Kaynak adı ve açıklamasının düzenleme modalından güncellenebildiğini doğrular.
     */
    @Test(priority = 4)
    public void kaynakDuzenleme() {
        testHazirligi();
        String kaynakAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_duzenleme_kaynak");
        String aciklama = "Otomasyon kaynak düzenleme testi - " + kaynakAdi;
        String yeniKaynakAdi = kaynakAdi + "_duzenlendi";
        String yeniAciklama = "Otomasyon kaynak düzenleme testi güncellendi - " + yeniKaynakAdi;
        testKaynagiOlusturma(kaynakAdi, KAYNAK_TABLOSU, aciklama);
        KaynakDuzenleModal duzenleModal = new KaynakDuzenleModal(page);

        ReportManager.step("Kaynağın Düzenle butonuna tıklanıyor: " + kaynakAdi);
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

        ReportManager.step("Kaynak düzenleme işlemi kaydediliyor.");
        duzenleModal.kaydetme();
        Assert.assertTrue(openDataPage.kaynakListedeMi(yeniKaynakAdi),
                "Güncellenen kaynak listede bulunamadı.");
        Assert.assertTrue(openDataPage.kaynakBilgileriDogruMu(yeniKaynakAdi, yeniAciklama),
                "Güncellenen kaynak bilgileri listede doğrulanamadı.");
        Assert.assertFalse(openDataPage.kaynakListedeMi(kaynakAdi),
                "Eski kaynak adı hâlâ listede görüntüleniyor.");
        ReportManager.info("KAYNAK DÜZENLEME BAŞARILI | Yeni kaynak: " + yeniKaynakAdi);
    }

    /**
     * Kendi oluşturduğu kaynağı pozitif kriterle, olmayan kaynağı negatif kriterle arar.
     * Her iki sonucun ekran görüntüsünü rapora ekler.
     */
    @Test(priority = 5)
    public void kaynakArama() {
        testHazirligi();
        String kaynakAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_arama_kaynak");
        String olmayanKriter = TestDataUtil.dinamikAdOlusturma("otomasyon_bulunmayan");
        testKaynagiOlusturma(kaynakAdi, KAYNAK_TABLOSU,
                "Otomasyon arama testi - " + kaynakAdi);

        ReportManager.step("Pozitif kaynak araması yapılıyor: " + kaynakAdi);
        openDataPage.kaynakArama(kaynakAdi);
        Assert.assertTrue(openDataPage.kaynakAramaSonuclariUygunMu(kaynakAdi),
                "Aranan kaynak listede bulunamadı: " + kaynakAdi);
        ReportManager.step("Pozitif arama sonucu ekran görüntüsü alınıyor.");
        Path pozitifScreenshot = ScreenshotUtil.takeScreenshot(page, "kaynak_arama_pozitif_otomasyon");
        if (pozitifScreenshot != null) {
            ReportManager.attachScreenshot("Kaynak Arama - Pozitif Sonuç", pozitifScreenshot);
        }

        ReportManager.step("Negatif kaynak araması yapılıyor: " + olmayanKriter);
        openDataPage.kaynakArama(olmayanKriter);
        Assert.assertTrue(openDataPage.kaynakBulunamadiMesajiGoruntulendiMi(),
                "Kaynak bulunamadı mesajı görüntülenemedi.");
        ReportManager.step("Negatif arama sonucu ekran görüntüsü alınıyor.");
        Path negatifScreenshot = ScreenshotUtil.takeScreenshot(page, "kaynak_arama_negatif_kayit_yok");
        if (negatifScreenshot != null) {
            ReportManager.attachScreenshot("Kaynak Arama - Sonuç Bulunamadı", negatifScreenshot);
        }
        ReportManager.info("KAYNAK ARAMA KONTROLÜ BAŞARILI");
    }

    /**
     * Kaynak Adı ve Tablo boş bırakıldığında iki zorunlu alan hatasını doğrular.
     */
    @Test(priority = 6)
    public void kaynakEklemeZorunluAlanKontrolu() {
        testHazirligi();
        ReportManager.step("Kaynak Ekle formu açılıyor.");
        openDataPage.kaynakEklemeEkraniniAcma();
        Assert.assertTrue(kaynakEkleModal.kaynakEkleFormuGoruntulendiMi(),
                "Kaynak Ekle formu görüntülenemedi.");

        ReportManager.step("Ad ve Tablo boş bırakılarak Kaynak Ekle seçiliyor.");
        kaynakEkleModal.kaynakEkleme();
        Assert.assertTrue(kaynakEkleModal.kaynakAdiZorunluMesajiGoruntulendiMi(),
                "Kaynak adı zorunlu alan mesajı görüntülenmedi.");
        Assert.assertTrue(kaynakEkleModal.tabloZorunluMesajiGoruntulendiMi(),
                "Tablo zorunlu alan mesajı görüntülenmedi.");
        Assert.assertTrue(kaynakEkleModal.kaynakEkleFormuGoruntulendiMi(),
                "Validasyon hatasına rağmen form kapandı.");
        ReportManager.info("TÜM ZORUNLU ALAN KONTROLÜ BAŞARILI");
    }

    /**
     * Kaynak Adı dolu, Tablo boşken yalnızca Tablo validasyonu oluşmasını doğrular.
     */
    @Test(priority = 7)
    public void kaynakEklemeTabloZorunluAlanKontrolu() {
        testHazirligi();
        String kaynakAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_zorunlu_tablo");
        ReportManager.step("Kaynak Ekle formu açılıyor, yalnızca ad giriliyor.");
        openDataPage.kaynakEklemeEkraniniAcma();
        kaynakEkleModal.kaynakAdiGirme(kaynakAdi);
        kaynakEkleModal.kaynakEkleme();

        Assert.assertTrue(kaynakEkleModal.tabloZorunluMesajiGoruntulendiMi(),
                "Tablo zorunlu alan mesajı görüntülenmedi.");
        Assert.assertFalse(kaynakEkleModal.kaynakAdiZorunluMesajiGoruntulendiMi(),
                "Kaynak adı dolu olmasına rağmen zorunlu alan mesajı görüntülendi.");
        Assert.assertTrue(kaynakEkleModal.kaynakEkleFormuGoruntulendiMi(),
                "Tablo alanı boş olmasına rağmen form kapandı.");
        ReportManager.info("TABLO ZORUNLU ALAN KONTROLÜ BAŞARILI");
    }

    /**
     * Tablo seçili, Kaynak Adı boşken yalnızca Ad validasyonu oluşmasını doğrular.
     */
    @Test(priority = 8)
    public void kaynakEklemeAdZorunluAlanKontrolu() {
        testHazirligi();
        ReportManager.step("Kaynak Ekle formu açılıyor, yalnızca Tablo seçiliyor.");
        openDataPage.kaynakEklemeEkraniniAcma();
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

    /**
     * Hazır SELECT sorgusuyla kaynak oluşturur; kolonları ve kayıt bilgilerini doğrular.
     */
    @Test(priority = 9)
    public void hazirSorguIleKaynakOlusturma() {
        testHazirligi();
        String kaynakAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_sorgu_kaynak");
        String aciklama = "Otomasyon hazır sorgu kaynak testi - " + kaynakAdi;

        // Recorder'da doğrulanmış sorgu aynen korunur; JOIN ve kolon adları testin parçasıdır.
        String sorgu = "SELECT\n"
                + "    a.yil AS yil_yillik_hat_km,\n"
                + "    a.bolge_kodu AS bolge_kodu_yillik_hat_km,\n"
                + "    a.bolge_adi AS bolge_adi_yillik_hat_km,\n"
                + "    b.hat_kodu AS hat_kodu_yillik_hat_kapasitesi,\n"
                + "    b.hat_adi AS hat_adi_yillik_hat_kapasitesi\n"
                + "FROM yillik_hat_km a\n"
                + "LEFT JOIN yillik_hat_kapasitesi b\n"
                + "    ON a.bolge_kodu = b.bolge_kodu\n"
                + "    AND a.yil = b.yil;";
        ReportManager.info("HAZIR SORGU KAYNAK TEST VERİSİ | Kaynak: " + kaynakAdi);

        ReportManager.step("Kaynak Ekle formu açılıyor.");
        openDataPage.kaynakEklemeEkraniniAcma();
        Assert.assertTrue(kaynakEkleModal.kaynakEkleFormuGoruntulendiMi(),
                "Kaynak Ekle formu görüntülenemedi.");

        ReportManager.step("Hazır Sorgu seçeneğine geçiliyor.");
        kaynakEkleModal.hazirSorguyaGecme();
        kaynakEkleModal.kaynakAdiGirme(kaynakAdi);
        Assert.assertEquals(kaynakEkleModal.kaynakAdiAlma(), kaynakAdi,
                "Kaynak adı alanındaki değer beklenen değerle uyuşmuyor.");
        kaynakEkleModal.kaynakAciklamasiGirme(aciklama);
        Assert.assertEquals(kaynakEkleModal.kaynakAciklamasiAlma(), aciklama,
                "Kaynak açıklaması beklenen değerle uyuşmuyor.");

        ReportManager.step("Hazır SQL sorgusu giriliyor ve doğrulanıyor.");
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
        ReportManager.info("HAZIR SORGU İLE KAYNAK OLUŞTURMA BAŞARILI | Kaynak: " + kaynakAdi);
    }
    /**
     * Hazır Sorgu alanında SELECT dışındaki UPDATE komutunun
     * sistem tarafından engellendiğini ve beklenen hata
     * mesajının görüntülendiğini doğrular.
     */
    @Test(priority = 10)
    public void hazirSorguGecirsizDmlKomutlari() {

        testHazirligi();

        // Test verileri hazırlanır.
        String kaynakAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_update_kontrol");
        String aciklama = "Otomasyon UPDATE sorgu kontrolü - " + kaynakAdi;

        // WHERE 1 = 0, sorgu çalışsa bile veri güncellenmesini önler.
        String sorgu = "UPDATE yillik_hat_km\n"
                + "SET yil = yil\n"
                + "WHERE 1 = 0;";

        ReportManager.info("HAZIR SORGU UPDATE NEGATİF TESTİ | Kaynak: " + kaynakAdi);

        // 1. Kaynak Ekle formu açılır.
        ReportManager.step("Kaynak Ekle formu açılıyor.");

        openDataPage.kaynakEklemeEkraniniAcma();

        Assert.assertTrue(
                kaynakEkleModal.kaynakEkleFormuGoruntulendiMi(),
                "Kaynak Ekle formu görüntülenemedi."
        );

        // 2. Hazır Sorgu sekmesine geçilir ve bilgiler doldurulur.
        ReportManager.step("Hazır Sorgu alanına geçiliyor.");

        kaynakEkleModal.hazirSorguyaGecme();
        kaynakEkleModal.kaynakAdiGirme(kaynakAdi);
        kaynakEkleModal.kaynakAciklamasiGirme(aciklama);

        // 3. UPDATE sorgusu girilir ve doğrulanmaya çalışılır.
        ReportManager.step("SELECT dışı UPDATE sorgusu giriliyor ve doğrulanıyor.");

        kaynakEkleModal.hazirSorguGirme(sorgu);
        kaynakEkleModal.hazirSorguDogrulama();

        // 4. Beklenen hata mesajları doğrulanır.
        ReportManager.step("UPDATE sorgusunun engellendiği doğrulanıyor.");

        Assert.assertTrue(
                kaynakEkleModal.kullaniciKaynakliHataGoruntulendiMi(),
                "Kullanıcı Kaynaklı Hata mesajı görüntülenmedi."
        );

        Assert.assertTrue(
                kaynakEkleModal.sadeceSelectSorgusuHatasiGoruntulendiMi(),
                "Yalnızca SELECT sorgularına izin verildiği mesajı görüntülenmedi."
        );

        Assert.assertTrue(
                kaynakEkleModal.readOnlySorguHataKoduGoruntulendiMi(),
                "OPEN_DATA.SOURCE.QUERY_NOT_READ_ONLY hata kodu görüntülenmedi."
        );

        ReportManager.info(
                "UPDATE SORGUSU ENGELLEME KONTROLÜ BAŞARILI | Kaynak: " + kaynakAdi
        );
    }


    /**
     * Dinamik kaynak ile maskeleme uygular ve veri setini KVKK onay sürecine gönderir.
     */
    @Test(priority = 11)
    public void veriSetiOnayaGonderme() {
        testHazirligi();
        String kaynakAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_onay_kaynak");
        String kaynakAciklamasi = "Otomasyon veri seti onay kaynağı - " + kaynakAdi;
        String veriSetiAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_veri_seti");
        String veriSetiAciklamasi = "Otomasyon KVKK onay veri seti - " + veriSetiAdi;
        String sorumluBirim = "İşletme Dairesi Başkanlığı";
        String kategori = "Altyapı & Şebeke";
        String kullanimLisansi = "TCDD Taşımacılık Açık Veri Lisansı";
        ReportManager.info("VERİ SETİ ONAYA GÖNDERME | Kaynak: " + kaynakAdi
                + " | Veri Seti: " + veriSetiAdi);
        testKaynagiOlusturma(kaynakAdi, KAYNAK_TABLOSU, kaynakAciklamasi);

        ReportManager.step("Oluşturulan kaynak veri seti için seçiliyor: " + kaynakAdi);
        openDataPage.kaynakSecme(kaynakAdi);
        Assert.assertTrue(openDataPage.kaynakSeciliMi(kaynakAdi),
                "Kaynak seçimi gerçekleştirilemedi.");

        ReportManager.step("Alan Seçimi ve Anonimleştirme adımına geçiliyor.");
        openDataPage.veriSetiIleriGitme();
        ReportManager.step("yil alanına Maskele anonimleştirme kuralı uygulanıyor.");
        openDataPage.yilAlaninaMaskeleKuraliUygulama();

        ReportManager.step("Veri seti adı giriliyor: " + veriSetiAdi);
        openDataPage.veriSetiAdiGirme(veriSetiAdi);
        Assert.assertEquals(openDataPage.veriSetiAdiAlma(), veriSetiAdi,
                "Veri Seti Adı alanındaki değer beklenen değerle uyuşmuyor.");
        ReportManager.step("Sorumlu Birim, Kategori ve Kullanım Lisansı seçiliyor.");
        openDataPage.sorumluBirimSecme(sorumluBirim);
        openDataPage.kategoriSecme(kategori);
        openDataPage.kullanimLisansiSecme(kullanimLisansi);
        ReportManager.step("Veri seti açıklaması giriliyor.");
        openDataPage.veriSetiAciklamasiGirme(veriSetiAciklamasi);
        Assert.assertEquals(openDataPage.veriSetiAciklamasiAlma(), veriSetiAciklamasi,
                "Veri seti açıklaması beklenen değerle uyuşmuyor.");

        ReportManager.step("Veri seti önizlemesine geçiliyor.");
        openDataPage.veriSetiOnizleme();
        ReportManager.step("Veri seti KVKK onayına gönderiliyor.");
        openDataPage.veriSetiOnayaGonderme();
        ReportManager.step("Onaya gönderme işlemi onaylanıyor.");
        openDataPage.veriSetiOnayaGondermeyiOnaylama();
        Assert.assertTrue(openDataPage.veriSetiKvkkOnayinaGonderildiMi(),
                "Veri setinin KVKK onayına gönderilme başarı mesajı görüntülenmedi.");
        ReportManager.info("VERİ SETİ KVKK ONAYINA GÖNDERME BAŞARILI | Veri Seti: "
                + veriSetiAdi + " | Kaynak: " + kaynakAdi);
    }

    /**
     * Veri Seti Adı, Sorumlu Birim ve Kategori için bağımsız validasyonları topluca doğrular.
     */
    @Test(priority = 12)
    public void veriSetiZorunluAlanKontrolleri() {
        testHazirligi();
        String kaynakAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_zorunlu_alan_kaynak");
        String aciklama = "Otomasyon veri seti zorunlu alan testi - " + kaynakAdi;
        String veriSetiAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_zorunlu_veri_seti");
        String sorumluBirim = "İşletme Dairesi Başkanlığı";
        String kategori = "Yolcu Taşımacılığı";
        SoftAssert softAssert = new SoftAssert();
        testKaynagiOlusturma(kaynakAdi, KAYNAK_TABLOSU, aciklama);

        ReportManager.step("Kaynak seçiliyor ve katalog bilgileri ekranına geçiliyor.");
        openDataPage.kaynakSecme(kaynakAdi);
        Assert.assertTrue(openDataPage.kaynakSeciliMi(kaynakAdi), "Kaynak seçilemedi.");
        openDataPage.veriSetiIleriGitme();

        // 1. Veri Seti Adı dolu, Sorumlu Birim ve Kategori boş.
        ReportManager.step("Sorumlu Birim ve Kategori boş bırakılarak Önizle seçiliyor.");
        openDataPage.veriSetiAdiGirme(veriSetiAdi);
        Assert.assertEquals(openDataPage.veriSetiAdiAlma(), veriSetiAdi,
                "Veri Seti Adı alanı doğru doldurulamadı.");
        openDataPage.veriSetiOnizleme();
        softAssert.assertTrue(openDataPage.sorumluBirimZorunluMesajiGoruntulendiMi(),
                "Sorumlu Birim boşken zorunlu alan mesajı görüntülenmedi.");
        softAssert.assertTrue(openDataPage.kategoriZorunluMesajiGoruntulendiMi(),
                "Kategori boşken zorunlu alan mesajı görüntülenmedi.");

        // 2. Sorumlu Birim ve Kategori dolu, Veri Seti Adı boş.
        ReportManager.step("Sorumlu Birim ve Kategori seçiliyor, Veri Seti Adı boşaltılıyor.");
        openDataPage.sorumluBirimSecme(sorumluBirim);
        openDataPage.kategoriSecme(kategori);
        openDataPage.veriSetiAdiGirme(""); // fill("") mevcut içeriği temizler.
        Assert.assertEquals(openDataPage.veriSetiAdiAlma(), "",
                "Veri Seti Adı alanı boşaltılamadı.");
        openDataPage.veriSetiOnizleme();
        softAssert.assertTrue(openDataPage.veriSetiAdiZorunluMesajiGoruntulendiMi(),
                "Veri Seti Adı boşken zorunlu alan mesajı görüntülenmedi.");
        softAssert.assertAll();
        ReportManager.info("VERİ SETİ KATALOG ZORUNLU ALAN KONTROLLERİ BAŞARILI");
    }

    /**
     * Belirli bir kolona Maskele kuralı uygulayıp önizlemede maskelemenin etkisini doğrular.
     */
    @Test(priority = 13)
    public void veriSetiKolonMaskelemeKontrolu() {
        testHazirligi();
        String kaynakAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_maskeleme_kaynak");
        String aciklama = "Otomasyon kolon maskeleme testi - " + kaynakAdi;
        String maskelenecekKolon = "bolge_adi";
        String veriSetiAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_maskeli_veri_seti");
        String sorumluBirim = "İşletme Dairesi Başkanlığı";
        String kategori = "Yolcu Taşımacılığı";
        ReportManager.info("VERİ SETİ MASKELEME | Kaynak: " + kaynakAdi
                + " | Kolon: " + maskelenecekKolon);
        testKaynagiOlusturma(kaynakAdi, KAYNAK_TABLOSU, aciklama);

        ReportManager.step("Kaynak seçiliyor: " + kaynakAdi);
        openDataPage.kaynakSecme(kaynakAdi);
        Assert.assertTrue(openDataPage.kaynakSeciliMi(kaynakAdi),
                "Maskeleme testi için kaynak seçilemedi.");
        ReportManager.step("Alan Seçimi ve Anonimleştirme adımına geçiliyor.");
        openDataPage.veriSetiIleriGitme();

        ReportManager.step(maskelenecekKolon + " kolonuna Maskele kuralı uygulanıyor.");
        openDataPage.kolonaMaskelemeKuraliUygulama(maskelenecekKolon);
        ReportManager.step("Veri seti katalog bilgileri dolduruluyor.");
        openDataPage.veriSetiAdiGirme(veriSetiAdi);
        openDataPage.sorumluBirimSecme(sorumluBirim);
        openDataPage.kategoriSecme(kategori);
        ReportManager.step("Anonimleştirilmiş veri önizlemesine geçiliyor.");
        openDataPage.veriSetiOnizleme();

        ReportManager.step(maskelenecekKolon + " kolonunun önizlemede bulunması doğrulanıyor.");
        Assert.assertTrue(openDataPage.onizlemeKolonuGoruntulendiMi(maskelenecekKolon),
                "Maskelenen kolon önizlemede görüntülenmedi: " + maskelenecekKolon);
        ReportManager.step("Kolonun Maskelendi bilgisinin görüntülenmesi doğrulanıyor.");
        Assert.assertTrue(openDataPage.maskelendiBilgisiGoruntulendiMi(),
                "Maskeleme uygulanan kolonda Maskelendi bilgisi görüntülenmedi.");
        ReportManager.step("Önizleme tablosundaki maskeli **** değerleri doğrulanıyor.");
        Assert.assertTrue(openDataPage.maskelenmisVeriGoruntulendiMi(),
                "Anonimleştirilmiş önizlemede maskelenmiş veri bulunamadı.");
        ReportManager.info("KOLON MASKELEME KONTROLÜ BAŞARILI | Kolon: "
                + maskelenecekKolon + " | Veri Seti: " + veriSetiAdi);
    }
}
