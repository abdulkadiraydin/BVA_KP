package com.bvakp.automation.utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.nio.file.Path;

public final class HighlightScreenshotUtil {

    private HighlightScreenshotUtil() {
    }


    /**
     * Belirtilen alanı kırmızı çerçeve ile işaretleyerek
     * ekran görüntüsü almak için kullanılır.
     */
    public static void cerceveliEkranGoruntusuAlma(
            Page page,
            Locator locator,
            Path dosyaYolu) {

        locator.scrollIntoViewIfNeeded();

        locator.evaluate(
                "element => {" +
                        "element.dataset.previousOutline = element.style.outline;" +
                        "element.dataset.previousOutlineOffset = element.style.outlineOffset;" +
                        "element.style.outline = '4px solid red';" +
                        "element.style.outlineOffset = '3px';" +
                        "}"
        );

        try {

            page.screenshot(
                    new Page.ScreenshotOptions()
                            .setPath(dosyaYolu)
                            .setFullPage(false)
            );

        } finally {

            locator.evaluate(
                    "element => {" +
                            "element.style.outline = element.dataset.previousOutline || '';" +
                            "element.style.outlineOffset = element.dataset.previousOutlineOffset || '';" +
                            "delete element.dataset.previousOutline;" +
                            "delete element.dataset.previousOutlineOffset;" +
                            "}"
            );
        }
    }
}