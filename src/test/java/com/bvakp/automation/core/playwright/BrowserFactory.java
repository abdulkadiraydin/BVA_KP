package com.bvakp.automation.core.playwright;

import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Ayarlardaki tarayıcı adına göre Playwright tarayıcısını başlatır.
 *
 * <p>CHROMIUM, FIREFOX ve WEBKIT Playwright'ın indirdiği tarayıcılardır. CHROME ve EDGE ise
 * bilgisayarda kurulu Google Chrome ve Microsoft Edge'i kullanır (channel).
 */
public class BrowserFactory {

  /** Tarayıcı bilgisi koşu başında bir kez yazılır; her testte tekrar edilmez. */
  private static final AtomicBoolean BILGI_YAZILDI = new AtomicBoolean(false);

  private BrowserFactory() {}

  /** Verilen tarayıcıyı headless ayarıyla başlatır. */
  public static Browser createBrowser(
      Playwright playwright, BrowserName browserName, boolean headless) {

    BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(headless);

    Browser browser =
        switch (browserName) {
          case CHROMIUM -> playwright.chromium().launch(launchOptions);

          case CHROME -> playwright.chromium().launch(launchOptions.setChannel("chrome"));

          case EDGE -> playwright.chromium().launch(launchOptions.setChannel("msedge"));

          case FIREFOX -> playwright.firefox().launch(launchOptions);

          case WEBKIT -> playwright.webkit().launch(launchOptions);
        };

    if (BILGI_YAZILDI.compareAndSet(false, true)) {
      tarayiciBilgisiniYazma(browserName, browser.version(), headless);
    }
    return browser;
  }

  /** Tarayıcı bilgisini loga ve Allure raporunun Environment bölümüne yazar. */
  private static void tarayiciBilgisiniYazma(
      BrowserName browserName, String surum, boolean headless) {

    ReportManager.info(
        "Tarayıcı: " + browserName + " | Sürüm: " + surum + " | Headless: " + headless);

    Path dosya =
        Paths.get(System.getProperty("allure.results.directory", "target/allure-results"))
            .resolve("environment.properties");

    String icerik =
        "Tarayici="
            + browserName
            + System.lineSeparator()
            + "Tarayici.Surumu="
            + surum
            + System.lineSeparator()
            + "Headless="
            + headless
            + System.lineSeparator()
            + "Ortam="
            + ConfigManager.ortam()
            + System.lineSeparator()
            + "Kapali.Portal="
            + ConfigManager.getOrDefault("base.url", "-")
            + System.lineSeparator();

    try {
      Files.createDirectories(dosya.getParent());
      Files.writeString(dosya, icerik, StandardCharsets.UTF_8);
    } catch (IOException e) {
      // Rapor bilgisi yazılamaması testi etkilememeli.
      ReportManager.warn("Allure environment bilgisi yazılamadı: " + e.getMessage());
    }
  }
}
