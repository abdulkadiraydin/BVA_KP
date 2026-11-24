pipeline {
    // Kubernetes agent pod'u: Playwright'in resmi Java imajı (JDK 21 + Maven + tarayıcılar + Linux
    // bağımlılıkları). İmaj sürümü pom.xml'deki playwright.version ile AYNI olmalı; farklı olursa
    // Playwright tarayıcıları her koşuda yeniden indirmeye çalışır.
    agent {
        kubernetes {
            defaultContainer 'playwright'
            yaml '''
apiVersion: v1
kind: Pod
spec:
  containers:
    - name: playwright
      image: mcr.microsoft.com/playwright/java:v1.64.0-noble
      command: ["sleep"]
      args: ["infinity"]
      # Küme CPU rezervasyonu dolu (%90+); düşük request ile pod yerleşir,
      # limit sayesinde boşta CPU varsa 3 çekirdeğe kadar kullanır.
      resources:
        requests:
          cpu: "500m"
          memory: "3Gi"
        limits:
          cpu: "3000m"
          memory: "6Gi"
      volumeMounts:
        # Chromium varsayılan 64MB /dev/shm ile paralel koşuda çöker
        - name: dshm
          mountPath: /dev/shm
  volumes:
    - name: dshm
      emptyDir:
        medium: Memory
        sizeLimit: 1Gi
'''
        }
    }

    parameters {
        // Listedeki ilk değer varsayılandır (zamanlanmış koşular da bunu kullanır).
        choice(name: 'SUITE', choices: ['regression.xml', 'smoke.xml'], description: 'Koşturulacak TestNG paketi')
    }

    triggers {
        // Yalnızca main dalında, hafta içi 08:00–18:00 arası saat başı (H: Jenkins'in seçtiği sabit dakika).
        // Multibranch'te her dal kendi job'ıdır; diğer dallar yalnızca push ile koşar.
        cron(env.BRANCH_NAME == 'main' ? 'H 8-18 * * 1-5' : '')
    }

    options {
        // Aynı anda tek koşu: test kullanıcıları ve temizlik çakışmasın
        disableConcurrentBuilds()
        // Saatlik koşular birikmesin
        timeout(time: 50, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '200', artifactNumToKeepStr: '30'))
        timestamps()
    }

    environment {
        // Pod varsayılan olarak UTC; "bugün", dönem tarihleri, kayıt adlarındaki zaman damgası ve
        // temizlik yaş hesabı Türkiye saatine göre olmalı.
        TZ                                 = 'Europe/Istanbul'

        // Maven çıktısı da UTF-8 olsun; yoksa Jenkins konsolunda Türkçe karakterler bozulur
        LANG                               = 'C.UTF-8'
        MAVEN_OPTS                         = '-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -Duser.timezone=Europe/Istanbul'

        // Hedef ortam (BVAKP_ENV) burada değil, her Jenkins'in kendi ayarında tanımlıdır:
        // Jenkins'i Yönet → Sistem → Global properties → Environment variables
        //   Contabo Jenkins'i: BVAKP_ENV=dev   |   VPN'li Jenkins: BVAKP_ENV=test
        // Değer src/test/resources/config/env/<BVAKP_ENV>.properties dosyasını seçer.

        // Jenkins'e özel ayarlar
        BVAKP_HEADLESS                     = 'true'
        // Rapor Jenkins Allure eklentisiyle üretilir (build sayfasında "Allure Report")
        BVAKP_ALLURE_REPORT_AUTO_GENERATE  = 'false'
        BVAKP_TEST_RETRY_COUNT             = '1'
        BVAKP_CLEANUP_ENABLED              = 'true'
        // Ardışık hata alarm maili kapalı; sonuçlar rapor mailiyle takip edilir
        BVAKP_FAILURE_TRACKING_ENABLED     = 'false'
        // Trace girilen değerleri (parolalar dahil) açık metin saklar; Jenkins'te kapalı
        BVAKP_TRACE_MODE                   = 'off'
        BVAKP_VIDEO_MODE                   = 'off'
        // Parolalar (Jenkins Credentials'tan gelir)
        BVAKP_PASSWORD                     = credentials('bvakp-password')
        BVAKP_PASSWORD_HAZIRLAYAN          = credentials('bvakp-password-hazirlayan')
        BVAKP_PASSWORD_KVKK                = credentials('bvakp-password-kvkk')
        BVAKP_PASSWORD_YAYIN               = credentials('bvakp-password-yayin')
        BVAKP_PASSWORD_TEMIZLIK            = credentials('bvakp-password-temizlik')
        BVAKP_PORTAL_PASSWORD_TALEP        = credentials('bvakp-portal-password-talep')
        BVAKP_PORTAL_PASSWORD_ONAY         = credentials('bvakp-portal-password-onay')

        // Rapor maili: MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_FROM, MAIL_TO Jenkins'in genel
        // ortam değişkenlerinden (JCasC globalNodeProperties), parola Credentials'tan gelir.
        MAIL_PASSWORD                      = credentials('bvakp-mail-password')
    }

    stages {
        stage('Testleri çalıştır') {
            steps {
                script {
                    // Tanımsızsa kod varsayılan "test" ortamına gider; erişilemeyen ortamda bütün
                    // testler anlamsız şekilde düşmesin diye koşu baştan durdurulur.
                    if (!env.BVAKP_ENV?.trim()) {
                        error('BVAKP_ENV tanımlı değil. Jenkins\'i Yönet → Sistem → Global properties altında tanımlayın (dev / test).')
                    }
                }
                echo "Suite: ${params.SUITE} | Ortam: ${env.BVAKP_ENV}"
                sh "mvn -B clean test -Dsuite.xml=${params.SUITE}"
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'

            // Ekran görüntüleri ve özet rapor (kırılan testleri incelemek için)
            archiveArtifacts allowEmptyArchive: true,
                artifacts: 'reports/kosular/*/ozet.html, reports/kosular/*/kanitlar/**'

            // Allure raporu: build sayfasında "Allure Report" linki olarak görünür.
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                allure commandline: 'allure', includeProperties: false, results: [[path: 'target/allure-results']]
            }

            // Testler kırılsa da rapor maili gitsin; mail gönderilemezse build UNSTABLE (sarı) olur.
            script {
                if (env.MAIL_TO?.trim()) {
                    catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                        sh 'mvn -B exec:java -Dexec.mainClass=com.bvakp.automation.mail.MailRunner -Dexec.classpathScope=test'
                    }
                } else {
                    echo 'MAIL_TO tanımlı değil; rapor maili atlandı.'
                }
            }
        }
    }
}