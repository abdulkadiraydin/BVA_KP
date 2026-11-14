pipeline {
    agent any

    parameters {
        // Listedeki ilk değer varsayılandır (zamanlanmış koşular da bunu kullanır).
        choice(name: 'SUITE', choices: ['smoke.xml', 'testng.xml'], description: 'Koşturulacak TestNG paketi')
    }

    tools {
        maven 'Maven'
    }

    options {
        // Aynı anda tek koşu: test kullanıcıları ve temizlik çakışmasın
        disableConcurrentBuilds()
    }

    environment {
        // Maven çıktısı da UTF-8 olsun; yoksa Jenkins konsolunda Türkçe karakterler bozulur
        MAVEN_OPTS                         = '-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8'

        // Jenkins'e özel ayarlar
        BVAKP_HEADLESS                     = 'true'
        BVAKP_ALLURE_REPORT_AUTO_GENERATE  = 'false'
        BVAKP_TEST_RETRY_COUNT             = '1'
        BVAKP_CLEANUP_ENABLED              = 'true'
        // Aynı test üst üste kırılırsa alarm maili (günde en fazla bir kez)
        BVAKP_FAILURE_TRACKING_ENABLED     = 'true'

        // Parolalar (Jenkins Credentials'tan gelir)
        BVAKP_PASSWORD                     = credentials('bvakp-password')
        BVAKP_PASSWORD_HAZIRLAYAN          = credentials('bvakp-password-hazirlayan')
        BVAKP_PASSWORD_KVKK                = credentials('bvakp-password-kvkk')
        BVAKP_PASSWORD_YAYIN               = credentials('bvakp-password-yayin')
        BVAKP_PASSWORD_TEMIZLIK            = credentials('bvakp-password-temizlik')
        BVAKP_PORTAL_PASSWORD_TALEP        = credentials('bvakp-portal-password-talep')
        BVAKP_PORTAL_PASSWORD_ONAY         = credentials('bvakp-portal-password-onay')

        // Rapor maili: sunucu ve adresler Jenkins'in genel ortam değişkenlerinden
        // (Jenkins'i Yönet → Sistem → Global özellikler), parola Credentials'tan gelir.
        MAIL_PASSWORD                      = credentials('bvakp-mail-password')
    }

    stages {
        stage('Testleri çalıştır') {
            steps {
                echo "Suite: ${params.SUITE}"
                // chcp 65001: Windows konsolunu UTF-8 yapar; Türkçe karakterler Jenkins'te bozulmaz
                bat "chcp 65001 > nul && mvn clean test \"-Dsuite.xml=${params.SUITE}\""
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'

            // Allure raporu: build sayfasında "Allure Report" linki olarak görünür.
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                allure includeProperties: false, results: [[path: 'target/allure-results']]
            }

            // Testler kırılsa da rapor maili gitsin; mail gönderilemezse build UNSTABLE (sarı) olur.
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                bat 'chcp 65001 > nul && mvn exec:java "-Dexec.mainClass=com.bvakp.automation.mail.MailRunner" "-Dexec.classpathScope=test"'
            }
        }
    }
}