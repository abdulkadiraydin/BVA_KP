pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    environment {
        // Jenkins'e özel ayarlar
        BVAKP_HEADLESS                     = 'true'
        BVAKP_ALLURE_REPORT_AUTO_GENERATE  = 'false'

        // Parolalar (Jenkins Credentials'tan gelir)
        BVAKP_PASSWORD                     = credentials('bvakp-password')
        BVAKP_PASSWORD_HAZIRLAYAN          = credentials('bvakp-password-hazirlayan')
        BVAKP_PASSWORD_KVKK                = credentials('bvakp-password-kvkk')
        BVAKP_PASSWORD_YAYIN               = credentials('bvakp-password-yayin')
        BVAKP_PORTAL_PASSWORD_TALEP        = credentials('bvakp-portal-password-talep')
        BVAKP_PORTAL_PASSWORD_ONAY         = credentials('bvakp-portal-password-onay')

        // Rapor maili: sunucu ve adresler Jenkins'in genel ortam değişkenlerinden
        // (Jenkins'i Yönet → Sistem → Global özellikler), parola Credentials'tan gelir.
        MAIL_PASSWORD                      = credentials('bvakp-mail-password')
    }

    stages {
        stage('Smoke suite hazırla') {
            steps {
                // pom her zaman testng.xml'i çalıştırıyor; Jenkins'in kopyasında smoke.xml ile değiştiriyoruz
                bat 'copy /Y smoke.xml testng.xml'
            }
        }
        stage('Smoke testleri çalıştır') {
            steps {
                bat 'mvn clean test'
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
        }
        unsuccessful {   // build başarılı değilse (kırmızı veya sarı)
            catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                bat 'mvn exec:java "-Dexec.mainClass=com.bvakp.automation.mail.MailRunner" "-Dexec.classpathScope=test"'
            }
        }
    }