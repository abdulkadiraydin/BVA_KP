package com.bvakp.automation.mail;

/**
 * Son test koşusunun rapor mailini gönderen giriş noktası.
 *
 * <p>Testlerden sonra ayrı bir adım olarak çalıştırılır (test JVM'i kapanıp
 * arşiv oluştuktan sonra). Mail gönderilemezse 1 çıkış koduyla biter;
 * böylece CI adımı başarısız görünür.
 */
public final class MailRunner {

    private MailRunner() {
    }

    public static void main(String[] args) {

        try {
            TestReportMailService.sonRaporuMailGonder();
        } catch (RuntimeException e) {
            System.err.println("Test raporu maili gönderilemedi: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}