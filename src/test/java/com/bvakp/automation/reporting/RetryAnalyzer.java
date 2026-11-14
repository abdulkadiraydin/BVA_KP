package com.bvakp.automation.reporting;

import com.bvakp.automation.core.config.ConfigManager;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Kırılan testi test.retry.count kadar tekrar dener. Varsayılan 0'dır: localde tekrar denenmez,
 * Jenkins'te BVAKP_TEST_RETRY_COUNT ile açılır.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

  private int yapilanTekrar = 0;

  @Override
  public boolean retry(ITestResult sonuc) {
    int azami = azamiTekrar();
    if (yapilanTekrar >= azami) {
      return false;
    }
    yapilanTekrar++;
    ReportManager.warn(
        "TEST TEKRAR DENENECEK | "
            + TestListener.getTestName(sonuc)
            + " | Deneme: "
            + yapilanTekrar
            + "/"
            + azami);
    return true;
  }

  private static int azamiTekrar() {
    return Integer.parseInt(ConfigManager.getOrDefault("test.retry.count", "0"));
  }
}
