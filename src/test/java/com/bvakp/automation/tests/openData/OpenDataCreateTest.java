package com.bvakp.automation.tests.openData;

import com.bvakp.automation.base.BaseTest;
import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.pages.LoginPage;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.pages.openData.OpenDataPublishPage;
import com.bvakp.automation.companents.KaynakEkleModal;
import com.bvakp.automation.reporting.ReportManager;
import com.bvakp.automation.utils.TestDataUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OpenDataCreateTest extends BaseTest {

    @Test
    public void kaynakOlusturmaSilme() {
        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma(
                        "otomasyon_kaynak"
                );

        String aciklama =
                "Otomasyon kaynak açıklaması - "
                        + kaynakAdi;

        // Login
        LoginPage loginPage = new LoginPage(page);

        loginPage
                .open()
                .enterUsername(ConfigManager.get("username"))
                .enterPassword(ConfigManager.get("password"))
                .clickLogin();

        Assert.assertEquals(
                loginPage.getTitle(),
                "Büyük Veri Analitiği Kaynak Planlama"
        );


        // Açık Veri Portalı
        OpenDataPage openDataPage = new OpenDataPage(page);

        openDataPage.clickAcikVeriPortali();

        KaynakEkleModal kaynakEkleModal =
                new KaynakEkleModal(page);

        // Kaynak oluştur
        openDataPage.clickKaynakEkle();
        kaynakEkleModal
                .adGirme(
                        kaynakAdi
                );

        /*
         * Tablo listesindeki ilk kayıt seçilir.
         */
        ReportManager.step(
                "Tablo listesindeki ilk tablo seçiliyor."
        );

        kaynakEkleModal
                .ilkTabloyuSecme();

        /*
         * Dinamik açıklama girilir.
         */
        ReportManager.step(
                "Dinamik kaynak açıklaması giriliyor."
        );

        kaynakEkleModal
                .aciklamaGirme(
                        aciklama
                );

        /*
         * Tablo seçimi sonrasında Veri Önizleme
         * alanının oluştuğu doğrulanır.
         */
        ReportManager.step(
                "Veri Önizleme alanının görüntülendiği doğrulanıyor."
        );

        Assert.assertTrue(
                kaynakEkleModal
                        .veriOnizlemeGoruntulendiMi(),
                "Veri Önizleme alanı görüntülenemedi."
        );

        /*
         * Önizleme alanında tam 10 veri satırı
         * görüntülenmesi beklenir.
         */
        ReportManager.step(
                "Veri Önizleme alanındaki satır sayısı doğrulanıyor."
        );

        int satirSayisi =
                kaynakEkleModal
                        .veriOnizlemeSatirSayisiAlma();

        ReportManager.info(
                "Veri Önizleme satır sayısı: "
                        + satirSayisi
        );

        Assert.assertEquals(
                satirSayisi,
                10,
                "Veri Önizleme alanında 10 kayıt görüntülenmedi."
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
         * İlk aşamada modalın kapanması kontrol edilir.
         */
        ReportManager.step(
                "Kaynak oluşturma ekranının kapandığı doğrulanıyor."
        );

        Assert.assertTrue(
                kaynakEkleModal
                        .kaynakEkleModalKapandiMi(),
                "Kaynak Ekle ekranı işlem sonrasında kapanmadı."
        );

        ReportManager.info(
                "KAYNAK OLUŞTURMA İŞLEMİ TAMAMLANDI | "
                        + kaynakAdi
        );
        ReportManager.step(
                "Oluşturulan kaynağın listede görüntülendiği doğrulanıyor."
        );

        Assert.assertTrue(
                openDataPage
                        .kaynakListesindeGoruntulendiMi(
                                kaynakAdi
                        ),
                "Oluşturulan kaynak listede görüntülenemedi: "
                        + kaynakAdi
        );

        // Kaynağı sil
        openDataPage.deleteSource("Test Kaynak");

        Assert.assertTrue(
                openDataPage.isSourceDeleted("Test Kaynak"),
                "Kaynak silinemedi."
        );
    }

    @Test
    public void mevcutKaynaktanVeriSetiOlusturma() {

        // Login
        LoginPage loginPage = new LoginPage(page);

        loginPage
                .open()
                .enterUsername(ConfigManager.get("username"))
                .enterPassword(ConfigManager.get("password"))
                .clickLogin();

        Assert.assertEquals(
                loginPage.getTitle(),
                "Büyük Veri Analitiği Kaynak Planlama"
        );

        // Açık Veri Portalı
        OpenDataPage openDataPage = new OpenDataPage(page);

        openDataPage.clickAcikVeriPortali();

        // Mevcut kaynağı seç
        openDataPage.selectDataSet("Test Veri Seti");
        openDataPage.clickIleri();

        // Veri seti oluşturma
        openDataPage.enterDataSetName("Test Veri Seti");

        openDataPage.selectSorumluBirim(
                "İşletme Dairesi Başkanlığı"
        );

        openDataPage.selectKategori(
                "Altyapı & Şebeke"
        );

        openDataPage.selectLisans(
                "TCDD Taşımacılık Açık Veri Lisansı"
        );
        openDataPage.enterDataSetAciklama(
                "Test veri seti açıklaması"
        );

        openDataPage.selectGuncellemeTipi(
                "Periyodik"
        );

        openDataPage.enterTakvimBaslangici(
                "25.09.2026"
        );

        openDataPage.enterSiklik("1");

        openDataPage.selectAralik("Hafta");

        // Önizleme ve onaya gönderme
        openDataPage.clickOnizle();
        openDataPage.clickOnayaGonder();
        openDataPage.confirmOnayaGonder();

        // Veri Seti Yönetimi
        OpenDataPublishPage publishPage =
                new OpenDataPublishPage(page);

        publishPage.clickPublishTab();

        Assert.assertTrue(
                publishPage.isStatus(
                        "Test Veri Seti",
                        "KVKK Onayında"
                ),
                "Veri seti oluşturulamadı veya KVKK Onayında durumuna geçmedi."
        );
    }
}