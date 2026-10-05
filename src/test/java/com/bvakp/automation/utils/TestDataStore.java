package com.bvakp.automation.utils;

public final class TestDataStore {

    private static String kaynakAdi;

    private TestDataStore() {
    }

    /**
     * Otomasyon koşusunda kullanılacak
     * güncel kaynak adını saklar.
     */
    public static void kaynakAdiKaydet(
            String yeniKaynakAdi) {

        if (yeniKaynakAdi == null
                || yeniKaynakAdi.isBlank()) {

            throw new IllegalArgumentException(
                    "Kaydedilecek kaynak adı boş olamaz."
            );
        }

        kaynakAdi =
                yeniKaynakAdi;
    }

    /**
     * Güncel otomasyon kaynak adını döndürür.
     */
    public static String kaynakAdiAl() {

        if (!kaynakAdiVarMi()) {

            throw new IllegalStateException(
                    "Otomasyon kaynak adı bulunamadı. "
                            + "AvpKaynakEklemeTest aynı test suite içerisinde "
                            + "AcikVeriKaynakIslemleriTest'ten önce çalıştırılmalıdır."
            );
        }

        return kaynakAdi;
    }

    /**
     * Store içerisinde kullanılabilir
     * kaynak adı olup olmadığını kontrol eder.
     */
    public static boolean kaynakAdiVarMi() {

        return kaynakAdi != null
                && !kaynakAdi.isBlank();
    }

    /**
     * Test akışı tamamlandıktan sonra
     * saklanan kaynak adını temizler.
     */
    public static void kaynakAdiTemizle() {

        kaynakAdi = null;
    }
}