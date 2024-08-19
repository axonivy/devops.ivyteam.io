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

    stage('test') {
      agent {
        dockerfile {
          filename 'Dockerfile.maven'
        }
        reuseNode true
      }

      steps {
        script {
          maven cmd: "clean install -Pproduction"
        }
        junit testDataPublishers: [[$class: 'StabilityTestDataPublisher']], testResults: '**/target/surefire-reports/**/*.xml'
        recordIssues tools: [eclipse()], qualityGates: [[threshold: 1, type: 'TOTAL']]
        recordIssues tools: [mavenConsole()]
      }
    }

    stage('deploy') {
      agent {
        reuseNode true
      }

      steps {
        script {
          def image = docker.build("ivyteam-devops:latest", ".")
          if (env.BRANCH_NAME == 'master') {
            docker.withRegistry('https://docker-registry.ivyteam.io', 'docker-registry.ivyteam.io') {            
              image.push()
            }
          }
        }
      }
    }
  }
}
