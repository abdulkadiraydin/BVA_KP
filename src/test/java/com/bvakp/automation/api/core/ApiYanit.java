package com.bvakp.automation.api.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Bir API çağrısının sonucu. Gövde istek bittiğinde okunur; Playwright yanıtı
 * kapatıldıktan sonra da güvenle kullanılabilir.
 *
 * @param metot      HTTP metodu (GET, POST...)
 * @param url        sorgu parametreleriyle birlikte çağrılan adres
 * @param durumKodu  HTTP durum kodu
 * @param basliklar  yanıt başlıkları
 * @param govde      yanıt gövdesi (metin)
 */
public record ApiYanit(
        String metot,
        String url,
        int durumKodu,
        Map<String, String> basliklar,
        String govde) {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public ApiYanit {
        basliklar = Map.copyOf(basliklar);
    }

    /** 2xx durum kodu. */
    public boolean basariliMi() {
        return durumKodu >= 200 && durumKodu < 300;
    }

    /** Gövdeyi JSON olarak döndürür; JSON değilse anlamlı bir hata verir. */
    public JsonNode json() {
        try {
            return MAPPER.readTree(govde);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Yanıt JSON değil | " + metot + " " + url
                    + " | Durum: " + durumKodu + " | Gövde: " + kisaGovde(), e);
        }
    }

    /**
     * JSON Pointer ile bir alanı okur. Örnek: {@code alan("/result/total")}.
     * Alan yoksa "missing" düğüm döner ({@link JsonNode#isMissingNode()}).
     */
    public JsonNode alan(String jsonPointer) {
        return json().at(jsonPointer);
    }

    /** Hata mesajları ve loglar için gövdenin ilk 500 karakteri. */
    public String kisaGovde() {
        return govde == null || govde.length() <= 500 ? govde : govde.substring(0, 500) + "…";
    }
}