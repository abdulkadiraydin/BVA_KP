package com.bvakp.automation.base;

import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.reporting.TestListener;
import com.microsoft.playwright.Page;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import com.bvakp.automation.reporting.AllureRunOrganizer;

@Listeners(TestListener.class)
public class BaseTest {

    protected Page page;

    static {
        AllureRunOrganizer.initialize();
    }

    @BeforeMethod
    public void setUp() {

        PlaywrightManager.initialize();

        page = PlaywrightManager.getPage();
    }

    @AfterMethod
    public void tearDown() {

        PlaywrightManager.close();
    }
}