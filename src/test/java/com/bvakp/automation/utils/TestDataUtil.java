package com.bvakp.automation.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TestDataUtil {

    private TestDataUtil() {
    }

    /**
     * Testlerde tekrar etmeyen dinamik bir ad oluşturmak için kullanılır.
     * Verilen ön ekin sonuna tarih ve saat bilgisi ekler.
     *
     * Örnek:
     * otomasyon_deneme_20260924_152315_125
     *
     * @param onEk oluşturulacak adın başlangıç değeri
     * @return tarih ve saat eklenmiş dinamik ad
     */
    public static String dinamikAdOlusturma(String onEk) {

        String tarihSaat = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));

        return onEk + "_" + tarihSaat;
    }
}