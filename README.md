# BVA KP Test Otomasyonu

Büyük Veri Analitiği Kurumsal Portalı (AVP) ve Açık Veri Portalı için UI ve API test otomasyonu.

- **Kapalı portal:** veri kaynağı ve veri seti oluşturma, KVKK onayı, yayın, güncelleme takvimi, dashboard
- **Kamuya açık portal:** yayınlanan veri setinin arama, detay, kaynak sayfası ve CKAN API üzerinden doğrulanması

## Teknolojiler

| | Sürüm |
|---|---|
| Java | 21 |
| Maven | 3.x |
| Playwright (Java) | 1.41 |
| TestNG | 7.8 |
| Allure (kütüphane) | 2.35 |
| Allure CLI | 2.24 veya üstü (tek dosyalık rapor için) |

## Kurulum

1. Java 21 ve Maven kurulu olmalı: `java -version`, `mvn -version`
2. Allure CLI kurulu ve `PATH`'te olmalı: `allure --version`
3. Bağımlılıkları indir ve derle:
   ```powershell
   mvn test-compile
   ```
4. Mail gönderilecekse proje kökünde `.env.mail` dosyası oluştur (Git'e eklenmez).

> Windows terminalinde Türkçe karakterler bozuk görünüyorsa PowerShell profiline
> (`notepad $PROFILE`) şunları ekle:
> ```powershell
> [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
> $OutputEncoding = [System.Text.Encoding]::UTF8
> chcp 65001 > $null
> ```

## Testleri çalıştırma

```powershell
mvn test                                         # testng.xml'deki bütün sınıflar
mvn test "-Dtest=PublicOpenDataTest"             # tek sınıf
mvn test "-Dtest=LoginTest,OpenDataDashboardTest" # birkaç sınıf
mvn test "-Dtest=PublicOpenDataTest" "-Dheadless=false"  # tarayıcıyı izleyerek
mvn test "-Dtest=PublicOpenDataTest" "-Dtrace.mode=on"   # bütün testlerin trace'ini sakla
```

`testng.xml` testleri `parallel="methods"`, `thread-count="3"` ile koşar.
Test ortamı daha yüksek paralellikte yetişemiyor (5'te tablolar yüklenmedi); 3'ün üstüne çıkmayın.

## Ayarlar

Ayarlar `src/test/resources/config.properties` dosyasındadır. Bir değer şu öncelikle okunur:

1. `-D` parametresi (ör. `mvn test "-Dheadless=true"`)
2. Ortam değişkeni: `BVAKP_` ön ekiyle (ör. `BVAKP_PASSWORD`)
3. `config.properties`
4. Koddaki varsayılan

| Anahtar | Açıklama | Varsayılan |
|---|---|---|
| `base.url` | Kapalı portal adresi (test ortamı) | |
| `public.data.url` | Kamuya açık portal adresi | |
| `username`, `password` | Test kullanıcısı; parolayı ortam değişkeniyle vermek tercih edilir | |
| `browser`, `headless` | Tarayıcı ve görünmez mod | |
| `viewport.width`, `viewport.height` | Pencere boyutu | 1280 × 720 |
| `auth.state.reuse` | Oturumu dosyada saklayıp testler arasında paylaş | `false` |
| `trace.mode` | `off`, `on`, `retain-on-failure` | `retain-on-failure` |
| `video.mode` | `off`, `on`, `retain-on-failure` | `retain-on-failure` |
| `allure.attach.trace`, `allure.attach.video` | Trace/videoyu Allure raporuna ekle | `true` / `false` |
| `allure.report.auto.generate` | Koşu sonunda raporu otomatik üret | `true` |
| `allure.report.timeout.seconds` | Rapor üretimi zaman aşımı | `300` |
| `kosu.saklama.adedi` | Saklanacak koşu sayısı; eskiler silinir (`0` = silme) | `20` |
| `failure.tracking.enabled`, `failure.alert.threshold` | Ardışık hata sayacı ve alarm maili eşiği | |
| `cleanup.enabled` | Koşu sonunda otomasyon kaynaklarını sil | |
| `cleanup.allowed.hosts` | Temizliğin çalışabileceği host'lar; boşsa hiçbir yerde silmez | |
| `cleanup.name.prefix` | Yalnızca bu önekle başlayan kaynaklar silinir | `otomasyon_` |
| `cleanup.max.delete` | Bir koşuda silinebilecek en fazla kaynak | |

## Proje yapısı

```
src/test/java/com/bvakp/automation/
  api/
    core/            ApiClient, ApiYanit: genel API istemcisi (Playwright APIRequestContext)
    publicportal/    PublicPortalApi, DatastoreSonuc: kamuya açık portalın CKAN API'si
  base/              BaseTest, AvpBaseTest, @ApiTesti
  components/        Ekranlarda tekrar eden modallar (Kaynak Ekle/Düzenle/Sil...)
  core/
    config/          ConfigManager
    playwright/      PlaywrightManager, BrowserFactory, TraceManager
    auth/            Oturum dosyası yönetimi
  flows/             İş adımları (kaynak oluştur, onaya gönder, yayınla, ön koşul hazırla)
  mail/              Rapor maili ve hata alarm maili
  model/             VeriSetiTanimi, KolonTanimi, YayinAyarlari: ekranlardan toplanan beklenen değerler
  pages/
    openData/        Kapalı portal sayfaları
    publicportal/    Kamuya açık portal sayfaları (arama, veri seti detayı, kaynak)
  reporting/         TestListener, Allure/özet raporu, koşu arşivi
  tests/             Test sınıfları
  utils/             Ekran görüntüsü, video, ZIP, test verisi yardımcıları
scripts/
  Generate-AllureReport.ps1   Koşu sonunda Allure raporunu ve history'yi üretir
```

## Yazım kuralları

- **Katmanlar:** Page sınıfları ekranı okur ve tıklar; Flow sınıfları iş adımlarını birleştirir; doğrulama (`Assert`) testlerde yapılır. Flow'larda yalnızca "Ön koşul:" ile başlayan assert'ler bulunur.
- **Adlandırma:** Metot adları Türkçedir ve işi anlatır (`veriSetiArama`, `kategoriOkuma`, `detaySayfasiGoruntulendiMi`).
- **Görünürlük kontrolü:** `BasePage.gorunurMu` / `gorunmuyorMu` kullanılır; sayfa sınıflarında try/catch beklemesi tekrar yazılmaz.
- **Bekleme süresi:** Element ve `assertThat` beklemeleri varsayılan olarak 15 sn'dir.
- **Test verisi:** Oluşturulan her kaynak ve veri setinin adı `otomasyon_` ile başlar ve zaman damgası içerir (`TestDataUtil.dinamikAdOlusturma`).
- **Ortak veri:** Aynı veri setini kullanan sınıflar onu `@BeforeClass`'ta bir kez hazırlar (ör. `OpenDataUpdateCalendarTest`, `PublicOpenDataTest`). Bu testler veriyi yalnızca okur ve paralel çalışabilir. Veriyi değiştiren bir test kendi verisini oluşturmalıdır.
- **API testleri:** Yalnızca API çağıran testler `@ApiTesti` ile işaretlenir. Bu testlerde tarayıcı açılmaz, `page()` null döner; kanıt Allure'daki istek/yanıt ekleridir.
- **Yorumlar ve loglar:** Kısa tutulur; yorum "ne"yi değil "neden"i anlatır.

## Raporlar

Her koşunun çıktısı `reports` altında toplanır (`target` yalnızca geçici çalışma alanıdır):

```
reports/
  SON-KOSU.html                       en son koşunun raporu (çift tıkla)
  allure-history/                     Allure Trend ve History verisi
  kosular/                            en yeni 20 koşu
    2026-10-22_19-41-05_HATALI/
      RAPOR.html                      Allure raporu, bütün testler (çift tıkla)
      ozet.html                       sade özet; mailde giden
      kanitlar/<Sınıf>/<test>/        ekran.png, video.webm, trace.zip
      kanitlar/_eslesmeyen/           teste bağlanamayan kanıtlar (ör. @BeforeClass)
      teknik/                         allure-results, allure-report, kosu-bilgisi.txt, allure-uretim.log
```

- Video ve trace yalnızca kırılan UI testlerinde saklanır (`retain-on-failure`).
- Trace'i açmak için `trace.zip`'i https://trace.playwright.dev sayfasına sürükleyin; dosya tarayıcıda yerel olarak açılır.
- Çok dosyalı Allure raporu için: `allure open "reports\kosular\<koşu>\teknik\allure-report"`
- Sayfa yüklemesi 5xx dönerse logda `ORTAM HATASI | 503 | ...` satırı çıkar; bu, hatanın testte değil ortamda olduğunu gösterir.

## Mail

- **Rapor maili:** Koşu bittikten sonra `MailRunner` ayrı bir süreç olarak çalıştırılır. Ekte `BVA_KP_Test_Raporu_<tarih>.zip` vardır; içinde `ozet.html` ve ekran görüntüleri bulunur. `RAPOR.html`, video ve trace boyut ve hassas veri nedeniyle maile eklenmez.
- **Hata alarm maili:** Bir test art arda `failure.alert.threshold` kez kırılırsa gönderilir.
- SMTP bilgileri `.env.mail` dosyasından okunur.

## Temizlik

`cleanup.enabled=true` iken koşu sonunda `otomasyon_` önekli kaynaklar silinir. Temizlik yalnızca `cleanup.allowed.hosts` listesindeki ortamlarda çalışır; liste boşsa hiçbir şey silinmez. Canlı ortam bu listeye **eklenmemelidir**.

## Git'e eklenmeyenler

`.env.mail`, `.automation-state/` (oturum dosyası), `target/`, `reports/`. Trace ve videolar sayfa içeriği ve ağ verisi barındırabileceği için hassas kabul edilir.
