package com.bvakp.automation.utils;

import com.bvakp.automation.reporting.AllureArtifactCollector;
import com.bvakp.automation.reporting.ReportManager;
import com.bvakp.automation.reporting.TestListener;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.testng.ITestResult;
import org.testng.Reporter;

/**
 * Ekran görüntüsü alır ve target/screenshots altına kaydeder. Başarısız testlerde tam sayfa,
 * başarılı testlerde yalnızca görünen alan alınır. Hata durumunda exception fırlatmaz, null döner;
 * ekran görüntüsü alınamaması test sonucunu değiştirmemelidir.
 */
public final class ScreenshotUtil {

  private static final Path SCREENSHOT_DIRECTORY = Paths.get("target", "screenshots");

  private static final DateTimeFormatter ZAMAN_BICIMI =
      DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

  private ScreenshotUtil() {}

  /** Tam sayfa ekran görüntüsü alır. */
  public static Path takeScreenshot(Page page, String testName) {
    return takeScreenshot(page, testName, true);
  }

  /**
   * Ekran görüntüsü alır. tamSayfa false ise yalnızca görünen alan alınır; başarılı testlerde rapor
   * ve mail boyutunu küçültmek için kullanılır.
   *
   * <p>Dosya adı: testAdi_basarili_2026-10-22_19-41-05_ab12cd34.png
   */
  public static Path takeScreenshot(Page page, String testName, boolean tamSayfa) {

    if (page == null) {
      return null;
    }

    try {
      Files.createDirectories(SCREENSHOT_DIRECTORY);

      // UUID, aynı adlı paralel testlerin aynı saniyede birbirini ezmesini önler.
      String fileName =
          sanitizeFileName(testName)
              + "_"
              + LocalDateTime.now().format(ZAMAN_BICIMI)
              + "_"
              + UUID.randomUUID().toString().substring(0, 8)
              + ".png";

      Path screenshotPath = SCREENSHOT_DIRECTORY.resolve(fileName);

      if (tamSayfa) {
        sayfaGoruntusuAlma(page, screenshotPath);
      } else {
        page.screenshot(new Page.ScreenshotOptions().setPath(screenshotPath).setFullPage(false));
      }

      calisanTesteBaglama(screenshotPath, testName);

      return screenshotPath;

    } catch (Exception e) {
      ReportManager.error("Screenshot alınırken hata oluştu.", e);
      return null;
    }
  }

  /**
   * Tüm sayfanın görüntüsünü alır. Sayfa tarayıcının sınırından uzunsa (WebKit 32767 px) yalnızca
   * görünen alanın görüntüsünü alır.
   */
  private static void sayfaGoruntusuAlma(Page page, Path yol) {
    try {
      page.screenshot(new Page.ScreenshotOptions().setPath(yol).setFullPage(true));
    } catch (PlaywrightException e) {
      ReportManager.warn(
          "Tam sayfa screenshot alınamadı, görünen alan alınıyor | " + e.getMessage());
      page.screenshot(new Page.ScreenshotOptions().setPath(yol).setFullPage(false));
    }
  }

  /**
   * Görüntüyü o an çalışan teste bağlar; arşivde testin klasörüne kendi adıyla gider. Test dışında
   * (ör. @BeforeClass) çalışan test yoksa bağlanmaz.
   */
  private static void calisanTesteBaglama(Path screenshotPath, String ad) {
    ITestResult sonuc = Reporter.getCurrentTestResult();
    if (sonuc == null || !sonuc.getMethod().isTest()) {
      return;
    }
    AllureArtifactCollector.registerTestScreenshot(
        screenshotPath,
        sonuc.getTestClass().getRealClass().getSimpleName(),
        TestListener.getTestName(sonuc),
        sanitizeFileName(ad));
  }

  /** Dosya adında kullanılamayan karakterleri temizler. */
  private static String sanitizeFileName(String value) {
    if (value == null || value.isBlank()) {
      return "screenshot";
    }
    return value.replaceAll("[^a-zA-Z0-9._-]", "_");
  }
}
