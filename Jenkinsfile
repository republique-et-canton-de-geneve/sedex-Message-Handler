// see ***REMOVED***/job/sedex-message-handler-2/job/master
pipeline {
  agent any

  tools {
    maven 'Maven 3.2.1'
    jdk 'Java 1.8'
  }

  stages {

    stage('Initialize') {
      steps {
        sh '''
          echo "PATH = ${PATH}"
          echo "M2_HOME = ${M2_HOME}"
        ''' 
      }
    }

    stage('Build') {
      steps {
        // on a clean local Maven repository, 'mvn clean install' or 'mvn clean deploy'
        // do not work, due to the install-file instructions in the POM. Rather, the clean
        // phase must be invoked separately
        sh 'mvn -s ${USER_SETTINGS_DIR}/act_settings.xml -B clean'
        sh 'mvn -s ${USER_SETTINGS_DIR}/act_settings.xml -B -Pwindows-x86-32 deploy'
      }
    }

    stage('SonarQube') {
      steps {
        script {
          withSonarQubeEnv('Sonarqube') {
            sh 'mvn -s ${USER_SETTINGS_DIR}/act_settings.xml verify sonar:sonar'
          }
        }
      }
    }

    stage('NexusIQServer') {
      steps {
        sh '''
          set 
        ''' 
      }
    }
  }
}
