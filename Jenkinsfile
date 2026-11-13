pipeline {
    agent any

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
    }

    stages {
        stage('Testleri çalıştır') {
            steps {
                bat 'mvn clean test'
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
        }
    }
}