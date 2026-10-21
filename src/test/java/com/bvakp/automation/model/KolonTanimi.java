package com.bvakp.automation.model;

/**
 * "Alan Seçimi ve Anonimleştirme" ekranındaki tek bir kolonun tanımı.
 *
 * @param ad          kolon adı (ör. "yil")
 * @param veriTipi    kaynaktaki veri tipi (ör. "integer", "varchar(10)", "date")
 * @param kuralKodu   anonimleştirme kuralının kodu (ör. "NONE", "MASK")
 * @param kuralEtiketi ekranda görünen kural adı (ör. "Kural Yok", "Maskele")
 * @param dahil       kolon yayına dahil mi (satırdaki onay kutusu)
 */
public record KolonTanimi(
        String ad,
        String veriTipi,
        String kuralKodu,
        String kuralEtiketi,
        boolean dahil) {

    public static final String KURAL_YOK = "NONE";
    public static final String KURAL_MASKELE = "MASK";

    public boolean maskeliMi() {
        return KURAL_MASKELE.equals(kuralKodu);
    }
}