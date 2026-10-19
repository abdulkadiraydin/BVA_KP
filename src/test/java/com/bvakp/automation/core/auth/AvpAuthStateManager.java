package com.bvakp.automation.core.auth;

import com.microsoft.playwright.BrowserContext;

import java.io.IOException;
import java.nio.file.*;

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

        Path gecici = null;

        try {
            Files.createDirectories(AUTH_STATE_PATH.getParent());

            // Aynı klasörde benzersiz geçici dosya: başka thread yarım dosyayı görmesin
            gecici = Files.createTempFile(
                    AUTH_STATE_PATH.getParent(), "avp-auth-", ".tmp"
            );

            context.storageState(
                    new BrowserContext.StorageStateOptions().setPath(gecici)
            );

            try {
                Files.move(
                        gecici,
                        AUTH_STATE_PATH,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (AtomicMoveNotSupportedException e) {
                // Dosya sistemi atomik taşımayı desteklemiyor: normal taşımaya düş
                Files.move(
                        gecici,
                        AUTH_STATE_PATH,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

        } catch (IOException e) {

            try {
                if (gecici != null) {
                    Files.deleteIfExists(gecici);
                }
            } catch (IOException silmeHatasi) {
                // Asıl hata aşağıda fırlatılır. Geçici dosya silme hatası
                // onu gölgelemesin diye ekleme bilgisi olarak saklanır.
                e.addSuppressed(silmeHatasi);
            }

            throw new RuntimeException(
                    "AVP authentication storage state dosyası oluşturulamadı.", e
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