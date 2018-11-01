pipeline {
  agent any

  tools {
    maven 'Maven 3.5.0'
    jdk 'Java 1.8'
  }

  parameters {
    booleanParam (
        name: 'Must_release',
        description: 'Unchecked this option for a normal build. Check this option for a \'mvn release\' operation.',
        defaultValue: false
    );
    booleanParam (
        name: 'Dry_run_only',
        description: 'Dry run only? Note: this option is relevant only if option \'Must_release\' is checked.',
        defaultValue: true
    );
    string (
        name: 'Release_version',
        description: 'Version to release now. Note: this option is relevant only if option \'Must_release\' is checked.',
        defaultValue: '<YOUR CURRENT POM VERSION, WITHOUT THE -SNAPSHOT EXTENSION>'
    );
    string (
        name: 'Development_version',
        description: 'Next snapshot version. Note: this option is relevant only if option \'Must_release\' is checked.',
        defaultValue: '<YOUR CURRENT POM VERSION, INCREMENTED, WITH THE -SNAPSHOT EXTENSION>'
    );
  }

  stages {

    stage('Initialize') {
      steps {
        echo "PATH = ${PATH}"
        echo "M2_HOME = ${M2_HOME}"
        echo "parameter Must_release = ${params.Must_release}"
        echo "parameter Dry_run_only = ${params.Dry_run_only}"
        echo "parameter Release_version = ${params.Release_version}"
        echo "parameter Development_version = ${params.Development_version}"
      }
    }

    stage('Build') {
      steps {
        // on a clean local Maven repository, 'mvn clean install' or 'mvn clean deploy' do
        // not work, due to the install-file instructions in the POM. Rather, the clean phase
        // must be invoked separately
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
        script {
          try {
            def policyEvaluation = nexusPolicyEvaluation failBuildOnNetworkError: false, iqApplication: 'Sedex_Message_Handler', iqStage: 'build', jobCredentialsId: ''
          } catch (error) {
              def policyEvaluation = error.policyEvaluation
              throw error
          }
        }
      }
    }

    stage ('Maven release') {
      when {
        expression {
          return params.Must_release
        }
      }
      steps {
        sh "mvn -B -Dresume=false \
                -DdryRun=${params.Dry_run_only} \
                -DreleaseVersion=${params.Release_version} \
                -DdevelopmentVersion=${params.Development_version} \
                release:prepare release:perform"
      }
    }

  }
}
