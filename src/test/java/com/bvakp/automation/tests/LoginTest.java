package com.bvakp.automation.tests;

import com.bvakp.automation.base.BaseTest;
import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.pages.LoginPage;
import com.bvakp.automation.reporting.ReportManager;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    @Test
    public void girisSayfasiAcilsin() {

        ReportManager.step("Ana sayfa açılıyor.");

        LoginPage loginPage = new LoginPage(page);
        loginPage.open();

        ReportManager.step("Login sayfası kontrol ediliyor.");


        Assert.assertTrue(
                loginPage.isLoginPageDisplayed(),
                "Login sayfası görüntülenemedi."
        );
    }

    @Test
    public void basariliGiris() {

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