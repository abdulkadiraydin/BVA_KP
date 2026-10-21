package com.bvakp.automation.flows.avpFlows;

import com.bvakp.automation.pages.openData.OpenDataPublishPage;
import com.bvakp.automation.reporting.ReportManager;
import com.bvakp.automation.utils.TestDataUtil;

/**
 * Yayınlanmış veriyle çalışan sayfaların (Güncelleme Takvimi, kamuya açık portal)
 * testleri için ön koşul hazırlar.
 *
 * <p>Yayın sürecinin kendisini test eden OpenDataPublishTest bu flow'u kullanmaz;
 * adımları kendisi yapar ve doğrular.
 */
public class OpenDataSetupFlow {

    private static final String KAYNAK_TABLOSU = "arac_bakim_is_yeri_sayisi";
    private static final String SORUMLU_BIRIM = "İşletme Dairesi Başkanlığı";
    private static final String KATEGORI = "Altyapı & Şebeke";
    private static final String KULLANIM_LISANSI = "TCDD Taşımacılık Açık Veri Lisansı";

    private final OpenDataCreateFlow createFlow;
    private final OpenDataPublishPage publishPage;
    private final OpenDataPublishFlow publishFlow;

    public OpenDataSetupFlow(OpenDataCreateFlow createFlow,
                             OpenDataPublishPage publishPage,
                             OpenDataPublishFlow publishFlow) {
        this.createFlow = createFlow;
        this.publishPage = publishPage;
        this.publishFlow = publishFlow;
    }

    /**
     * Kaynak ve veri seti oluşturur, KVKK onayından geçirir, yayınlar ve portala
     * aktarılmasını bekler.
     *
     * @param adOnEki veri seti adının ön eki; "otomasyon_" ile başlamalıdır
     * @return "Yayında" ve portala "Aktarıldı" durumundaki veri setinin adı
     */
    public String yayindaVeriSetiOlusturma(String adOnEki) {

        String kaynakAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");
        String veriSeti = TestDataUtil.dinamikAdOlusturma(adOnEki);

        ReportManager.step("Ön koşul: yayında veri seti hazırlanıyor | " + veriSeti);

        createFlow.createSource(kaynakAdi, KAYNAK_TABLOSU, "Otomasyon kaynağı - " + kaynakAdi);
        createFlow.createDataSetForApproval(kaynakAdi, veriSeti, SORUMLU_BIRIM, KATEGORI, KULLANIM_LISANSI,
                "Otomasyon veri seti - " + veriSeti);
        publishPage.waitForStatus(veriSeti, "KVKK Onayında");

        publishFlow.approveKvkk(veriSeti);
        publishPage.waitForStatus(veriSeti, "Yayın Onayında");

        publishFlow.publishDataSet(veriSeti);
        publishPage.waitForStatus(veriSeti, "Yayında");
        publishPage.waitForPortalStatus(veriSeti, "Aktarıldı");

        ReportManager.info("Ön koşul hazır: yayında ve portala aktarıldı | " + veriSeti);
        return veriSeti;
    }
}