package com.bvakp.automation.core.auth;

import com.microsoft.playwright.BrowserContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class AvpAuthStateManager {

    private static final Path AUTH_STATE_PATH =
            Paths.get("target", "auth", "avp-auth.json");

    private AvpAuthStateManager() {
    }

    /**
     * AVP giriş oturumunun kaydedileceği storage state
     * dosyasının yolunu döndürmek için kullanılır.
     *
     * @return AVP authentication storage state dosya yolu
     */
    public static Path authStatePathAlma() {
        return AUTH_STATE_PATH;
    }

    /**
     * Başarılı AVP girişinden sonra mevcut browser oturumunu
     * storage state dosyasına kaydetmek için kullanılır.
     *
     * Bu dosya cookie ve oturum bilgilerini saklayarak
     * sonraki testlerin tekrar login olmadan başlamasını sağlar.
     *
     * @param context aktif Playwright BrowserContext nesnesi
     */
    public static void authStateKaydetme(BrowserContext context) {

        try {

            Files.createDirectories(AUTH_STATE_PATH.getParent());

            context.storageState(
                    new BrowserContext.StorageStateOptions()
                            .setPath(AUTH_STATE_PATH)
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "AVP authentication storage state dosyası oluşturulamadı.",
                    e
            );
        }
    }


    /**
     * Daha önce kaydedilmiş AVP oturum dosyasının
     * mevcut olup olmadığını kontrol etmek için kullanılır.
     *
     * @return auth state dosyası varsa true, yoksa false
     */
    public static boolean authStateVarMi() {
        return Files.exists(AUTH_STATE_PATH);
    }
}