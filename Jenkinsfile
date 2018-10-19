pipeline {
  agent any
  tools {
    maven 'Maven 3.2.1'
    jdk 'Java 1.8'
  }
  stages {
    stage('Intro') {
      steps {
        sh 'echo Salut camarade !'
      }
    }
    stage ('Initialize') {
      steps {
        sh '''
          echo "PATH = ${PATH}"
          echo "M2_HOME = ${M2_HOME}"
        ''' 
      }
    }
    stage('Build') {
      steps {
        sh 'mvn -s ${USER_SETTINGS_DIR}/act_settings.xml clean compile'
      }
    }
  }
}
