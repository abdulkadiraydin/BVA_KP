package com.bvakp.automation.api.publicportal;

import java.util.List;
import java.util.Map;

/**
 * datastore_search sonucunun tipli hali.
 *
 * @param toplam    kaynaktaki toplam kayıt sayısı (limit'ten bağımsız)
 * @param alanlar   alan adı → tip ("text", "int4"...), sistem alanı "_id" hariç, sırayla
 * @param kayitlar  dönen kayıtlar; değerler metne çevrilmiş, "_id" hariç
 */
public record DatastoreSonuc(
        int toplam,
        Map<String, String> alanlar,
        List<Map<String, String>> kayitlar) {

    public DatastoreSonuc {
        alanlar = java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(alanlar));
        kayitlar = List.copyOf(kayitlar);
    }

    public List<String> alanAdlari() {
        return List.copyOf(alanlar.keySet());
    }

    /** Verilen alanın bütün kayıtlardaki değerleri. */
    public List<String> degerler(String alan) {
        return kayitlar.stream().map(kayit -> kayit.get(alan)).toList();
    }
}