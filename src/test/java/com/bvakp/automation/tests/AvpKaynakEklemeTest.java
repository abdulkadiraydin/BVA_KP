package com.bvakp.automation.tests;

import com.bvakp.automation.base.AvpBaseTest;
import com.bvakp.automation.pages.AcikVeriPage;
import com.bvakp.automation.pages.DashboardPage;
import com.bvakp.automation.reporting.ReportManager;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.bvakp.automation.components.KaynakEkleModal;
import com.bvakp.automation.utils.TestDataUtil;

public class AvpKaynakEklemeTest extends AvpBaseTest {

    /**
     * Başarılı giriş sonrasında Açık Veri Portalı ekranına
     * geçiş yapılabildiğini doğrulamak için yazılmıştır.
     */
    @Test
    public void acikVeriPortalinaGiris() {

        DashboardPage dashboardPage = avpGirisYapma();

        ReportManager.step("Açık Veri Portalı menüsüne tıklanıyor.");
        dashboardPage.acikVeriPortalinaGitme();

        AcikVeriPage acikVeriPage = new AcikVeriPage(page);

        ReportManager.step("Açık Veri Portalı ekranının açıldığı doğrulanıyor.");

        Assert.assertTrue(
                acikVeriPage.veriSetiOlusturmaGoruntulenmeKontrolu(),
                "Açık Veri Portalı ekranında Veri Seti Oluşturma bölümü görüntülenemedi."
        );
    }

    /**
     * Açık Veri Portalı ekranında Kaynak Ekle butonuna
     * tıklanabildiğini ve kaynak ekleme ekranının açılma işleminin
     * başlatılabildiğini doğrulamak için yazılmıştır.
     */
    @Test
    public void kaynakEklemeEkraniAcma() {

        DashboardPage dashboardPage = avpGirisYapma();

        ReportManager.step("Açık Veri Portalı menüsüne tıklanıyor.");
        dashboardPage.acikVeriPortalinaGitme();

        AcikVeriPage acikVeriPage = new AcikVeriPage(page);

        ReportManager.step("Kaynak Ekle butonuna tıklanıyor.");
        acikVeriPage.kaynakEklemeEkraniniAcma();

        KaynakEkleModal kaynakEkleModal = new KaynakEkleModal(page);


        ReportManager.step("Kaynak Ekle ekranının açıldığı doğrulanıyor.");

        Assert.assertTrue(
                kaynakEkleModal.kaynakEkleModalGoruntulenmeKontrolu(),
                "Kaynak Ekle ekranı görüntülenemedi."
        );

    }

    /**
     * Kaynak Ekle ekranındaki Ad alanına
     * dinamik olarak oluşturulan kaynak adının girilebildiğini
     * ve doğru şekilde yazıldığını doğrulamak için kullanılır.
     */
    @Test
    public void kaynakAdiGirme() {

        DashboardPage dashboardPage = avpGirisYapma();

        ReportManager.step("Açık Veri Portalı menüsüne tıklanıyor.");
        dashboardPage.acikVeriPortalinaGitme();

        AcikVeriPage acikVeriPage = new AcikVeriPage(page);

        ReportManager.step("Kaynak Ekle butonuna tıklanıyor.");
        acikVeriPage.kaynakEklemeEkraniniAcma();

        KaynakEkleModal kaynakEkleModal = new KaynakEkleModal(page);

        ReportManager.step("Kaynak Ekle ekranının açıldığı doğrulanıyor.");

        Assert.assertTrue(
                kaynakEkleModal.kaynakEkleModalGoruntulenmeKontrolu(),
                "Kaynak Ekle ekranı görüntülenemedi."
        );

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_deneme");

        ReportManager.step(
                "Dinamik kaynak adı oluşturuldu: " + kaynakAdi
        );

        ReportManager.step("Kaynak adı Ad alanına giriliyor.");
        kaynakEkleModal.adGirme(kaynakAdi);

        ReportManager.step("Ad alanına girilen değerin doğru olduğu doğrulanıyor.");

        Assert.assertEquals(
                kaynakEkleModal.adDegeriAlma(),
                kaynakAdi,
                "Ad alanına girilen kaynak adı beklenen değerle eşleşmiyor."
        );

        String tabloAdi = "arac_bakim_is_yeri_sayisi";

        ReportManager.step("Tablo alanından seçim yapılıyor: " + tabloAdi);

        kaynakEkleModal.tabloSecme(tabloAdi);

        ReportManager.step("Seçilen tablonun doğru olduğu doğrulanıyor.");

        Assert.assertEquals(
                kaynakEkleModal.tabloDegeriAlma(),
                tabloAdi,
                "Seçilen tablo beklenen tablo ile eşleşmiyor."
        );

        String aciklama =
                "Otomasyon testi için oluşturulan kaynak: " + kaynakAdi;

        ReportManager.step("Açıklama alanına açıklama giriliyor.");

        kaynakEkleModal.aciklamaGirme(aciklama);

        ReportManager.step("Açıklama alanına girilen değerin doğru olduğu doğrulanıyor.");

        Assert.assertEquals(
                kaynakEkleModal.aciklamaDegeriAlma(),
                aciklama,
                "Açıklama alanına girilen değer beklenen açıklama ile eşleşmiyor."
        );

        ReportManager.step("Veri Önizleme alanının görüntülendiği doğrulanıyor.");

        Assert.assertTrue(
                kaynakEkleModal.veriOnizlemeGoruntulenmeKontrolu(),
                "Veri Önizleme alanı görüntülenemedi."
        );

        ReportManager.step("Veri Önizleme alanında 10 satır veri bulunduğu doğrulanıyor.");

        Assert.assertEquals(
                kaynakEkleModal.veriOnizlemeSatirSayisiAlma(),
                10,
                "Veri Önizleme alanındaki satır sayısı 10 değil."
        );
        ReportManager.step("Açıklama alanına girilen değerin doğru olduğu doğrulanıyor.");

        Assert.assertEquals(
                kaynakEkleModal.aciklamaDegeriAlma(),
                aciklama,
                "Açıklama alanına girilen değer beklenen açıklama ile eşleşmiyor."
        );



        ReportManager.step("Kaynak bilgileri tamamlandı. Kaynak Ekle butonuna tıklanıyor.");

        kaynakEkleModal.kaynakEkleme();

        ReportManager.step(
                "Oluşturulan kaynağın listede görüntülendiği doğrulanıyor: " + kaynakAdi
        );

        Assert.assertTrue(
                acikVeriPage.kaynakListesindeGoruntulenmeKontrolu(kaynakAdi),
                "Oluşturulan kaynak listede görüntülenemedi: " + kaynakAdi
        );
    }



}