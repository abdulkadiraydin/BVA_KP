package com.bvakp.automation.model;

import java.util.List;

/**
 * "Veri Setini Yayınla" penceresinde, yayınla butonuna basılmadan önce okunan ayarlar.
 *
 * @param formatlar       işaretli indirilebilir formatlar (ör. CSV, TSV, JSON, XML, XLSX)
 * @param dataApiAcik     "Data API (sorgu API'si)" işaretli mi
 * @param zamanlama       "now" (hemen) veya "scheduled" (belirtilen tarihte)
 * @param kvkkNotu        portalda açıklamanın altında görünecek KVKK notu
 * @param yayinAciklamasi portalın değişiklik geçmişinde görünecek açıklama
 */
public record YayinAyarlari(
        List<String> formatlar,
        boolean dataApiAcik,
        String zamanlama,
        String kvkkNotu,
        String yayinAciklamasi) {

    public YayinAyarlari {
        formatlar = List.copyOf(formatlar);
    }
}