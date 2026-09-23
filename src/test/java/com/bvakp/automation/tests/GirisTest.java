package com.bvakp.automation.tests;

import com.bvakp.automation.base.BaseTest;
import com.bvakp.automation.pages.LoginPage;
import com.bvakp.automation.reporting.ReportManager;
import org.testng.Assert;
import org.testng.annotations.Test;

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

        ReportManager.step("Ana sayfa açıldı.");

        Thread.sleep(3000);

        Assert.fail("Screenshot test etmek için bilinçli hata.");
    }
}