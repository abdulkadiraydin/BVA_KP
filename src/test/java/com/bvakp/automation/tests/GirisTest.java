package com.bvakp.automation.tests;

import com.bvakp.automation.base.BaseTest;
import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.pages.DataSetCreationPage;
import com.bvakp.automation.pages.LoginPage;
import com.bvakp.automation.reporting.ReportManager;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.bvakp.automation.pages.OpenDataPage;

public class GirisTest extends BaseTest {

   /* @Test
    public void girisSayfasiAcilsin() throws InterruptedException {

        ReportManager.step("Ana sayfa açılıyor.");

        LoginPage loginPage = new LoginPage(page);
        loginPage.open();

        ReportManager.step("Ana sayfa açıldı.");

        Thread.sleep(10_000);
    }*/


    @Test
    public void girisSayfasiAcilsin() throws InterruptedException {

        ReportManager.step("Ana sayfa açılıyor.");

        LoginPage loginPage = new LoginPage(page);
        loginPage.open();

        ReportManager.step("Login sayfası kontrol ediliyor.");
        Assert.assertTrue(loginPage.isLoginPageDisplayed(), "Login sayfası görüntülenemedi.");
    }

    @Test
    public void basariliGiris() throws InterruptedException {

        LoginPage loginPage = new LoginPage(page);
        DataSetCreationPage dataSetCreationPage = new DataSetCreationPage(page);




        String username = ConfigManager.get("username");
        String password = ConfigManager.get("password");

        loginPage
                .open()
                .enterUsername(username)
                .enterPassword(password)
                .clickLogin();

        Assert.assertEquals(
                loginPage.getTitle(),
                "Büyük Veri Analitiği Kaynak Planlama"
        );

        OpenDataPage acikVeriPage = new OpenDataPage(page);

        acikVeriPage.clickAcikVeriPortali();
        acikVeriPage.clickKaynakEkle();
        acikVeriPage.enterAd("Test Veri Seti");
        acikVeriPage.clickTablo();
        acikVeriPage.selectFirstTable();
        acikVeriPage.enterAciklama("Test veri seti açıklaması");
        acikVeriPage.clickKaynakEkleSubmit();
        acikVeriPage.selectDataSet("Test Veri Seti");
        acikVeriPage.clickIleri();
        dataSetCreationPage.enterDataSetName("Test Veri Seti");
        dataSetCreationPage.selectSorumluBirim(
                "İşletme Dairesi Başkanlığı"
        );
        dataSetCreationPage.selectKategori("Altyapı & Şebeke");
        dataSetCreationPage.selectLisans(
                "TCDD Taşımacılık Açık Veri Lisansı"
        );
        dataSetCreationPage.enterAciklama("Test veri seti açıklaması");
        dataSetCreationPage.selectGuncellemeTipi("Periyodik");
        dataSetCreationPage.enterTakvimBaslangici("25.09.2026");
        dataSetCreationPage.enterSiklik("1");
        dataSetCreationPage.selectAralik("Hafta");
        dataSetCreationPage.clickOnizle();
        dataSetCreationPage.clickOnayaGonder();
        dataSetCreationPage.confirmOnayaGonder();
        acikVeriPage.clickAcikVeriPortali();
        acikVeriPage.clickKaynakEkle();
    }

    @Test
    public void hataliGiris() {

        LoginPage loginPage = new LoginPage(page);

        loginPage
                .open()
                .enterUsername("yanlisKullanici")
                .enterPassword("yanlisSifre")
                .clickLogin();

        Assert.assertEquals(
                loginPage.getLoginErrorMessage(),
                "Invalid username or password."
        );
    }
}