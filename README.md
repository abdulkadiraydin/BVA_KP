# BVA KP Test Otomasyonu

Büyük Veri Analitiği ve Kaynak Planlama (BVA KP) projesinin Açık Veri Portalı (AVP) modülü için UI ve API test otomasyonu.

- **Kapalı portal:** veri kaynağı ve veri seti oluşturma, KVKK onayı, yayın, güncelleme takvimi, dashboard
- **Kamuya açık portal (CKAN):** giriş/çıkış, veri talebi, API anahtarı; yayınlanan veri setinin arama, detay, kaynak sayfası ve CKAN API üzerinden doğrulanması

## Teknolojiler

| | Sürüm |
|---|---|
| Java | 21 |
| Maven | 3.10.0 (proje Maven Wrapper kullanmaz; Maven kurulu olmalı) |
| Playwright (Java) | 1.64 |
| TestNG | 7.12 |
| Maven Surefire | 3.5.4 (3.6.0 ve üstü `regression.xml`'i okumuyor; yükseltilmemeli) |
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
mvn test                                         # regression.xml, test ortamı (varsayılan)
mvn test "-Denv=dev"                             # aynı paket, dev ortamı
mvn test "-Dsuite.xml=smoke.xml"                 # hızlı kontrol
mvn test "-Dtest=PublicOpenDataTest"             # tek sınıf
mvn test "-Dtest=OpenDataPublishTest#veriSetiArama" # tek test
mvn test "-Dtest=LoginTest,OpenDataDashboardTest" # birkaç sınıf
mvn test "-Dtest=PublicOpenDataTest" "-Dheadless=false"  # tarayıcıyı izleyerek
mvn test "-Dtest=PublicOpenDataTest" "-Dtrace.mode=on"   # bütün testlerin trace'ini sakla
mvn test "-Dtest.retry.count=1"                  # kırılan testi 1 kez tekrar dene
```

| Paket | İçerik | Ne zaman |
|---|---|---|
| `smoke.xml` | Her modülün ana akışı (giriş, dashboard, kaynak oluşturma, onaya gönderme, yayın, portalda arama ve maske) | Her değişiklikten sonra |
| `regression.xml` | Bütün test sınıfları (`parallel="methods"`, 3 thread) | Jenkins'te saat başı, sürüm öncesi; `mvn test` ile varsayılan |

Paket `pom.xml`'deki `suite.xml` özelliğiyle seçilir; IntelliJ'de xml dosyasına sağ tık → *Run* de kullanılabilir.

**IntelliJ'de dev ortamında koşmak:** Çalıştırma ayarında (*Edit Configurations*) **VM options** alanına `-ea -Denv=dev` yazılır. Sağ tık → *Run* ile oluşan ayarlar geçicidir ve bir sonraki sağ tıkta sıfırlanır; ayarı disket simgesiyle (*Save Configuration*) kaydedip üstteki listeden çalıştırın. Bütün yeni ayarların dev'e gitmesi için *Edit configuration templates → TestNG* kullanılabilir; iş bitince şablon `-ea`'ya geri alınmalıdır.

- Test ortamı yüksek paralellikte yetişemiyor (5'te tablolar yüklenmedi); `thread-count` 3'ün üstüne çıkarılmamalı.
- Smoke'ta yalnızca seçilen testler koşsa da sınıfın `@BeforeClass` hazırlığı yine çalışır.
- `<include name="...">` içindeki metot adı yanlışsa TestNG hata vermez, testi atlar; metot adı değişirse `smoke.xml` de güncellenmelidir.

## Ayarlar

```
src/test/resources/config/
  config.properties              bütün ortamlarda ortak ayarlar                     (Git'te)
  env/test.properties            test ortamı (VPN): adresler, kullanıcılar, test verisi (Git'te)
  env/dev.properties             dev ortamı (Contabo): adresler, kullanıcılar, test verisi (Git'te)
  config.local.properties        kişisel ayarlar, isteğe bağlı           (Git'e girmez)
  config.local.properties.ornek  kişisel ayar dosyası örneği
```

Ortam `-Denv=<ad>` (veya `BVAKP_ENV`) ile seçilir; verilmezse `test` kullanılır.
`config.local.properties` içine `env=...` yazmak işe yaramaz: ortam, dosyalar okunmadan önce belirlenir.

| Ortam | Adresler | Erişim |
|---|---|---|
| `test` | `10.161.71.30` | VPN gerekir |
| `dev` | `*.buyukverianalitigi.com` | İnternetten erişilir (Contabo Jenkins'i bunu koşar) |

Yeni bir ortam eklemek için mevcut bir `env/<ad>.properties` dosyasını kopyalayıp değerlerini değiştirmek yeterlidir. Ayar dosyaları **UTF-8** okunur; Türkçe değerler doğrudan yazılabilir (IntelliJ'de *File Encodings → properties files* UTF-8 olmalı).

### Ortama özgü test verisi

Ortama göre değişen test verisi kodda değil, `env/<ortam>.properties` dosyasındadır ve `TestDataConfig` sınıfından okunur:

| Anahtar | Açıklama | Boşsa |
|---|---|---|
| `testdata.kaynak.sema` | Kaynak Ekle'de seçilen şema (ör. `dwh`) | Zorunlu |
| `testdata.kaynak.tablo` | Kaynak ve veri seti testlerinin tablosu (ör. `d_hat`); Hazır Sorgu ve UPDATE testlerinin sorgusu da bundan üretilir | Zorunlu |
| `testdata.maskelenecek.kolon` | Maskele kuralı uygulanan kolon (ör. `hat`) | Zorunlu |
| `testdata.sorumlu.birim`, `testdata.kategori`, `testdata.kullanim.lisansi` | Veri seti formunda seçilecek değerler | Listedeki ilk seçenek seçilir |
| `testdata.duzenleme.sorumlu.birim`, `testdata.duzenleme.kategori` | Düzenleme testlerinde seçilecek yeni değerler | Mevcut değerden farklı ilk seçenek seçilir |

Seçilen gerçek değer okunup saklanır; kamuya açık portal testleri bu değerle karşılaştırır. Yeni bir ortamda yalnızca ilk üç zorunlu anahtarın doldurulması yeterlidir.

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
| `browser`, `headless` | Tarayıcı (`chromium`, `firefox`, `webkit`) ve görünmez mod (CI'da `headless=true`) | `chromium` |
| `browser.locale` | Tarayıcı dili; sayı ve tarih biçimini belirler (`3.529`) | `tr-TR` |
| `ignore.https.errors` | Sertifika hatalarını yok say (kamuya açık portal self-signed sertifika kullanıyor). **Yalnızca `env/test.properties`'te** `true`; canlı ortamda açılmamalı | `false` |
| `viewport.width`, `viewport.height` | Pencere boyutu | 1280 × 720 |
| `auth.state.reuse` | Kapalı portal oturumunu dosyada saklayıp testler arasında paylaş; paralel koşuda `false` kalmalı | `false` |
| `trace.mode` | `off`, `on`, `retain-on-failure`; Jenkins'te `off` (bkz. Raporlar) | `retain-on-failure` |
| `video.mode` | `off`, `on`, `retain-on-failure` | `retain-on-failure` |
| `allure.attach.trace`, `allure.attach.video` | Trace/videoyu Allure raporuna ekle | `true` / `false` |
| `allure.report.auto.generate` | Koşu sonunda raporu otomatik üret | `true` |
| `allure.report.timeout.seconds` | Rapor üretimi zaman aşımı | `300` |
| `kosu.saklama.adedi` | Saklanacak koşu sayısı; eskiler silinir (`0` = silme) | `20` |
| `test.retry.count` | Kırılan test kaç kez tekrar denensin (Jenkins'te `1`) | `0` |
| `failure.tracking.enabled`, `failure.alert.threshold` | Ardışık hata sayacı ve alarm maili eşiği | `false` / `5` |
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
    config/          ConfigManager, TestDataConfig (ortama özgü test verisi)
    playwright/      PlaywrightManager, BrowserFactory, TraceManager
    auth/            Oturum dosyası yönetimi (kamuya açık portalda rol bazlı oturum: PublicPortalAuthStateManager)
  flows/             İş adımları (kaynak oluştur, onaya gönder, yayınla, ön koşul hazırla)
  mail/              Rapor maili ve hata alarm maili
  model/             VeriSetiTanimi, KolonTanimi, YayinAyarlari: ekranlardan toplanan beklenen değerler
  pages/
    openData/        Kapalı portal sayfaları
    publicPortal/    Kamuya açık portal sayfaları (giriş, Hesabım, veri talebi, arama, veri seti detayı, kaynak)
  reporting/         TestListener, Allure/özet raporu, koşu arşivi
  tests/             Test sınıfları
  utils/             Ekran görüntüsü, video, ZIP, test verisi yardımcıları
scripts/
  Generate-AllureReport.ps1   Yerelde (Windows) Allure raporunu ve history'yi üretir
  generate-allure-report.sh   Aynı işin Linux/macOS karşılığı
regression.xml, smoke.xml   TestNG paketleri (bkz. Testleri çalıştırma)
Jenkinsfile                 Jenkins pipeline'ı, Linux/Kubernetes (bkz. Jenkins)
.gitattributes              .sh dosyaları her zaman LF satır sonuyla saklanır
```

## Yazım kuralları

- **Katmanlar:** Page sınıfları ekranı okur ve tıklar; Flow sınıfları iş adımlarını birleştirir; doğrulama (`Assert`) testlerde yapılır. Flow'larda yalnızca "Ön koşul:" ile başlayan assert'ler bulunur.
- **Adlandırma:** Metot adları Türkçedir ve işi anlatır (`veriSetiArama`, `kategoriOkuma`, `detaySayfasiGoruntulendiMi`).
- **Görünürlük kontrolü:** `BasePage.gorunurMu` / `gorunmuyorMu` kullanılır; sayfa sınıflarında try/catch beklemesi tekrar yazılmaz.
- **Bekleme süresi:** Element ve `assertThat` beklemeleri varsayılan olarak 15 sn'dir.
- **Test verisi:** Oluşturulan her kaynak ve veri setinin adı `otomasyon_` ile başlar ve zaman damgası içerir (`TestDataUtil.dinamikAdOlusturma`).
- **Ortak veri:** Aynı veri setini kullanan sınıflar onu `@BeforeClass`'ta bir kez hazırlar (ör. `OpenDataUpdateCalendarTest`, `PublicOpenDataTest`). Bu testler veriyi yalnızca okur ve paralel çalışabilir. Veriyi değiştiren bir test kendi verisini oluşturmalıdır.
- **API testleri:** Yalnızca API çağıran testler `@ApiTesti` ile işaretlenir. Bu testlerde tarayıcı açılmaz, `page()` null döner; kanıt Allure'daki istek/yanıt ekleridir.
- **DataProvider:** İlk parametre okunabilir bir senaryo adı olmalıdır; rapordaki test adına eklenir (ör. `donemSecimi [Haftalık]`). İlk parametre **hiçbir zaman parola olmamalıdır**.
- **Javadoc:** Her sınıf ve public metodun kısa bir Javadoc açıklaması bulunur.
- **Yorumlar ve loglar:** Kısa tutulur; yorum "ne"yi değil "neden"i anlatır.

## Kamuya açık portal: oturum ve robot doğrulaması

- **Oturum tekrar kullanımı:** CKAN, kısa sürede çok giriş yapılınca `429 (RATE_LIMIT_EXCEEDED)` döner. Bu yüzden her rolün (TALEP, ONAY) oturum çerezleri ilk girişte `.automation-state/public-portal-<rol>.json` dosyasına yazılır ve sonraki testlerde yüklenir. Oturum geçersizse yeniden giriş yapılır. Bu davranış `auth.state.reuse`'dan bağımsızdır ve her zaman açıktır.
- `PublicPortalLoginTest.cikisYapma` kayıtlı oturumu bozmamak için kendi gerçek girişini yapar (`gercekGirisYapma`).
- **Robot doğrulaması (ALTCHA):** Giriş ve veri talebi formunda çıkabilir. Widget varsa `PublicPortalBasePage.robotDogrulamasiniTamamlama` kutuya tıklar ve `verified` durumunu en fazla 120 sn bekler. Chromium'da birkaç saniye sürer; WebKit'te 50–80 sn sürebilir.

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
- **Trace, form alanlarına girilen değerleri (parolalar dahil) açık metin olarak saklar.** Bu yüzden Jenkins'te kapalıdır (`BVAKP_TRACE_MODE=off`); yerel trace dosyaları paylaşılmamalıdır.
- Sayfa, tarayıcının ekran görüntüsü sınırından uzunsa (WebKit: 32767 px) tam sayfa yerine görünen alanın ekran görüntüsü alınır.
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
- **Hata alarm maili:** Varsayılan olarak kapalıdır (`failure.tracking.enabled=false`). Açıksa, bir test art arda `failure.alert.threshold` kez kırıldığında test koşarken gönderilir; aynı test için günde en fazla bir kez. Test geçince sayaç sıfırlanır.
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

Tek bir `Jenkinsfile` vardır; Kubernetes üzerinde, Playwright'ın resmi Java imajıyla (`mcr.microsoft.com/playwright/java`) bir Linux pod'unda koşar. İmaj sürümü `pom.xml`'deki Playwright sürümüyle aynı olmalıdır.

**Hangi ortamın test edileceği Jenkinsfile'da değil, her Jenkins'in kendi ayarındadır** (`BVAKP_ENV`). Aynı dosya iki Jenkins'te de değişmeden çalışır:

| Jenkins | `BVAKP_ENV` | Okunan dosya |
|---|---|---|
| Contabo (Kubernetes) | `dev` | `env/dev.properties` |
| VPN'li Jenkins (Kubernetes) | `test` | `env/test.properties` |

`BVAKP_ENV` tanımlı değilse koşu en başta açık bir hata mesajıyla durur.

Koşu adımları:
1. `mvn -B clean test -Dsuite.xml=<SUITE>`: paket *Parametrelerle Yapılandır* ekranında seçilir (`regression.xml` varsayılan). Headless, `test.retry.count=1`, trace ve video kapalı; `RAPOR.html` üretilmez, Allure raporu Jenkins eklentisiyle oluşur.
2. `post { always }`: JUnit sonuçları yüklenir, `ozet.html` ve ekran görüntüleri artefakt olarak saklanır, Allure raporu üretilir ve `MAIL_TO` tanımlıysa `MailRunner` çalışır. Mail gönderilemezse build **UNSTABLE** (sarı) olur; testler kırılırsa **FAILURE** (kırmızı).

Zamanlama: yalnızca `main` dalında, hafta içi 08:00–18:00 arası saat başı regresyon. Diğer dallar (multibranch) yalnızca push ile koşar. Aynı anda tek koşu yapılır, bir koşu en fazla 50 dakika sürebilir. Pod UTC çalıştığı için saat dilimi `TZ` ve `-Duser.timezone` ile `Europe/Istanbul` yapılır; takvim testlerindeki "bugün" ve temizliğin yaş hesabı buna bağlıdır.

Jenkins'te tanımlanması gerekenler:

| Nerede | Ne |
|---|---|
| Jenkins'i Yönet → Credentials (*Secret text*) | `bvakp-password`, `bvakp-password-hazirlayan`, `bvakp-password-kvkk`, `bvakp-password-yayin`, `bvakp-portal-password-talep`, `bvakp-portal-password-onay`, `bvakp-mail-password` |
| Jenkins'i Yönet → Credentials (*Secret text*) | ek olarak `bvakp-password-temizlik` |
| Jenkins'i Yönet → Sistem → Global özellikler → Ortam değişkenleri | `BVAKP_ENV` (`dev` / `test`), `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_FROM`, `MAIL_TO` |
| Eklentiler | Kubernetes, Allure (`allure` adında komut satırı aracı), JUnit |

- Parolalar yalnızca Credentials'ta tutulur; Global özelliklere parola yazılmaz (orada düz metin görünür).
- Jenkinsfile `BVAKP_TRACE_MODE=off` tanımlar; trace Jenkins'te üretilmez.
- `.env.mail` Jenkins çalışma alanına kopyalanmaz.
- Jenkinsfile'da Windows'a özgü komut yoktur (`bat`, `chcp`); bütün adımlar `sh` ile çalışır.
- Kümenin, `BVAKP_ENV` ile seçilen ortama erişebilmesi gerekir (Contabo kümesi VPN'e erişemez, bu yüzden `dev`'i koşar). Maven indirmeleri ve SMTP çıkışı açık olmalıdır.
- Chromium'un paralel koşuda çökmemesi için pod'a 1 GB `/dev/shm` bağlanır.

## Temizlik

`cleanup.enabled=true` iken koşu sonunda `otomasyon_` önekli kaynaklar silinir. Temizlik yalnızca `cleanup.allowed.hosts` listesindeki ortamlarda çalışır (her ortamın kendi dosyasında tanımlıdır); liste boşsa hiçbir şey silinmez. Canlı ortam bu listeye **eklenmemelidir**.

## Git'e eklenmeyenler

`.env.mail`, `config.local.properties`, `.automation-state/` (oturum çerezleri dahil), `target/`, `reports/`, `logs/`. Trace ve videolar sayfa içeriği ve ağ verisi barındırabileceği için hassas kabul edilir.