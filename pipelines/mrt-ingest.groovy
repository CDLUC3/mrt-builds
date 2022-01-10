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
                  sh "mvn -Dmaven.repo.local=$HOME/.m2-refactor clean install -DskipTests"
                }
            }
        }
        stage('Build CDL ZK') {
            steps {
                dir('cdl-zk-queue') {
                  git branch: 'java-refactor', url: 'https://github.com/CDLUC3/cdl-zk-queue.git'
                  sh "mvn -Dmaven.repo.local=$HOME/.m2-refactor clean install -DskipTests"
                }
            }
        }
        stage('Obsolete dependencies') {
            steps {
                sh "mvn -Dmaven.repo.local=$HOME/.m2-refactor dependency:get -Dmaven.legacyLocalRepo=true -DgroupId=jargs -DartifactId=jargs -Dversion=1.1.1 -Dpackaging=jar -DrepoUrl=https://mvn.cdlib.org/content/repositories/thirdparty/"
                sh "mvn -Dmaven.repo.local=$HOME/.m2-refactor dependency:get -Dmaven.legacyLocalRepo=true -DgroupId=org.cdlib.mrt -DartifactId=mrt-dataonesrc -Dversion=1.0-SNAPSHOT -Dpackaging=jar -DrepoUrl=https://mvn.cdlib.org/content/repositories/cdl-snapshots/"
            }
        }
        stage('Build Ingest') {
            steps {
                dir('mrt-ingest'){
                  git branch: 'java-refactor', url: 'https://github.com/CDLUC3/mrt-ingest.git'
                  sh "mvn -Dmaven.repo.local=$HOME/.m2-refactor clean install -Denforcer.skip=true"
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'mrt-ingest/ingest-war/target/mrt-ingestwar-1.0-SNAPSHOT.war,mrt-ingest/ingest-war/target/mrt-ingestwar-1.0-SNAPSHOT-archive.zip'

                }
            }
        }
    }
}
