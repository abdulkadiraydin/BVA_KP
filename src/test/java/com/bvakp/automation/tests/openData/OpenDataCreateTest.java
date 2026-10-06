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
     * Dinamik test verisi kullanarak yeni bir kaynak oluşturur.
     *
     * Kaynak oluşturma sürecinde form alanları,
     * girilen değerler, veri önizleme alanı ve
     * kayıt sonrasındaki liste bilgileri doğrulanır.
     */
    @Test(priority = 1)
    public void kaynakOlusturma() {

        OpenDataPage openDataPage =
                new OpenDataPage(page);

        KaynakEkleModal kaynakEkleModal =
                new KaynakEkleModal(page);


        /*
         * Her test koşusunda benzersiz kaynak adı oluşturulur.
         */
        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma(
                        "otomasyon_kaynak"
                );


        /*
         * Recorder üzerinde doğrulanan kaynak tablo kullanılır.
         */
        String tabloAdi =
                "arac_bakim_is_yeri_sayisi";


        /*
         * Açıklama kaynak adıyla ilişkilendirilerek
         * her koşuda benzersiz hale getirilir.
         */
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


        /*
         * Geçerli AVP oturumu hazırlanır.
         *
         * Auth state geçerliyse mevcut oturum kullanılır.
         * Geçersizse Keycloak üzerinden login gerçekleştirilir.
         */
        avpOturumuHazirlama();


        /*
         * Açık Veri Portalı ekranına geçilir.
         */
        ReportManager.step(
                "Açık Veri Portalına geçiliyor."
        );

        openDataPage
                .acikVeriPortalinaGitme();


        /*
         * Kaynak yönetim alanının açıldığı doğrulanır.
         */
        ReportManager.step(
                "Kaynak Tablo / Sorgu bölümünün görüntülendiği doğrulanıyor."
        );

        Assert.assertTrue(
                openDataPage
                        .kaynakTabloSorguGoruntulendiMi(),
                "Kaynak Tablo / Sorgu bölümü görüntülenemedi."
        );


        /*
         * Kaynak Ekle formu açılır.
         */
        ReportManager.step(
                "Kaynak Ekle formu açılıyor."
        );

        openDataPage
                .kaynakEklemeEkraniniAcma();


        /*
         * Kaynak oluşturma formunun gerekli
         * alanlarla açıldığı doğrulanır.
         */
        ReportManager.step(
                "Kaynak Ekle formundaki Ad, Tablo ve Açıklama alanları doğrulanıyor."
        );

        Assert.assertTrue(
                kaynakEkleModal
                        .kaynakEkleFormuGoruntulendiMi(),
                "Kaynak Ekle formu gerekli alanlarla birlikte görüntülenemedi."
        );


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
         * Ad alanındaki değerin doğru yazıldığı doğrulanır.
         */
        ReportManager.step(
                "Kaynak adı alanına girilen değer doğrulanıyor."
        );

        Assert.assertEquals(
                kaynakEkleModal
                        .kaynakAdiAlma(),
                kaynakAdi,
                "Kaynak adı alanındaki değer beklenen değerle uyuşmuyor."
        );


        /*
         * Kaynak tablosu seçilir.
         */
        ReportManager.step(
                "Kaynak tablosu seçiliyor: "
                        + tabloAdi
        );

        kaynakEkleModal
                .kaynakTablosuSecme(
                        tabloAdi
                );


        /*
         * Seçilen kaynak tablosu doğrulanır.
         */
        ReportManager.step(
                "Seçilen kaynak tablosu doğrulanıyor."
        );

        Assert.assertEquals(
                kaynakEkleModal
                        .seciliTabloyuAlma(),
                tabloAdi,
                "Seçilen kaynak tablosu beklenen tablo ile uyuşmuyor."
        );


        /*
         * Dinamik kaynak açıklaması girilir.
         */
        ReportManager.step(
                "Dinamik kaynak açıklaması giriliyor."
        );

        kaynakEkleModal
                .kaynakAciklamasiGirme(
                        kaynakAciklamasi
                );


        /*
         * Açıklama alanına yazılan değer doğrulanır.
         */
        ReportManager.step(
                "Kaynak açıklaması alanındaki değer doğrulanıyor."
        );

        Assert.assertEquals(
                kaynakEkleModal
                        .kaynakAciklamasiAlma(),
                kaynakAciklamasi,
                "Kaynak açıklaması beklenen değerle uyuşmuyor."
        );


        /*
         * Tablo seçimi sonrasında veri önizlemesinin
         * oluşturulduğu doğrulanır.
         */
        ReportManager.step(
                "Seçilen kaynak tablosuna ait Veri Önizleme bölümünün görüntülendiği doğrulanıyor."
        );

        Assert.assertTrue(
                kaynakEkleModal
                        .veriOnizlemeGoruntulendiMi(),
                "Veri Önizleme bölümü görüntülenemedi."
        );


        /*
         * Kaynak oluşturma işlemi tamamlanır.
         */
        ReportManager.step(
                "Kaynak Ekle butonuna tıklanıyor."
        );

        kaynakEkleModal
                .kaynakEkleme();


        /*
         * Kaydetme işlemi sonrası formun
         * kapandığı doğrulanır.
         */
        ReportManager.step(
                "Kaynak Ekle formunun kapandığı doğrulanıyor."
        );

        Assert.assertTrue(
                kaynakEkleModal
                        .kaynakEkleFormuKapandiMi(),
                "Kaynak oluşturma işleminden sonra Kaynak Ekle formu kapanmadı."
        );


        /*
         * Oluşturulan kaynağın kaynak listesine
         * eklendiği doğrulanır.
         */
        ReportManager.step(
                "Oluşturulan kaynağın listede bulunduğu doğrulanıyor: "
                        + kaynakAdi
        );

        Assert.assertTrue(
                openDataPage
                        .kaynakListedeMi(
                                kaynakAdi
                        ),
                "Oluşturulan kaynak listede bulunamadı: "
                        + kaynakAdi
        );


        /*
         * Listede bulunan kaynağın adı ve açıklamasının
         * oluşturma sırasında girilen değerlerle aynı
         * olduğu doğrulanır.
         */
        ReportManager.step(
                "Oluşturulan kaynağın liste üzerindeki ad ve açıklama bilgileri doğrulanıyor."
        );

        Assert.assertTrue(
                openDataPage
                        .kaynakBilgileriDogruMu(
                                kaynakAdi,
                                kaynakAciklamasi
                        ),
                "Kaynak satırındaki bilgiler oluşturulan değerlerle uyuşmuyor."
        );


        ReportManager.info(
                "KAYNAK OLUŞTURMA BAŞARILI"
                        + " | Kaynak: "
                        + kaynakAdi
                        + " | Tablo: "
                        + tabloAdi
        );
    }
    /**
     * Test tarafından oluşturulan dinamik kaynağın
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


        ReportManager.info(
                "SİLİNECEK TEST KAYNAĞI"
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
         * Test kendi sileceği kaynağı oluşturur.
         */
        ReportManager.step(
                "Silme testi için dinamik kaynak oluşturuluyor: "
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


        /*
         * Kaydın gerçekten oluştuğu doğrulanır.
         */
        ReportManager.step(
                "Silinecek kaynağın listede oluştuğu doğrulanıyor."
        );

        Assert.assertTrue(
                openDataPage
                        .kaynakListedeMi(
                                kaynakAdi
                        ),
                "Silme testi için oluşturulan kaynak listede bulunamadı: "
                        + kaynakAdi
        );


        /*
         * Sadece oluşturulan dinamik kaydın
         * Sil aksiyonu çalıştırılır.
         */
        ReportManager.step(
                "Oluşturulan kaynağın Sil butonuna tıklanıyor: "
                        + kaynakAdi
        );

        openDataPage
                .kaynakSilmeButonunaTiklama(
                        kaynakAdi
                );


        /*
         * Silme dialogunda doğru kayıt kontrol edilir.
         */
        ReportManager.step(
                "Kaynağı Sil onay ekranında doğru kaynak adı doğrulanıyor."
        );

        Assert.assertTrue(
                openDataPage
                        .kaynakSilmeOnayiGoruntulendiMi(
                                kaynakAdi
                        ),
                "Kaynağı Sil onay ekranı veya kaynak adı doğrulanamadı."
        );


        /*
         * Silme işlemi onaylanır.
         */
        ReportManager.step(
                "Kaynak silme işlemi onaylanıyor."
        );

        openDataPage
                .kaynakSilmeOnayiVerme();


        /*
         * Dialogun kapandığı doğrulanır.
         */
        ReportManager.step(
                "Silme dialogunun kapandığı doğrulanıyor."
        );

        Assert.assertTrue(
                openDataPage
                        .kaynakSilmeDialoguKapandiMi(),
                "Silme işlemi sonrasında dialog kapanmadı."
        );


        /*
         * Silinen kaydın artık listede bulunmadığı doğrulanır.
         */
        ReportManager.step(
                "Silinen kaynağın listeden kaldırıldığı doğrulanıyor: "
                        + kaynakAdi
        );

        Assert.assertTrue(
                openDataPage
                        .kaynakListedenSilindiMi(
                                kaynakAdi
                        ),
                "Silinen kaynak hâlâ listede görüntüleniyor: "
                        + kaynakAdi
        );


        ReportManager.info(
                "KAYNAK SİLME BAŞARILI"
                        + " | Kaynak: "
                        + kaynakAdi
        );
    }
    /**
     * Kaynak Tablo / Sorgu listesindeki bir kaynağın
     * Genel Bilgi ve Önizleme ekranlarının
     * görüntülenebildiğini doğrular.
     *
     * Sistemde hiç kaynak bulunmuyorsa test kendi
     * dinamik kaynağını oluşturur.
     */
    @Test(priority = 3)
    public void kaynakGoruntuleme() {

        OpenDataPage openDataPage =
                new OpenDataPage(page);

        KaynakEkleModal kaynakEkleModal =
                new KaynakEkleModal(page);


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
         * Kaynak listesinde mevcut kayıt kontrol edilir.
         */
        ReportManager.step(
                "Kaynak Tablo / Sorgu listesinde görüntülenecek kaynak kontrol ediliyor."
        );


        if (openDataPage.kaynakKaydiVarMi()) {

            /*
             * Mevcut kayıt varsa salt-okuma amacıyla
             * mevcut kaynaklardan biri görüntülenir.
             */
            ReportManager.info(
                    "Kaynak Tablo / Sorgu listesinde mevcut kayıt bulundu."
            );

            ReportManager.step(
                    "Mevcut kaynağın Görüntüle butonuna tıklanıyor."
            );

            openDataPage
                    .ilkKaynagiGoruntuleme();

        } else {

            /*
             * Kaynak bulunmuyorsa test kendi
             * dinamik kaynağını oluşturur.
             */
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
                    "Kaynak bulunamadı. "
                            + "Görüntüleme testi için yeni kaynak oluşturulacak: "
                            + kaynakAdi
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
                    "Dinamik kaynak adı giriliyor: "
                            + kaynakAdi
            );

            kaynakEkleModal
                    .kaynakAdiGirme(
                            kaynakAdi
                    );


            ReportManager.step(
                    "Kaynak tablosu seçiliyor: "
                            + tabloAdi
            );

            kaynakEkleModal
                    .kaynakTablosuSecme(
                            tabloAdi
                    );


            ReportManager.step(
                    "Kaynak açıklaması giriliyor."
            );

            kaynakEkleModal
                    .kaynakAciklamasiGirme(
                            kaynakAciklamasi
                    );


            Assert.assertTrue(
                    kaynakEkleModal
                            .veriOnizlemeGoruntulendiMi(),
                    "Kaynak oluşturma sırasında Veri Önizleme görüntülenemedi."
            );


            ReportManager.step(
                    "Kaynak oluşturuluyor."
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


        /*
         * Genel Bilgi ekranı kontrol edilir.
         */
        ReportManager.step(
                "Genel Bilgi sekmesine geçiliyor."
        );

        openDataPage
                .genelBilgiSekmesineTiklama();


        ReportManager.step(
                "Genel Bilgi ekranında Kaynak Bilgileri ve Kayıt Bilgileri alanları doğrulanıyor."
        );

        Assert.assertTrue(
                openDataPage
                        .genelBilgiSayfasiGoruntulendiMi(),
                "Kaynak Genel Bilgi ekranı doğru şekilde görüntülenemedi."
        );


        /*
         * Önizleme ekranı kontrol edilir.
         */
        ReportManager.step(
                "Önizleme sekmesine geçiliyor."
        );

        openDataPage
                .onizlemeSekmesineTiklama();


        ReportManager.step(
                "Kaynak Önizleme ekranının görüntülendiği doğrulanıyor."
        );

        Assert.assertTrue(
                openDataPage
                        .onizlemeSayfasiGoruntulendiMi(),
                "Kaynak Önizleme ekranı veya önizleme verileri görüntülenemedi."
        );


        ReportManager.info(
                "KAYNAK GÖRÜNTÜLEME VE ÖNİZLEME KONTROLÜ BAŞARILI"
        );
    }
    /**
     * Test tarafından oluşturulan bir kaynağın
     * ad ve açıklama bilgilerinin güncellenebildiğini doğrular.
     */
    @Test(priority = 4)
    public void kaynakDuzenleme() {

        OpenDataPage openDataPage =
                new OpenDataPage(page);

        KaynakEkleModal kaynakEkleModal =
                new KaynakEkleModal(page);


        /*
         * Test için benzersiz kaynak bilgileri oluşturulur.
         */
        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma(
                        "otomasyon_duzenleme_kaynak"
                );

        String tabloAdi =
                "arac_bakim_is_yeri_sayisi";

        String kaynakAciklamasi =
                "Otomasyon kaynak düzenleme testi - "
                        + kaynakAdi;


        /*
         * Düzenleme sonrasında kullanılacak
         * yeni değerler hazırlanır.
         */
        String yeniKaynakAdi =
                kaynakAdi
                        + "_duzenlendi";

        String yeniKaynakAciklamasi =
                "Otomasyon kaynak düzenleme testi güncellendi - "
                        + yeniKaynakAdi;


        ReportManager.info(
                "DÜZENLENECEK TEST KAYNAĞI"
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
         * Test kendi düzenleyeceği kaynağı oluşturur.
         */
        ReportManager.step(
                "Düzenleme testi için yeni kaynak oluşturuluyor: "
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
                "Kaynak oluşturma sırasında Veri Önizleme görüntülenemedi."
        );


        kaynakEkleModal
                .kaynakEkleme();


        /*
         * Kaynağın gerçekten oluşturulduğu doğrulanır.
         */
        ReportManager.step(
                "Düzenlenecek kaynağın listede bulunduğu doğrulanıyor."
        );

        Assert.assertTrue(
                openDataPage
                        .kaynakListedeMi(
                                kaynakAdi
                        ),
                "Düzenleme testi için oluşturulan kaynak listede bulunamadı."
        );


        /*
         * Oluşturulan kaydın Düzenle aksiyonu açılır.
         */
        ReportManager.step(
                "Kaynağın Düzenle butonuna tıklanıyor: "
                        + kaynakAdi
        );

        openDataPage
                .kaynakDuzenlemeButonunaTiklama(
                        kaynakAdi
                );


        /*
         * Düzenleme ekranının açıldığı doğrulanır.
         */
        ReportManager.step(
                "Kaynak düzenleme ekranının açıldığı doğrulanıyor."
        );

        Assert.assertTrue(
                openDataPage
                        .kaynakDuzenlemeEkraniGoruntulendiMi(),
                "Kaynak düzenleme ekranı görüntülenemedi."
        );


        /*
         * Düzenleme öncesinde formdaki mevcut değerlerin
         * oluşturulan kaynakla aynı olduğu doğrulanır.
         */
        ReportManager.step(
                "Düzenleme ekranındaki mevcut kaynak adı doğrulanıyor."
        );

        Assert.assertEquals(
                openDataPage
                        .duzenlemeKaynakAdiAlma(),
                kaynakAdi,
                "Düzenleme ekranındaki kaynak adı beklenen değerle uyuşmuyor."
        );


        ReportManager.step(
                "Düzenleme ekranındaki mevcut kaynak açıklaması doğrulanıyor."
        );

        Assert.assertEquals(
                openDataPage
                        .duzenlemeKaynakAciklamasiAlma(),
                kaynakAciklamasi,
                "Düzenleme ekranındaki açıklama beklenen değerle uyuşmuyor."
        );


        /*
         * Kaynak adı değiştirilir.
         */
        ReportManager.step(
                "Kaynak adı güncelleniyor: "
                        + yeniKaynakAdi
        );

        openDataPage
                .kaynakAdiniGuncelleme(
                        yeniKaynakAdi
                );


        /*
         * Açıklama değiştirilir.
         */
        ReportManager.step(
                "Kaynak açıklaması güncelleniyor."
        );

        openDataPage
                .kaynakAciklamasiniGuncelleme(
                        yeniKaynakAciklamasi
                );


        /*
         * Form üzerindeki yeni değerler
         * kaydetmeden önce doğrulanır.
         */
        ReportManager.step(
                "Güncellenen kaynak adı ve açıklaması kaydetmeden önce doğrulanıyor."
        );

        Assert.assertEquals(
                openDataPage
                        .duzenlemeKaynakAdiAlma(),
                yeniKaynakAdi,
                "Güncellenen kaynak adı forma doğru yazılmadı."
        );

        Assert.assertEquals(
                openDataPage
                        .duzenlemeKaynakAciklamasiAlma(),
                yeniKaynakAciklamasi,
                "Güncellenen açıklama forma doğru yazılmadı."
        );


        /*
         * Düzenleme kaydedilir.
         */
        ReportManager.step(
                "Kaynak düzenleme işlemi kaydediliyor."
        );

        openDataPage
                .kaynakDuzenlemeyiKaydetme();


        /*
         * Yeni kaynak adının listede görüntülendiği doğrulanır.
         */
        ReportManager.step(
                "Güncellenen kaynağın listede bulunduğu doğrulanıyor: "
                        + yeniKaynakAdi
        );

        Assert.assertTrue(
                openDataPage
                        .kaynakListedeMi(
                                yeniKaynakAdi
                        ),
                "Güncellenen kaynak listede bulunamadı."
        );


        /*
         * Yeni ad ve açıklamanın kalıcı olarak
         * kaydedildiği doğrulanır.
         */
        ReportManager.step(
                "Güncellenen kaynak bilgilerinin kalıcı olarak kaydedildiği doğrulanıyor."
        );

        Assert.assertTrue(
                openDataPage
                        .kaynakBilgileriDogruMu(
                                yeniKaynakAdi,
                                yeniKaynakAciklamasi
                        ),
                "Güncellenen kaynak bilgileri listede beklenen değerlerle uyuşmuyor."
        );


        /*
         * Eski kaynak adının artık listede
         * bulunmaması da doğrulanır.
         */
        ReportManager.step(
                "Eski kaynak adının artık listede bulunmadığı doğrulanıyor."
        );

        Assert.assertFalse(
                openDataPage
                        .kaynakListedeMi(
                                kaynakAdi
                        ),
                "Kaynak güncellenmesine rağmen eski kaynak adı hâlâ listede bulunuyor."
        );


        ReportManager.info(
                "KAYNAK DÜZENLEME BAŞARILI"
                        + " | Eski: "
                        + kaynakAdi
                        + " | Yeni: "
                        + yeniKaynakAdi
        );
    }
    /**
     * Kaynak Ara alanının mevcut ve mevcut olmayan
     * kaynak kriterlerinde doğru çalıştığını doğrular.
     *
     * Her iki arama sonucunda ekran görüntüsü alınır.
     */
    @Test(priority = 5)
    public void kaynakArama() {

        OpenDataPage openDataPage =
                new OpenDataPage(page);


        String mevcutAramaKriteri =
                "otomasyon";

        String bulunmayanAramaKriteri =
                "bu kayıt yok";


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
         * Mevcut kayıt kriteri ile arama yapılır.
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
         * Pozitif arama sonucu ekran görüntüsü alınır.
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
         * Görüntülenen kaynak adlarının arama
         * kriteriyle uyumlu olduğu doğrulanır.
         */
        ReportManager.step(
                "Arama sonucunda listelenen kaynak adlarının '"
                        + mevcutAramaKriteri
                        + "' kriteriyle uyumlu olduğu doğrulanıyor."
        );

        Assert.assertTrue(
                openDataPage
                        .kaynakAramaSonuclariUygunMu(
                                mevcutAramaKriteri
                        ),
                "Kaynak arama sonuçları arama kriteriyle uyumlu değil."
        );


        /*
         * Sistemde bulunmayan bir kaynak adıyla
         * ikinci arama gerçekleştirilir.
         */
        ReportManager.step(
                "Kaynak Ara alanında bulunmayan kayıt aranıyor: "
                        + bulunmayanAramaKriteri
        );

        openDataPage
                .kaynakArama(
                        bulunmayanAramaKriteri
                );


        /*
         * Negatif arama sonucu ekran görüntüsü alınır.
         */
        ReportManager.step(
                "Sonuç bulunamayan kaynak araması için ekran görüntüsü alınıyor."
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


        /*
         * Kaynak bulunamadığında gösterilen
         * boş sonuç mesajı doğrulanır.
         */
        ReportManager.step(
                "Kaynak bulunamadığında boş liste mesajının görüntülendiği doğrulanıyor."
        );

        Assert.assertTrue(
                openDataPage
                        .kaynakBulunamadiMesajiGoruntulendiMi(),
                "Kaynak bulunamadı mesajı görüntülenemedi."
        );


        ReportManager.info(
                "KAYNAK ARAMA KONTROLÜ BAŞARILI"
                        + " | Pozitif kriter: "
                        + mevcutAramaKriteri
                        + " | Negatif kriter: "
                        + bulunmayanAramaKriteri
        );
    }

}