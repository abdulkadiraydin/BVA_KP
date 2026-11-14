package com.bvakp.automation.reporting;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

/**
 * TestNG testleri başlatmadan önce her @Test'i buradan geçirir; hepsine RetryAnalyzer atanır.
 * Böylece her teste ayrı ayrı retryAnalyzer yazmak gerekmez.
 */
public class RetryTransformer implements IAnnotationTransformer {

  @Override
  public void transform(
      ITestAnnotation test, Class testClass, Constructor testConstructor, Method testMethod) {
    test.setRetryAnalyzer(RetryAnalyzer.class);
  }
}
