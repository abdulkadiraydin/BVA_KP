package com.bvakp.automation.model;

import java.util.List;
import java.util.Map;

/**
 * Kapalı portalda oluşturulan veri setinin, oluşturma ve yayınlama sırasında
 * ekranlardan toplanan değerleri.
 *
 * <p>Kamuya açık portal ve API testlerinde "beklenen değer" olarak kullanılır:
 * <pre>
 * Kaynak / katalog alanları     → testin girdiği değerler
 * kolonlar                       → Alan Seçimi ve Anonimleştirme ekranı
 * onizlemeKayitlari              → Önizleme ve KVKK Kontrolü ekranı (ilk 10 kayıt)
 * yayin                          → Veri Setini Yayınla penceresi
 * </pre>
 */
public record VeriSetiTanimi(
        String ad,
        String aciklama,
        String kaynakAdi,
        String sorumluBirim,
        String kategori,
        String kullanimLisansi,
        List<KolonTanimi> kolonlar,
        List<Map<String, String>> onizlemeKayitlari,
        YayinAyarlari yayin) {

    public VeriSetiTanimi {
        kolonlar = List.copyOf(kolonlar);
        onizlemeKayitlari = List.copyOf(onizlemeKayitlari);
    }

    /** Yayına dahil edilen kolonların adları, ekrandaki sırayla. */
    public List<String> kolonAdlari() {
        return kolonlar.stream().filter(KolonTanimi::dahil).map(KolonTanimi::ad).toList();
    }

    /** Maskele kuralı uygulanan kolonların adları. */
    public List<String> maskelenenKolonlar() {
        return kolonlar.stream().filter(KolonTanimi::maskeliMi).map(KolonTanimi::ad).toList();
    }

    /** Yayın penceresinde okunan ayarlarla yeni bir tanım döndürür. */
    public VeriSetiTanimi yayinAyarlariIle(YayinAyarlari yayinAyarlari) {
        return new VeriSetiTanimi(ad, aciklama, kaynakAdi, sorumluBirim, kategori,
                kullanimLisansi, kolonlar, onizlemeKayitlari, yayinAyarlari);
    }
}