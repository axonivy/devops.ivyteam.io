pipeline {
  agent any
  
  triggers {
    cron 'H 22 * * *'
  }
  
  options {
    buildDiscarder(logRotator(numToKeepStr: '30', artifactNumToKeepStr: '3'))
  }

  stages {
    stage('editorconfig') {
      steps {
        script {
          docker.image('mstruebing/editorconfig-checker').inside {
            sh 'ec -no-color'
          }
        }
      }
    }

    stage('build') {
      steps {
        script {
          docker.build('maven-build', '-f Dockerfile.maven .').inside {
            maven cmd: "clean verify -Pit,production"

            if (env.BRANCH_NAME == 'master') {
              maven cmd: "org.cyclonedx:cyclonedx-maven-plugin:makeAggregateBom -DincludeLicenseText=true -DoutputFormat=json"
              withCredentials([string(credentialsId: 'dependency-track', variable: 'API_KEY')]) {
                sh 'curl -v --fail -X POST https://api.dependency-track.ivyteam.io/api/v1/bom \
                      -H "Content-Type: multipart/form-data" \
                      -H "X-API-Key: ' + API_KEY + '" \
                      -F "autoCreate=true" \
                      -F "projectName=devops.ivyteam.io" \
                      -F "projectVersion=master" \
                      -F "bom=@target/bom.json"'
              }
            }
          }

          def image = docker.build("devops:latest", ".")
          if (env.BRANCH_NAME == 'master') {
            docker.withRegistry('https://docker-registry.ivyteam.io', 'docker-registry.ivyteam.io') {            
              image.push()
            }
          }
        }

        junit testDataPublishers: [[$class: 'StabilityTestDataPublisher']], testResults: '**/target/*-reports/**/*.xml'
        recordIssues tools: [eclipse()], qualityGates: [[threshold: 1, type: 'TOTAL']]
        recordIssues tools: [mavenConsole()]
      }
    }
  }
}
