pipeline {
    agent any

    tools {
        // Install the Maven version configured as "M3" and add it to the path.
        maven 'maven384'
    }

    stages {
        stage('Purge Local') {
            steps {
                sh "rm -rf ~/.m2-refactor"
            }
        }
        stage('Build Core') {
            steps {
                dir('mrt-core2') {
                  git branch: 'java-refactor', url: 'https://github.com/CDLUC3/mrt-core2.git'
                  sh "mvn -Dmaven.repo.local=$HOME/.m2-refactor clean install -DskipTests "
                }
            }
        }
        stage('Build Cloud') {
            steps {
                dir('mrt-cloud') {
                  git branch: 'java-refactor', url: 'https://github.com/CDLUC3/mrt-cloud.git'
                  sh "mvn -Dmaven.repo.local=$HOME/.m2-refactor clean install -DskipTests "
                }
            }
        }
        stage('Build CDL ZK') {
            steps {
                dir('cdl-zk-queue') {
                  git branch: 'java-refactor', url: 'https://github.com/CDLUC3/cdl-zk-queue.git'
                  sh "mvn -Dmaven.repo.local=$HOME/.m2-refactor clean install -DskipTests "
                }
            }
        }
        stage('Build Store') {
            steps {
                dir('mrt-store'){
                  git branch: 'java-refactor', url: 'https://github.com/CDLUC3/mrt-store.git'
                  sh "mvn -Dmaven.repo.local=$HOME/.m2-refactor clean install"
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'mrt-store/store-war/target/mrt-storewar-1.0-SNAPSHOT.war'
                }
            }
        }
    }
}
