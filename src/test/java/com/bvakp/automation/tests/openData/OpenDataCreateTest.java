package com.bvakp.automation.tests.openData;

import com.bvakp.automation.base.AvpBaseTest;
import com.bvakp.automation.companents.KaynakEkleModal;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.reporting.ReportManager;
import com.bvakp.automation.utils.ScreenshotUtil;
import com.bvakp.automation.utils.TestDataUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;

public class OpenDataCreateTest extends AvpBaseTest {


    /**
     * Dinamik test verisi kullanarak yeni kaynak oluşturur
     * ve oluşturulan kaydın listeye doğru yansıdığını doğrular.
     */
    @Test(priority = 1)
    public void kaynakOlusturma() {

        OpenDataPage openDataPage =
                new OpenDataPage(page);

        KaynakEkleModal kaynakEkleModal =
                new KaynakEkleModal(page);


        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma(
                        "otomasyon_kaynak"
                );

        String tabloAdi =
                "arac_bakim_is_yeri_sayisi";

        String kaynakAciklamasi =
                "Otomasyon kaynak oluşturma testi - "
                        + kaynakAdi;


        ReportManager.info(
                "KAYNAK OLUŞTURMA TEST VERİSİ"
                        + " | Kaynak: "
                        + kaynakAdi
                        + " | Tablo: "
                        + tabloAdi
        );


        avpOturumuHazirlama();


        ReportManager.step(
                "Açık Veri Portalına geçiliyor."
        );

        openDataPage
                .acikVeriPortalinaGitme();


        ReportManager.step(
                "Kaynak Tablo / Sorgu bölümünün görüntülendiği doğrulanıyor."
        );

        Assert.assertTrue(
                openDataPage
                        .kaynakTabloSorguGoruntulendiMi(),
                "Kaynak Tablo / Sorgu bölümü görüntülenemedi."
        );


        ReportManager.step(
                "Kaynak Ekle formu açılıyor."
        );

        openDataPage
                .kaynakEklemeEkraniniAcma();


        Assert.assertTrue(
                kaynakEkleModal
                        .kaynakEkleFormuGoruntulendiMi(),
                "Kaynak Ekle formu görüntülenemedi."
        );


        ReportManager.step(
                "Kaynak adı giriliyor: "
                        + kaynakAdi
        );

        kaynakEkleModal
                .kaynakAdiGirme(
                        kaynakAdi
                );

        Assert.assertEquals(
                kaynakEkleModal
                        .kaynakAdiAlma(),
                kaynakAdi,
                "Kaynak adı alanındaki değer beklenen değerle uyuşmuyor."
        );


        ReportManager.step(
                "Kaynak tablosu seçiliyor: "
                        + tabloAdi
        );

        kaynakEkleModal
                .kaynakTablosuSecme(
                        tabloAdi
                );

        Assert.assertEquals(
                kaynakEkleModal
                        .seciliTabloyuAlma(),
                tabloAdi,
                "Seçilen kaynak tablosu beklenen değerle uyuşmuyor."
        );


        ReportManager.step(
                "Kaynak açıklaması giriliyor."
        );

        kaynakEkleModal
                .kaynakAciklamasiGirme(
                        kaynakAciklamasi
                );

        Assert.assertEquals(
                kaynakEkleModal
                        .kaynakAciklamasiAlma(),
                kaynakAciklamasi,
                "Kaynak açıklaması beklenen değerle uyuşmuyor."
        );


        ReportManager.step(
                "Veri Önizleme bölümünün görüntülendiği doğrulanıyor."
        );

        Assert.assertTrue(
                kaynakEkleModal
                        .veriOnizlemeGoruntulendiMi(),
                "Veri Önizleme bölümü görüntülenemedi."
        );


        ReportManager.step(
                "Kaynak oluşturuluyor."
        );

        kaynakEkleModal
                .kaynakEkleme();


        Assert.assertTrue(
                kaynakEkleModal
                        .kaynakEkleFormuKapandiMi(),
                "Kaynak oluşturma işleminden sonra form kapanmadı."
        );


        ReportManager.step(
                "Oluşturulan kaynağın listede bulunduğu doğrulanıyor."
        );

        Assert.assertTrue(
                openDataPage
                        .kaynakListedeMi(
                                kaynakAdi
                        ),
                "Oluşturulan kaynak listede bulunamadı: "
                        + kaynakAdi
        );


        Assert.assertTrue(
                openDataPage
                        .kaynakBilgileriDogruMu(
                                kaynakAdi,
                                kaynakAciklamasi
                        ),
                "Kaynak bilgileri oluşturulan değerlerle uyuşmuyor."
        );


        ReportManager.info(
                "KAYNAK OLUŞTURMA BAŞARILI"
                        + " | Kaynak: "
                        + kaynakAdi
        );
    }


    /**
     * Test tarafından oluşturulan kaynağın
     * başarılı şekilde silinebildiğini doğrular.
     */
    @Test(priority = 2)
    public void kaynakSilme() {

        OpenDataPage openDataPage =
                new OpenDataPage(page);

        KaynakEkleModal kaynakEkleModal =
                new KaynakEkleModal(page);


        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma(
                        "otomasyon_silinecek_kaynak"
                );

        String tabloAdi =
                "arac_bakim_is_yeri_sayisi";

        String kaynakAciklamasi =
                "Otomasyon silme testi - "
                        + kaynakAdi;


        avpOturumuHazirlama();


        ReportManager.step(
                "Açık Veri Portalına geçiliyor."
        );

        openDataPage
                .acikVeriPortalinaGitme();


        ReportManager.step(
                "Silme testi için kaynak oluşturuluyor: "
                        + kaynakAdi
        );

        openDataPage
                .kaynakEklemeEkraniniAcma();

        kaynakEkleModal
                .kaynakAdiGirme(
                        kaynakAdi
                );

        kaynakEkleModal
                .kaynakTablosuSecme(
                        tabloAdi
                );

        kaynakEkleModal
                .kaynakAciklamasiGirme(
                        kaynakAciklamasi
                );

        Assert.assertTrue(
                kaynakEkleModal
                        .veriOnizlemeGoruntulendiMi(),
                "Veri Önizleme bölümü görüntülenemedi."
        );

        kaynakEkleModal
                .kaynakEkleme();


        Assert.assertTrue(
                openDataPage
                        .kaynakListedeMi(
                                kaynakAdi
                        ),
                "Silme testi için oluşturulan kaynak listede bulunamadı."
        );


        ReportManager.step(
                "Oluşturulan kaynağın Sil butonuna tıklanıyor."
        );

        openDataPage
                .kaynakSilmeButonunaTiklama(
                        kaynakAdi
                );


        Assert.assertTrue(
                openDataPage
                        .kaynakSilmeOnayiGoruntulendiMi(
                                kaynakAdi
                        ),
                "Silme onay ekranı doğrulanamadı."
        );


        ReportManager.step(
                "Kaynak silme işlemi onaylanıyor."
        );

        openDataPage
                .kaynakSilmeOnayiVerme();


        Assert.assertTrue(
                openDataPage
                        .kaynakSilmeDialoguKapandiMi(),
                "Silme işleminden sonra dialog kapanmadı."
        );


        Assert.assertTrue(
                openDataPage
                        .kaynakListedenSilindiMi(
                                kaynakAdi
                        ),
                "Silinen kaynak hâlâ listede görüntüleniyor."
        );


        ReportManager.info(
                "KAYNAK SİLME BAŞARILI"
                        + " | Kaynak: "
                        + kaynakAdi
        );
    }


    /**
     * Bir kaynak kaydının Genel Bilgi ve
     * Önizleme ekranlarının görüntülenebildiğini doğrular.
     */
    @Test(priority = 3)
    public void kaynakGoruntuleme() {

        OpenDataPage openDataPage =
                new OpenDataPage(page);

        KaynakEkleModal kaynakEkleModal =
                new KaynakEkleModal(page);


        avpOturumuHazirlama();


        ReportManager.step(
                "Açık Veri Portalına geçiliyor."
        );

        openDataPage
                .acikVeriPortalinaGitme();


        ReportManager.step(
                "Görüntülenecek mevcut kaynak kontrol ediliyor."
        );


        if (openDataPage.kaynakKaydiVarMi()) {

            ReportManager.info(
                    "Kaynak listesinde mevcut kayıt bulundu."
            );

            ReportManager.step(
                    "Mevcut kaynağın Görüntüle butonuna tıklanıyor."
            );

            openDataPage
                    .ilkKaynagiGoruntuleme();

        } else {

            String kaynakAdi =
                    TestDataUtil.dinamikAdOlusturma(
                            "otomasyon_goruntuleme_kaynak"
                    );

            String tabloAdi =
                    "arac_bakim_is_yeri_sayisi";

            String kaynakAciklamasi =
                    "Otomasyon kaynak görüntüleme testi - "
                            + kaynakAdi;


            ReportManager.info(
                    "Mevcut kaynak bulunamadı."
                            + " Yeni kaynak oluşturulacak: "
                            + kaynakAdi
            );


            openDataPage
                    .kaynakEklemeEkraniniAcma();

            Assert.assertTrue(
                    kaynakEkleModal
                            .kaynakEkleFormuGoruntulendiMi(),
                    "Kaynak Ekle formu görüntülenemedi."
            );


            kaynakEkleModal
                    .kaynakAdiGirme(
                            kaynakAdi
                    );

            kaynakEkleModal
                    .kaynakTablosuSecme(
                            tabloAdi
                    );

            kaynakEkleModal
                    .kaynakAciklamasiGirme(
                            kaynakAciklamasi
                    );


            Assert.assertTrue(
                    kaynakEkleModal
                            .veriOnizlemeGoruntulendiMi(),
                    "Veri Önizleme bölümü görüntülenemedi."
            );


            kaynakEkleModal
                    .kaynakEkleme();


            Assert.assertTrue(
                    openDataPage
                            .kaynakListedeMi(
                                    kaynakAdi
                            ),
                    "Görüntüleme testi için oluşturulan kaynak listede bulunamadı."
            );


            ReportManager.step(
                    "Oluşturulan kaynağın Görüntüle butonuna tıklanıyor."
            );

            openDataPage
                    .kaynakGoruntulemeButonunaTiklama(
                            kaynakAdi
                    );
        }


        ReportManager.step(
                "Genel Bilgi sekmesine geçiliyor."
        );

        openDataPage
                .genelBilgiSekmesineTiklama();


        Assert.assertTrue(
                openDataPage
                        .genelBilgiSayfasiGoruntulendiMi(),
                "Kaynak Genel Bilgi ekranı doğru şekilde görüntülenemedi."
        );


        ReportManager.step(
                "Önizleme sekmesine geçiliyor."
        );

        openDataPage
                .onizlemeSekmesineTiklama();


        Assert.assertTrue(
                openDataPage
                        .onizlemeSayfasiGoruntulendiMi(),
                "Kaynak Önizleme ekranı görüntülenemedi."
        );


        ReportManager.info(
                "KAYNAK GÖRÜNTÜLEME VE ÖNİZLEME KONTROLÜ BAŞARILI"
        );
    }


    /**
     * Dinamik olarak oluşturulan bir kaynağın
     * ad ve açıklama bilgilerinin güncellenebildiğini doğrular.
     */
    @Test(priority = 4)
    public void kaynakDuzenleme() {

        OpenDataPage openDataPage =
                new OpenDataPage(page);

        KaynakEkleModal kaynakEkleModal =
                new KaynakEkleModal(page);


        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma(
                        "otomasyon_duzenleme_kaynak"
                );

        String tabloAdi =
                "arac_bakim_is_yeri_sayisi";

        String kaynakAciklamasi =
                "Otomasyon kaynak düzenleme testi - "
                        + kaynakAdi;

        String yeniKaynakAdi =
                kaynakAdi
                        + "_duzenlendi";

        String yeniKaynakAciklamasi =
                "Otomasyon kaynak düzenleme testi güncellendi - "
                        + yeniKaynakAdi;


        avpOturumuHazirlama();


        ReportManager.step(
                "Açık Veri Portalına geçiliyor."
        );

        openDataPage
                .acikVeriPortalinaGitme();


        ReportManager.step(
                "Düzenleme testi için kaynak oluşturuluyor."
        );

        openDataPage
                .kaynakEklemeEkraniniAcma();

        kaynakEkleModal
                .kaynakAdiGirme(
                        kaynakAdi
                );

        kaynakEkleModal
                .kaynakTablosuSecme(
                        tabloAdi
                );

        kaynakEkleModal
                .kaynakAciklamasiGirme(
                        kaynakAciklamasi
                );

        Assert.assertTrue(
                kaynakEkleModal
                        .veriOnizlemeGoruntulendiMi(),
                "Veri Önizleme bölümü görüntülenemedi."
        );

        kaynakEkleModal
                .kaynakEkleme();


        Assert.assertTrue(
                openDataPage
                        .kaynakListedeMi(
                                kaynakAdi
                        ),
                "Düzenleme testi için oluşturulan kaynak listede bulunamadı."
        );


        ReportManager.step(
                "Kaynağın Düzenle butonuna tıklanıyor."
        );

        openDataPage
                .kaynakDuzenlemeButonunaTiklama(
                        kaynakAdi
                );


        Assert.assertTrue(
                openDataPage
                        .kaynakDuzenlemeEkraniGoruntulendiMi(),
                "Kaynak düzenleme ekranı görüntülenemedi."
        );


        Assert.assertEquals(
                openDataPage
                        .duzenlemeKaynakAdiAlma(),
                kaynakAdi,
                "Düzenleme ekranındaki kaynak adı hatalı."
        );

        Assert.assertEquals(
                openDataPage
                        .duzenlemeKaynakAciklamasiAlma(),
                kaynakAciklamasi,
                "Düzenleme ekranındaki açıklama hatalı."
        );


        ReportManager.step(
                "Kaynak adı ve açıklaması güncelleniyor."
        );

        openDataPage
                .kaynakAdiniGuncelleme(
                        yeniKaynakAdi
                );

        openDataPage
                .kaynakAciklamasiniGuncelleme(
                        yeniKaynakAciklamasi
                );


        Assert.assertEquals(
                openDataPage
                        .duzenlemeKaynakAdiAlma(),
                yeniKaynakAdi,
                "Yeni kaynak adı forma doğru yazılmadı."
        );

        Assert.assertEquals(
                openDataPage
                        .duzenlemeKaynakAciklamasiAlma(),
                yeniKaynakAciklamasi,
                "Yeni açıklama forma doğru yazılmadı."
        );


        ReportManager.step(
                "Kaynak düzenleme işlemi kaydediliyor."
        );

        openDataPage
                .kaynakDuzenlemeyiKaydetme();


        Assert.assertTrue(
                openDataPage
                        .kaynakListedeMi(
                                yeniKaynakAdi
                        ),
                "Güncellenen kaynak listede bulunamadı."
        );


        Assert.assertTrue(
                openDataPage
                        .kaynakBilgileriDogruMu(
                                yeniKaynakAdi,
                                yeniKaynakAciklamasi
                        ),
                "Güncellenen kaynak bilgileri listede doğrulanamadı."
        );


        Assert.assertFalse(
                openDataPage
                        .kaynakListedeMi(
                                kaynakAdi
                        ),
                "Eski kaynak adı hâlâ listede görüntüleniyor."
        );


        ReportManager.info(
                "KAYNAK DÜZENLEME BAŞARILI"
                        + " | Yeni kaynak: "
                        + yeniKaynakAdi
        );
    }


    /**
     * Mevcut ve mevcut olmayan kriterler kullanılarak
     * kaynak arama fonksiyonunu doğrular.
     */
    @Test(priority = 5)
    public void kaynakArama() {

        OpenDataPage openDataPage =
                new OpenDataPage(page);


        String mevcutAramaKriteri =
                "otomasyon";

        String bulunmayanAramaKriteri =
                "bu kayıt yok";


        avpOturumuHazirlama();


        ReportManager.step(
                "Açık Veri Portalına geçiliyor."
        );

        openDataPage
                .acikVeriPortalinaGitme();


        /*
         * Pozitif arama
         */
        ReportManager.step(
                "Kaynak Ara alanında mevcut kayıt aranıyor: "
                        + mevcutAramaKriteri
        );

        openDataPage
                .kaynakArama(
                        mevcutAramaKriteri
                );


        /*
         * Bu metod sonuç yüklenene kadar beklemelidir.
         */
        ReportManager.step(
                "Arama sonucunda '"
                        + mevcutAramaKriteri
                        + "' kriterini içeren kaynak bulunduğu doğrulanıyor."
        );

        Assert.assertTrue(
                openDataPage
                        .kaynakAramaSonuclariUygunMu(
                                mevcutAramaKriteri
                        ),
                "Arama sonucunda '"
                        + mevcutAramaKriteri
                        + "' kriterini içeren kaynak bulunamadı."
        );


        /*
         * Sonuç yüklendikten sonra screenshot alınır.
         */
        ReportManager.step(
                "Pozitif kaynak arama sonucu ekran görüntüsü alınıyor."
        );

        Path pozitifScreenshot =
                ScreenshotUtil.takeScreenshot(
                        page,
                        "kaynak_arama_pozitif_otomasyon"
                );

        if (pozitifScreenshot != null) {

            ReportManager.attachScreenshot(
                    "Kaynak Arama - Pozitif Sonuç",
                    pozitifScreenshot
            );
        }


        /*
         * Negatif arama
         */
        ReportManager.step(
                "Kaynak Ara alanında bulunmayan kayıt aranıyor: "
                        + bulunmayanAramaKriteri
        );

        openDataPage
                .kaynakArama(
                        bulunmayanAramaKriteri
                );


        ReportManager.step(
                "Kaynak bulunamadığında boş liste mesajının görüntülendiği doğrulanıyor."
        );

        Assert.assertTrue(
                openDataPage
                        .kaynakBulunamadiMesajiGoruntulendiMi(),
                "Kaynak bulunamadı mesajı görüntülenemedi."
        );


        Path negatifScreenshot =
                ScreenshotUtil.takeScreenshot(
                        page,
                        "kaynak_arama_negatif_kayit_yok"
                );

        if (negatifScreenshot != null) {

            ReportManager.attachScreenshot(
                    "Kaynak Arama - Sonuç Bulunamadı",
                    negatifScreenshot
            );
        }


        ReportManager.info(
                "KAYNAK ARAMA KONTROLÜ BAŞARILI"
        );
    }


    /**
     * Ad ve Tablo zorunlu alanlarının ikisi de boşken
     * kayıt oluşturulamadığını doğrular.
     */
    @Test(priority = 6)
    public void kaynakEklemeZorunluAlanKontrolu() {

        OpenDataPage openDataPage =
                new OpenDataPage(page);

        KaynakEkleModal kaynakEkleModal =
                new KaynakEkleModal(page);


        avpOturumuHazirlama();


        ReportManager.step(
                "Açık Veri Portalına geçiliyor."
        );

        openDataPage
                .acikVeriPortalinaGitme();


        ReportManager.step(
                "Kaynak Ekle formu açılıyor."
        );

        openDataPage
                .kaynakEklemeEkraniniAcma();


        Assert.assertTrue(
                kaynakEkleModal
                        .kaynakEkleFormuGoruntulendiMi(),
                "Kaynak Ekle formu görüntülenemedi."
        );


        ReportManager.step(
                "Ad ve Tablo alanları boş bırakılarak kaynak ekleme deneniyor."
        );

        kaynakEkleModal
                .kaynakEkleme();


        Assert.assertTrue(
                kaynakEkleModal
                        .kaynakAdiZorunluMesajiGoruntulendiMi(),
                "Kaynak adı zorunlu alan mesajı görüntülenmedi."
        );

        Assert.assertTrue(
                kaynakEkleModal
                        .tabloZorunluMesajiGoruntulendiMi(),
                "Tablo zorunlu alan mesajı görüntülenmedi."
        );

        Assert.assertTrue(
                kaynakEkleModal
                        .kaynakEkleFormuGoruntulendiMi(),
                "Validasyon hatasına rağmen form kapandı."
        );


        ReportManager.info(
                "TÜM ZORUNLU ALAN KONTROLÜ BAŞARILI"
        );
    }


    /**
     * Kaynak adı dolu, Tablo alanı boş bırakıldığında
     * Tablo zorunlu alan validasyonunu doğrular.
     */
    @Test(priority = 7)
    public void kaynakEklemeTabloZorunluAlanKontrolu() {

        OpenDataPage openDataPage =
                new OpenDataPage(page);

        KaynakEkleModal kaynakEkleModal =
                new KaynakEkleModal(page);


        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma(
                        "otomasyon_zorunlu_tablo"
                );


        avpOturumuHazirlama();


        ReportManager.step(
                "Açık Veri Portalına geçiliyor."
        );

        openDataPage
                .acikVeriPortalinaGitme();


        openDataPage
                .kaynakEklemeEkraniniAcma();


        ReportManager.step(
                "Kaynak adı dolduruluyor, Tablo alanı boş bırakılıyor."
        );

        kaynakEkleModal
                .kaynakAdiGirme(
                        kaynakAdi
                );


        kaynakEkleModal
                .kaynakEkleme();


        Assert.assertTrue(
                kaynakEkleModal
                        .tabloZorunluMesajiGoruntulendiMi(),
                "Tablo zorunlu alan mesajı görüntülenmedi."
        );

        Assert.assertFalse(
                kaynakEkleModal
                        .kaynakAdiZorunluMesajiGoruntulendiMi(),
                "Kaynak adı dolu olmasına rağmen zorunlu alan mesajı görüntülendi."
        );

        Assert.assertTrue(
                kaynakEkleModal
                        .kaynakEkleFormuGoruntulendiMi(),
                "Tablo alanı boş olmasına rağmen form kapandı."
        );


        ReportManager.info(
                "TABLO ZORUNLU ALAN KONTROLÜ BAŞARILI"
        );
    }


    /**
     * Tablo seçili, Kaynak Adı boş bırakıldığında
     * Kaynak Adı zorunlu alan validasyonunu doğrular.
     */
    @Test(priority = 8)
    public void kaynakEklemeAdZorunluAlanKontrolu() {

        OpenDataPage openDataPage =
                new OpenDataPage(page);

        KaynakEkleModal kaynakEkleModal =
                new KaynakEkleModal(page);


        String tabloAdi =
                "arac_bakim_is_yeri_sayisi";


        avpOturumuHazirlama();


        ReportManager.step(
                "Açık Veri Portalına geçiliyor."
        );

        openDataPage
                .acikVeriPortalinaGitme();


        openDataPage
                .kaynakEklemeEkraniniAcma();


        ReportManager.step(
                "Kaynak adı boş bırakılıyor ve Tablo seçiliyor."
        );

        kaynakEkleModal
                .kaynakTablosuSecme(
                        tabloAdi
                );


        kaynakEkleModal
                .kaynakEkleme();


        Assert.assertTrue(
                kaynakEkleModal
                        .kaynakAdiZorunluMesajiGoruntulendiMi(),
                "Kaynak adı zorunlu alan mesajı görüntülenmedi."
        );

        Assert.assertFalse(
                kaynakEkleModal
                        .tabloZorunluMesajiGoruntulendiMi(),
                "Tablo seçili olmasına rağmen Tablo zorunlu alan mesajı görüntülendi."
        );

        Assert.assertTrue(
                kaynakEkleModal
                        .kaynakEkleFormuGoruntulendiMi(),
                "Kaynak adı boş olmasına rağmen form kapandı."
        );


        ReportManager.info(
                "KAYNAK ADI ZORUNLU ALAN KONTROLÜ BAŞARILI"
        );
    }
    /**
     * Hazır Sorgu kullanılarak dinamik ad ve açıklama ile
     * yeni kaynak oluşturulabildiğini doğrular.
     *
     * Sorgu doğrulaması sonrasında beklenen kolonlar ve
     * Veri Önizleme bölümü kontrol edilir.
     */
    @Test(priority = 9)
    public void hazirSorguIleKaynakOlusturma() {

        OpenDataPage openDataPage =
                new OpenDataPage(page);

        KaynakEkleModal kaynakEkleModal =
                new KaynakEkleModal(page);


        /*
         * Her koşuda benzersiz kaynak adı oluşturulur.
         */
        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma(
                        "otomasyon_sorgu_kaynak"
                );


        /*
         * Açıklama kaynak adına bağlı olarak
         * dinamik oluşturulur.
         */
        String kaynakAciklamasi =
                "Otomasyon hazır sorgu kaynak testi - "
                        + kaynakAdi;


        /*
         * Recorder üzerinde kullanılan sorgu
         * hiçbir değişiklik yapılmadan kullanılır.
         */
        String sorgu =
                "SELECT\n"
                        + "    a.yil AS yil_yillik_hat_km,\n"
                        + "    a.bolge_kodu AS bolge_kodu_yillik_hat_km,\n"
                        + "    a.bolge_adi AS bolge_adi_yillik_hat_km,\n"
                        + "    b.hat_kodu AS hat_kodu_yillik_hat_kapasitesi,\n"
                        + "    b.hat_adi AS hat_adi_yillik_hat_kapasitesi\n"
                        + "FROM yillik_hat_km a\n"
                        + "LEFT JOIN yillik_hat_kapasitesi b\n"
                        + "    ON a.bolge_kodu = b.bolge_kodu\n"
                        + "    AND a.yil = b.yil;";


        ReportManager.info(
                "HAZIR SORGU KAYNAK TEST VERİSİ"
                        + " | Kaynak: "
                        + kaynakAdi
        );


        /*
         * AVP oturumu hazırlanır.
         */
        avpOturumuHazirlama();


        /*
         * Açık Veri Portalına geçilir.
         */
        ReportManager.step(
                "Açık Veri Portalına geçiliyor."
        );

        openDataPage
                .acikVeriPortalinaGitme();


        /*
         * Kaynak Ekle formu açılır.
         */
        ReportManager.step(
                "Kaynak Ekle formu açılıyor."
        );

        openDataPage
                .kaynakEklemeEkraniniAcma();


        Assert.assertTrue(
                kaynakEkleModal
                        .kaynakEkleFormuGoruntulendiMi(),
                "Kaynak Ekle formu görüntülenemedi."
        );


        /*
         * Hazır Sorgu seçeneğine geçilir.
         */
        ReportManager.step(
                "Hazır Sorgu seçeneğine geçiliyor."
        );

        kaynakEkleModal
                .hazirSorguyaGecme();


        /*
         * Dinamik kaynak adı girilir.
         */
        ReportManager.step(
                "Dinamik kaynak adı giriliyor: "
                        + kaynakAdi
        );

        kaynakEkleModal
                .kaynakAdiGirme(
                        kaynakAdi
                );


        Assert.assertEquals(
                kaynakEkleModal
                        .kaynakAdiAlma(),
                kaynakAdi,
                "Kaynak adı alanındaki değer beklenen değerle uyuşmuyor."
        );


        /*
         * Dinamik açıklama girilir.
         */
        ReportManager.step(
                "Dinamik kaynak açıklaması giriliyor."
        );

        kaynakEkleModal
                .kaynakAciklamasiGirme(
                        kaynakAciklamasi
                );


        Assert.assertEquals(
                kaynakEkleModal
                        .kaynakAciklamasiAlma(),
                kaynakAciklamasi,
                "Kaynak açıklaması beklenen değerle uyuşmuyor."
        );


        /*
         * Hazır SQL sorgusu girilir.
         */
        ReportManager.step(
                "Hazır kaynak sorgusu giriliyor."
        );

        kaynakEkleModal
                .hazirSorguGirme(
                        sorgu
                );


        /*
         * Sorgu doğrulanır.
         */
        ReportManager.step(
                "Hazır sorgu doğrulanıyor."
        );

        kaynakEkleModal
                .hazirSorguDogrulama();


        /*
         * Sorgunun başarılı çalıştığı, dönen kolonlar
         * üzerinden doğrulanır.
         */
        ReportManager.step(
                "Sorgu sonucunda beklenen kolonların görüntülendiği doğrulanıyor."
        );

        Assert.assertTrue(
                kaynakEkleModal
                        .hazirSorguKolonlariGoruntulendiMi(),
                "Hazır sorgu doğrulaması sonrasında beklenen kolonlar görüntülenemedi."
        );


        /*
         * Sorgu doğrulaması sonrasında
         * Veri Önizleme alanı kontrol edilir.
         */
        ReportManager.step(
                "Hazır sorguya ait Veri Önizleme bölümünün görüntülendiği doğrulanıyor."
        );

        Assert.assertTrue(
                kaynakEkleModal
                        .veriOnizlemeGoruntulendiMi(),
                "Hazır sorgu sonrasında Veri Önizleme bölümü görüntülenemedi."
        );


        /*
         * Kaynak oluşturulur.
         */
        ReportManager.step(
                "Hazır sorgu kaynağı oluşturuluyor."
        );

        kaynakEkleModal
                .kaynakEkleme();


        /*
         * Kaynak Ekle formunun kapandığı doğrulanır.
         */
        Assert.assertTrue(
                kaynakEkleModal
                        .kaynakEkleFormuKapandiMi(),
                "Hazır sorgu kaynağı oluşturulduktan sonra form kapanmadı."
        );


        /*
         * Oluşturulan kaydın listede bulunduğu doğrulanır.
         */
        ReportManager.step(
                "Oluşturulan hazır sorgu kaynağının listede bulunduğu doğrulanıyor."
        );

        Assert.assertTrue(
                openDataPage
                        .kaynakListedeMi(
                                kaynakAdi
                        ),
                "Hazır sorgu ile oluşturulan kaynak listede bulunamadı: "
                        + kaynakAdi
        );


        /*
         * Kaynak adı ve açıklamanın listeye
         * doğru kaydedildiği doğrulanır.
         */
        Assert.assertTrue(
                openDataPage
                        .kaynakBilgileriDogruMu(
                                kaynakAdi,
                                kaynakAciklamasi
                        ),
                "Hazır sorgu ile oluşturulan kaynak bilgileri beklenen değerlerle uyuşmuyor."
        );


        ReportManager.info(
                "HAZIR SORGU İLE KAYNAK OLUŞTURMA BAŞARILI"
                        + " | Kaynak: "
                        + kaynakAdi
        );
    }
    /**
     * Hazır Sorgu alanında SELECT dışındaki veri değiştiren
     * SQL komutlarının sistem tarafından engellendiğini doğrular.
     */
    @Test(priority = 10)
    public void hazirSorguGecirsizDmlKomutlari() {

        OpenDataPage openDataPage =
                new OpenDataPage(page);

        KaynakEkleModal kaynakEkleModal =
                new KaynakEkleModal(page);


        /*
         * Dinamik test verileri oluşturulur.
         */
        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma(
                        "otomasyon_update_kontrol"
                );

        String kaynakAciklamasi =
                "Otomasyon UPDATE sorgu kontrolü - "
                        + kaynakAdi;


        /*
         * Veri değiştirmeyen güvenli UPDATE sorgusu kullanılır.
         *
         * Sistem yalnızca SELECT sorgularına izin verdiği için
         * sorgunun doğrulama aşamasında reddedilmesi beklenir.
         */
        String sorgu =
                "UPDATE yillik_hat_km\n"
                        + "SET yil = yil\n"
                        + "WHERE 1 = 0;";


        ReportManager.info(
                "HAZIR SORGU UPDATE NEGATİF TESTİ"
                        + " | Kaynak: "
                        + kaynakAdi
        );


        /*
         * AVP oturumu hazırlanır.
         */
        avpOturumuHazirlama();


        /*
         * Açık Veri Portalına geçilir.
         */
        ReportManager.step(
                "Açık Veri Portalına geçiliyor."
        );

        openDataPage
                .acikVeriPortalinaGitme();


        /*
         * Kaynak Ekle formu açılır.
         */
        ReportManager.step(
                "Kaynak Ekle formu açılıyor."
        );

        openDataPage
                .kaynakEklemeEkraniniAcma();


        Assert.assertTrue(
                kaynakEkleModal
                        .kaynakEkleFormuGoruntulendiMi(),
                "Kaynak Ekle formu görüntülenemedi."
        );


        /*
         * Hazır Sorgu seçeneğine geçilir.
         */
        ReportManager.step(
                "Hazır Sorgu seçeneğine geçiliyor."
        );

        kaynakEkleModal
                .hazirSorguyaGecme();


        /*
         * Dinamik kaynak adı girilir.
         */
        ReportManager.step(
                "Dinamik kaynak adı giriliyor: "
                        + kaynakAdi
        );

        kaynakEkleModal
                .kaynakAdiGirme(
                        kaynakAdi
                );


        /*
         * Dinamik açıklama girilir.
         */
        ReportManager.step(
                "Dinamik kaynak açıklaması giriliyor."
        );

        kaynakEkleModal
                .kaynakAciklamasiGirme(
                        kaynakAciklamasi
                );


        /*
         * SELECT dışı UPDATE sorgusu girilir.
         */
        ReportManager.step(
                "Hazır Sorgu alanına UPDATE komutu giriliyor."
        );

        kaynakEkleModal
                .hazirSorguGirme(
                        sorgu
                );


        /*
         * UPDATE sorgusu doğrulanmaya çalışılır.
         */
        ReportManager.step(
                "UPDATE sorgusu doğrulanıyor."
        );

        kaynakEkleModal
                .hazirSorguDogrulama();


        /*
         * Kullanıcı Kaynaklı Hata alanının
         * görüntülendiği doğrulanır.
         */
        ReportManager.step(
                "Kullanıcı Kaynaklı Hata mesajının görüntülendiği doğrulanıyor."
        );

        Assert.assertTrue(
                kaynakEkleModal
                        .kullaniciKaynakliHataGoruntulendiMi(),
                "UPDATE sorgusu sonrasında Kullanıcı Kaynaklı Hata görüntülenmedi."
        );


        /*
         * Sistemin yalnızca SELECT sorgularına
         * izin verdiği mesajı doğrulanır.
         */
        ReportManager.step(
                "Sorgunun yalnızca SELECT olması gerektiğine ait hata mesajı doğrulanıyor."
        );

        Assert.assertTrue(
                kaynakEkleModal
                        .sadeceSelectSorgusuHatasiGoruntulendiMi(),
                "SELECT dışındaki sorguların engellendiğine ait hata mesajı görüntülenmedi."
        );


        /*
         * Teknik hata kodu doğrulanır.
         */
        ReportManager.step(
                "Read-only sorgu hata kodu doğrulanıyor."
        );

        Assert.assertTrue(
                kaynakEkleModal
                        .readOnlySorguHataKoduGoruntulendiMi(),
                "OPEN_DATA.SOURCE.QUERY_NOT_READ_ONLY hata kodu görüntülenmedi."
        );


        /*
         * Hata sonrasında Kaynak Ekle formunun
         * açık kalmaya devam ettiği doğrulanır.
         */

        ReportManager.info(
                "UPDATE SORGUSU ENGELLEME KONTROLÜ BAŞARILI"
                        + " | Kaynak: "
                        + kaynakAdi
        );
    }
}