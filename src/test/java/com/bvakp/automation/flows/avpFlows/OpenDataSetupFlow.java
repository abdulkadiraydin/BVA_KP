package com.bvakp.automation.flows.avpFlows;

import com.bvakp.automation.model.VeriSetiTanimi;
import com.bvakp.automation.model.YayinAyarlari;
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
     * Yayında bir veri seti hazırlar ve yalnızca adını döndürür.
     * Ayrıntılı değerlere ihtiyacı olmayan testler içindir (ör. Güncelleme Takvimi).
     */
    public String yayindaVeriSetiOlusturma(String adOnEki) {
        return yayindaVeriSetiTanimiOlusturma(adOnEki).ad();
    }

    /**
     * Kaynak ve veri seti oluşturur, KVKK onayından geçirir, yayınlar ve portala
     * aktarılmasını bekler. Her adımda ekrandaki değerleri toplar.
     *
     * @param adOnEki veri seti adının ön eki; "otomasyon_" ile başlamalıdır
     * @return kamuya açık portalda karşılaştırılacak beklenen değerler
     */
    public VeriSetiTanimi yayindaVeriSetiTanimiOlusturma(String adOnEki) {

        String kaynakAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");
        String veriSetiAdi = TestDataUtil.dinamikAdOlusturma(adOnEki);

        ReportManager.step("Ön koşul: yayında veri seti hazırlanıyor | " + veriSetiAdi);

        createFlow.createSource(kaynakAdi, KAYNAK_TABLOSU, "Otomasyon kaynağı - " + kaynakAdi);

        VeriSetiTanimi tanim = createFlow.createDataSetForApproval(kaynakAdi, veriSetiAdi,
                SORUMLU_BIRIM, KATEGORI, KULLANIM_LISANSI, "Otomasyon veri seti - " + veriSetiAdi);
        publishPage.waitForStatus(veriSetiAdi, "KVKK Onayında");

        publishFlow.approveKvkk(veriSetiAdi);
        publishPage.waitForStatus(veriSetiAdi, "Yayın Onayında");

        YayinAyarlari yayinAyarlari = publishFlow.publishDataSet(veriSetiAdi);
        publishPage.waitForStatus(veriSetiAdi, "Yayında");
        publishPage.waitForPortalStatus(veriSetiAdi, "Aktarıldı");

        ReportManager.info("Ön koşul hazır: yayında ve portala aktarıldı | " + veriSetiAdi
                + " | Maskelenen: " + tanim.maskelenenKolonlar()
                + " | Formatlar: " + yayinAyarlari.formatlar());

        return tanim.yayinAyarlariIle(yayinAyarlari);
    }
}