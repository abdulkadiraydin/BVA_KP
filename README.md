# BVA KP Test Otomasyonu

Büyük Veri Analitiği Kurumsal Portalı (AVP) ve Açık Veri Portalı için UI ve API test otomasyonu.

- **Kapalı portal:** veri kaynağı ve veri seti oluşturma, KVKK onayı, yayın, güncelleme takvimi, dashboard
- **Kamuya açık portal:** yayınlanan veri setinin arama, detay, kaynak sayfası ve CKAN API üzerinden doğrulanması

## Teknolojiler

| | Sürüm |
|---|---|
| Java | 21 |
| Maven | 3.10.0 (proje Maven Wrapper kullanmaz; Maven kurulu olmalı) |
| Playwright (Java) | 1.64 |
| TestNG | 7.12 |
| Maven Surefire | 3.5.4 (3.6.0 ve üstü `testng.xml`'i okumuyor; yükseltilmemeli) |
| Allure (kütüphane) | 2.35.5 |
| Allure CLI | 2.24 veya üstü (tek dosyalık rapor için) |
| Spotless / google-java-format | Kod biçimi (Google stili) |

## Kurulum

1. Java 21 ve Maven 3.10.0 kurulu olmalı: `java -version`, `mvn -version`
   - Maven'ı indirip bir klasöre açın (ör. `C:\Program Files\Apache\maven-3.10.0`).
   - `MAVEN_HOME` ortam değişkenini bu klasör yapın, `Path`'e `%MAVEN_HOME%\bin` ekleyin.
   - Terminali ve IntelliJ'i yeniden başlatıp `mvn -version` ile 3.10.0 göründüğünü kontrol edin.
2. Allure CLI kurulu ve `PATH`'te olmalı: `allure --version`
3. Bağımlılıkları indir ve derle:
   ```powershell
   mvn test-compile
   ```
4. Test kullanıcısının parolasını `BVAKP_PASSWORD` ortam değişkeni olarak tanımla (bkz. Ayarlar).
5. Mail gönderilecekse proje kökünde `.env.mail` dosyası oluştur (Git'e eklenmez; bkz. Mail).
6. IntelliJ'e **google-java-format** eklentisini kurup etkinleştir; Settings → Editor → General → Smart Keys → *Reformat on paste* ayarını **None** yap (bkz. Kod biçimi).

> Windows terminalinde Türkçe karakterler bozuk görünüyorsa PowerShell profiline
> (`notepad $PROFILE`) şunları ekle:
> ```powershell
> [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
> $OutputEncoding = [System.Text.Encoding]::UTF8
> chcp 65001 > $null
> ```

## Testleri çalıştırma

```powershell
mvn test                                         # testng.xml (varsayılan)
mvn test "-Dsuite.xml=smoke.xml"                 # hızlı kontrol
mvn test "-Dsuite.xml=regresyon.xml"             # tam regresyon
mvn test "-Dtest=PublicOpenDataTest"             # tek sınıf
mvn test "-Dtest=LoginTest,OpenDataDashboardTest" # birkaç sınıf
mvn test "-Dtest=PublicOpenDataTest" "-Dheadless=false"  # tarayıcıyı izleyerek
mvn test "-Dtest=PublicOpenDataTest" "-Dtrace.mode=on"   # bütün testlerin trace'ini sakla
mvn test "-Dtest.retry.count=1"                  # kırılan testi 1 kez tekrar dene
```

| Paket | İçerik | Ne zaman |
|---|---|---|
| `smoke.xml` | Her modülün ana akışı (giriş, dashboard, kaynak oluşturma, onaya gönderme, yayın, portalda arama ve maske) | Her değişiklikten sonra |
| `regresyon.xml` | Bütün test sınıfları | Gece, sürüm öncesi, büyük değişiklik sonrası |
| `testng.xml` | Bütün test sınıfları (`parallel="methods"`) | IntelliJ'den ve `mvn test` ile varsayılan |

Paket `pom.xml`'deki `suite.xml` özelliğiyle seçilir; IntelliJ'de xml dosyasına sağ tık → *Run* de kullanılabilir.

- `smoke.xml` ve `regresyon.xml` `parallel="classes"`, `thread-count="2"` ile koşar: aynı anda daha az giriş yapılır, Keycloak kilitlenmesi riski azalır.
- Test ortamı yüksek paralellikte yetişemiyor (5'te tablolar yüklenmedi); `thread-count` 3'ün üstüne çıkarılmamalı.
- Smoke'ta yalnızca seçilen testler koşsa da sınıfın `@BeforeClass` hazırlığı yine çalışır.
- `<include name="...">` içindeki metot adı yanlışsa TestNG hata vermez, testi atlar; metot adı değişirse `smoke.xml` de güncellenmelidir.

## Ayarlar

```
src/test/resources/config/
  config.properties              bütün ortamlarda ortak ayarlar          (Git'te)
  env/test.properties            test ortamı: adresler, kullanıcı adı    (Git'te)
  config.local.properties        kişisel ayarlar, isteğe bağlı           (Git'e girmez)
  config.local.properties.ornek  kişisel ayar dosyası örneği
```

Ortam `-Denv=test` (veya `BVAKP_ENV`) ile seçilir; verilmezse `test` kullanılır.
Yeni bir ortam eklemek için `env/<ad>.properties` dosyası oluşturmak yeterlidir.

Bir değer şu öncelikle okunur:

1. `-D` parametresi (ör. `mvn test "-Dheadless=false"`)
2. Ortam değişkeni: `BVAKP_` + anahtarın büyük harfli hali, nokta yerine alt çizgi
   (ör. `password` → `BVAKP_PASSWORD`, `base.url` → `BVAKP_BASE_URL`)
3. `config.local.properties`
4. `env/<ortam>.properties`
5. `config.properties`
6. Koddaki varsayılan

**Parola hiçbir dosyaya yazılmaz.** Bir kez tanımlayın, ardından IntelliJ'i yeniden başlatın:

```powershell
[Environment]::SetEnvironmentVariable("BVAKP_PASSWORD", "<parola>", "User")
```

| Anahtar | Açıklama | Varsayılan |
|---|---|---|
| `base.url` | Kapalı portal adresi (test ortamı) | |
| `public.data.url` | Kamuya açık portal adresi | |
| `username` | Test kullanıcısı (ortam dosyasında) | |
| `password` | Parola; yalnızca `BVAKP_PASSWORD` ortam değişkeniyle | |
| `browser`, `headless` | Tarayıcı ve görünmez mod (CI'da `headless=true`) | |
| `browser.locale` | Tarayıcı dili; sayı ve tarih biçimini belirler (`3.529`) | `tr-TR` |
| `ignore.https.errors` | Sertifika hatalarını yok say (kamuya açık portal self-signed sertifika kullanıyor). **Yalnızca `env/test.properties`'te** `true`; canlı ortamda açılmamalı | `false` |
| `viewport.width`, `viewport.height` | Pencere boyutu | 1280 × 720 |
| `auth.state.reuse` | Oturumu dosyada saklayıp testler arasında paylaş; paralel koşuda `false` kalmalı | `false` |
| `trace.mode` | `off`, `on`, `retain-on-failure` | `retain-on-failure` |
| `video.mode` | `off`, `on`, `retain-on-failure` | `retain-on-failure` |
| `allure.attach.trace`, `allure.attach.video` | Trace/videoyu Allure raporuna ekle | `true` / `false` |
| `allure.report.auto.generate` | Koşu sonunda raporu otomatik üret | `true` |
| `allure.report.timeout.seconds` | Rapor üretimi zaman aşımı | `300` |
| `kosu.saklama.adedi` | Saklanacak koşu sayısı; eskiler silinir (`0` = silme) | `20` |
| `test.retry.count` | Kırılan test kaç kez tekrar denensin (Jenkins'te `1`) | `0` |
| `failure.tracking.enabled`, `failure.alert.threshold` | Ardışık hata sayacı ve alarm maili eşiği | |
| `cleanup.enabled` | Koşu sonunda otomasyon kaynaklarını sil | |
| `cleanup.allowed.hosts` | Temizliğin çalışabileceği host'lar (ortam dosyasında); boşsa hiçbir yerde silmez | |
| `cleanup.name.prefix` | Yalnızca bu önekle başlayan kaynaklar silinir | `otomasyon_` |
| `cleanup.max.delete` | Bir koşuda silinebilecek en fazla kaynak | |

## Kod biçimi

Kod Google Java stiliyle biçimlenir (Spotless + google-java-format: 2 boşluk girinti, 4 boşluk devam satırı, 100 karakter satır).

```powershell
mvn spotless:apply   # bütün dosyaları biçimle, kullanılmayan importları sil
mvn spotless:check   # biçim hatası varsa build'i kırar (commit/CI öncesi)
```

IntelliJ'de google-java-format eklentisi açıkken `Ctrl+Alt+L` aynı biçimi uygular.

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
testng.xml, smoke.xml, regresyon.xml   TestNG paketleri (bkz. Testleri çalıştırma)
Jenkinsfile                            Jenkins pipeline'ı (bkz. Jenkins)
```

## Yazım kuralları

- **Katmanlar:** Page sınıfları ekranı okur ve tıklar; Flow sınıfları iş adımlarını birleştirir; doğrulama (`Assert`) testlerde yapılır. Flow'larda yalnızca "Ön koşul:" ile başlayan assert'ler bulunur.
- **Adlandırma:** Metot adları Türkçedir ve işi anlatır (`veriSetiArama`, `kategoriOkuma`, `detaySayfasiGoruntulendiMi`).
- **Görünürlük kontrolü:** `BasePage.gorunurMu` / `gorunmuyorMu` kullanılır; sayfa sınıflarında try/catch beklemesi tekrar yazılmaz.
- **Bekleme süresi:** Element ve `assertThat` beklemeleri varsayılan olarak 15 sn'dir.
- **Test verisi:** Oluşturulan her kaynak ve veri setinin adı `otomasyon_` ile başlar ve zaman damgası içerir (`TestDataUtil.dinamikAdOlusturma`).
- **Ortak veri:** Aynı veri setini kullanan sınıflar onu `@BeforeClass`'ta bir kez hazırlar (ör. `OpenDataUpdateCalendarTest`, `PublicOpenDataTest`). Bu testler veriyi yalnızca okur ve paralel çalışabilir. Veriyi değiştiren bir test kendi verisini oluşturmalıdır.
- **API testleri:** Yalnızca API çağıran testler `@ApiTesti` ile işaretlenir. Bu testlerde tarayıcı açılmaz, `page()` null döner; kanıt Allure'daki istek/yanıt ekleridir.
- **DataProvider:** İlk parametre okunabilir bir senaryo adı olmalıdır; rapordaki test adına eklenir (ör. `trendDonemSecimi [Haftalık]`). İlk parametre **hiçbir zaman parola olmamalıdır**.
- **Javadoc:** Her sınıf ve public metodun kısa bir Javadoc açıklaması bulunur.
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

### Tekrar deneme (retry)

Kırılan test `test.retry.count` kadar tekrar denenir (varsayılan `0`; Jenkins'te `1`). `RetryTransformer` bütün testlere `RetryAnalyzer`'ı atar ve suite xml'lerinin `<listeners>` bölümünde tanımlıdır; **yeni bir suite xml'i açılırsa oraya da eklenmelidir.**

- Logda `TEST TEKRAR DENENECEK` ve `TEST TEKRAR DENENDİ | ... | İlk deneme hatası: ...` satırları çıkar.
- `ozet.html`, mail özeti ve koşu klasörü adı yalnızca son denemeyi sayar: ikinci denemede geçen test başarılıdır, atlanan sayılmaz.
- İlk denemenin ayrıntısı Allure'da testin *Retries* sekmesindedir.
- Jenkins'in kendi test ekranında ilk deneme *skipped* görünür; bu TestNG'nin XML'inden gelir, normaldir.
- Ardışık hata sayacı ve alarm maili yalnızca son deneme de kırılırsa işler.

## Mail

- **Rapor maili:** Koşu bittikten sonra `MailRunner` ayrı bir süreç olarak çalıştırılır. Ekte `BVA_KP_Test_Raporu_<tarih>.zip` vardır; içinde `ozet.html` ve ekran görüntüleri bulunur. `RAPOR.html`, video ve trace boyut ve hassas veri nedeniyle maile eklenmez.
- **Rapor durumu:** Başarısız veya atlanan test varsa ya da hiç test koşmadıysa `HATALI`, aksi halde `BASARILI`.
- **Hata alarm maili:** Bir test art arda `failure.alert.threshold` kez kırılırsa test koşarken gönderilir; aynı test için günde en fazla bir kez. Test geçince sayaç sıfırlanır.
- SMTP ayarları önce ortam değişkenlerinden, yoksa proje kökündeki `.env.mail` dosyasından okunur:

| Değişken | Açıklama |
|---|---|
| `MAIL_HOST`, `MAIL_PORT` | SMTP sunucusu ve portu (Gmail: `smtp.gmail.com`, `587`) |
| `MAIL_USERNAME` | SMTP giriş kullanıcısı |
| `MAIL_PASSWORD` | SMTP parolası (Gmail'de uygulama şifresi); hiçbir zaman Git'e girmez |
| `MAIL_FROM` | Gönderen adres (Gmail'de `MAIL_USERNAME` ile aynı) |
| `MAIL_TO` | Alıcılar, virgülle ayrılmış |
| `MAIL_STARTTLS` | STARTTLS (varsayılan `true`) |

Rapor mailini elle göndermek için (son koşunun özeti):

```powershell
mvn exec:java "-Dexec.mainClass=com.bvakp.automation.mail.MailRunner" "-Dexec.classpathScope=test"
```

## Jenkins

`Jenkinsfile` smoke paketini koşturur ve sonucu ne olursa olsun rapor mailini gönderir.
1. `mvn clean test "-Dsuite.xml=smoke.xml"` (headless, `test.retry.count=1`; `RAPOR.html` üretilmez, Allure raporu Jenkins eklentisiyle oluşur).
2. `post { always }`: JUnit sonuçları Jenkins'e yüklenir ve `MailRunner` çalışır. Mail gönderilemezse build **UNSTABLE** (sarı) olur; testler kırılırsa **FAILURE** (kırmızı).

Jenkins'te tanımlanması gerekenler:

| Nerede | Ne |
|---|---|
| Jenkins'i Yönet → Credentials (*Secret text*) | `bvakp-password`, `bvakp-password-hazirlayan`, `bvakp-password-kvkk`, `bvakp-password-yayin`, `bvakp-portal-password-talep`, `bvakp-portal-password-onay`, `bvakp-mail-password` |
| Jenkins'i Yönet → Sistem → Global özellikler → Ortam değişkenleri | `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_FROM`, `MAIL_TO` |
| Global Tool Configuration | `Maven` adında Maven kurulumu |

- Parolalar yalnızca Credentials'ta tutulur; Global özelliklere parola yazılmaz (orada düz metin görünür).
- `.env.mail` Jenkins çalışma alanına kopyalanmaz.
- Jenkinsfile şu an Windows agent içindir (`bat`). Linux agent'ta `bat` yerine `sh`, `copy /Y` yerine `cp` kullanılır.
- Jenkins makinesi test ortamına (VPN gerektirmeden) erişebilmeli; Maven ve Playwright indirmeleri ile SMTP çıkışı açık olmalıdır.

## Temizlik

`cleanup.enabled=true` iken koşu sonunda `otomasyon_` önekli kaynaklar silinir. Temizlik yalnızca `cleanup.allowed.hosts` listesindeki ortamlarda çalışır; liste boşsa hiçbir şey silinmez. Canlı ortam bu listeye **eklenmemelidir**.

## Git'e eklenmeyenler

`.env.mail`, `config.local.properties`, `.automation-state/`, `target/`, `reports/`. Trace ve videolar sayfa içeriği ve ağ verisi barındırabileceği için hassas kabul edilir.