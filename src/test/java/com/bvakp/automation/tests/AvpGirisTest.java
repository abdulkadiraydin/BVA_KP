package com.bvakp.automation.tests;

import com.bvakp.automation.base.BaseTest;
import com.bvakp.automation.pages.DashboardPage;
import com.bvakp.automation.pages.LoginPage;
import com.bvakp.automation.reporting.ReportManager;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.bvakp.automation.pages.AcikVeriPage;

public class AvpGirisTest extends BaseTest {

    /**
     * Login sayfasının açılmasını, kullanıcı adı ve şifre bilgilerinin
     * ilgili alanlara girilebildiğini ve giriş butonuna tıklanabildiğini
     * doğrulamak için yazılmıştır.
     */
    @Test
    public void girisSayfasiAcma() {

        LoginPage loginPage = new LoginPage(page);

        ReportManager.step("Giriş sayfası açılıyor.");
        loginPage.open();

        ReportManager.step("Giriş ekranının açıldığı doğrulanıyor.");
        Assert.assertTrue(
                loginPage.isLoginPageVisible(),
                "Giriş ekranı görüntülenemedi."
        );

        String kullaniciAdi = System.getenv("BVA_USERNAME");

        Assert.assertNotNull(
                kullaniciAdi,
                "BVA_USERNAME environment variable tanımlı değil."
        );

        ReportManager.step("Kullanıcı adı alanına kullanıcı adı giriliyor.");
        loginPage.kullaniciAdiGirme(kullaniciAdi);

        String sifre = System.getenv("BVA_PASSWORD");

        Assert.assertNotNull(
                sifre,
                "BVA_PASSWORD environment variable tanımlı değil."
        );

        ReportManager.step("Şifre alanına şifre giriliyor.");
        loginPage.sifreGirme(sifre);

        ReportManager.step("Sign In butonuna tıklanıyor.");
        loginPage.girisButonunaTiklama();

        ReportManager.info(
                "Giriş sonrası açılan URL: " + loginPage.getCurrentUrl()
        );

        DashboardPage dashboardPage = new DashboardPage(page);

        ReportManager.step("Başarılı giriş sonrası Gösterge Panosu ekranının açıldığı doğrulanıyor.");

        Assert.assertTrue(
                dashboardPage.gostergePanosuGoruntulenmeKontrolu(),
                "Giriş sonrası Gösterge Panosu ekranı görüntülenemedi.");


        ReportManager.step("Açık Veri Portalı menüsüne tıklanıyor.");
        dashboardPage.acikVeriPortalinaGitme();

        AcikVeriPage acikVeriPage = new AcikVeriPage(page);

        ReportManager.step("Açık Veri Portalı ekranının açıldığı doğrulanıyor.");

        Assert.assertTrue(
                acikVeriPage.veriSetiOlusturmaGoruntulenmeKontrolu(),
                "Açık Veri Portalı ekranında Veri Seti Oluşturma bölümü görüntülenemedi."
        );

    }

}