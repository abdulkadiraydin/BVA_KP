package com.bvakp.automation.api.core;

import com.bvakp.automation.api.publicportal.PublicPortalApi;
import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import io.qameta.allure.Allure;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Genel amaçlı API istemcisi. Belirli bir servise bağlı değildir; servis sınıfları
 * (ör. {@link PublicPortalApi}) bunun üzerine kurulur.
 *
 * <p>Her çağrı loglanır ve aktif bir Allure testi varsa istek/yanıt rapora eklenir.
 * Playwright APIRequestContext kullanır; tarayıcı oturumunun çerezlerini paylaşmaz.
 */
public class ApiClient {

    private static final int RAPOR_GOVDE_SINIRI = 50_000;

    private final APIRequestContext context;
    private final String tabanUrl;

    public ApiClient(APIRequestContext context, String tabanUrl) {
        this.context = context;
        this.tabanUrl = tabanUrl.endsWith("/") ? tabanUrl.substring(0, tabanUrl.length() - 1) : tabanUrl;
    }

    /** Aktif test thread'inin Playwright API bağlamıyla istemci oluşturur. */
    public static ApiClient aktifOturumdan(String tabanUrl) {
        APIRequestContext context = PlaywrightManager.getApiRequestContext();
        if (context == null) {
            throw new IllegalStateException("Aktif Playwright API bağlamı yok; test başlatılmamış olabilir.");
        }
        return new ApiClient(context, tabanUrl);
    }

    public ApiYanit get(String yol) {
        return get(yol, Map.of());
    }

    /** GET isteği; parametreler sorgu dizesine eklenir (URL kodlaması Playwright tarafından yapılır). */
    public ApiYanit get(String yol, Map<String, ?> sorguParametreleri) {
        RequestOptions secenekler = RequestOptions.create();
        sorguParametreleri.forEach((ad, deger) -> secenekler.setQueryParam(ad, String.valueOf(deger)));

        String url = tamUrl(yol);
        return cagir("GET", url, sorguParametreleri, null, () -> context.get(url, secenekler));
    }

    /** JSON gövdeli POST isteği (gövde nesnesi JSON'a çevrilir). */
    public ApiYanit post(String yol, Object jsonGovde) {
        String url = tamUrl(yol);
        RequestOptions secenekler = RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData(jsonGovde);
        return cagir("POST", url, Map.of(), jsonGovde, () -> context.post(url, secenekler));
    }

    private ApiYanit cagir(
            String metot,
            String url,
            Map<String, ?> sorguParametreleri,
            Object govde,
            Supplier<APIResponse> istek) {

        long baslangic = System.currentTimeMillis();
        APIResponse yanit = istek.get();

        try {
            ApiYanit sonuc = new ApiYanit(metot, yanit.url(), yanit.status(),
                    new LinkedHashMap<>(yanit.headers()),
                    new String(yanit.body(), StandardCharsets.UTF_8));

            long sure = System.currentTimeMillis() - baslangic;
            ReportManager.info("API | " + metot + " " + url + " | " + sonuc.durumKodu() + " | " + sure + " ms");
            allureyeEkle(sonuc, sorguParametreleri, govde, sure);

            return sonuc;

        } finally {
            yanit.dispose();
        }
    }

    private String tamUrl(String yol) {
        if (yol.startsWith("http://") || yol.startsWith("https://")) {
            return yol;
        }
        return tabanUrl + (yol.startsWith("/") ? yol : "/" + yol);
    }

    /** Aktif Allure testi yoksa (ör. @BeforeClass) sessizce atlar. */
    private static void allureyeEkle(ApiYanit yanit, Map<String, ?> parametreler, Object govde, long sure) {

        if (Allure.getLifecycle().getCurrentTestCase().isEmpty()) {
            return;
        }

        String yanitGovdesi = yanit.govde() == null ? "" : yanit.govde();
        if (yanitGovdesi.length() > RAPOR_GOVDE_SINIRI) {
            yanitGovdesi = yanitGovdesi.substring(0, RAPOR_GOVDE_SINIRI) + "\n… (kısaltıldı)";
        }

        String icerik = yanit.metot() + " " + yanit.url()
                + (parametreler.isEmpty() ? "" : "\nParametreler: " + parametreler)
                + (govde == null ? "" : "\nİstek gövdesi: " + govde)
                + "\n\nDurum: " + yanit.durumKodu() + " (" + sure + " ms)"
                + "\n\n" + yanitGovdesi;

        Allure.addAttachment("API: " + yanit.metot() + " " + yanit.url(), "text/plain", icerik, ".txt");
    }
}