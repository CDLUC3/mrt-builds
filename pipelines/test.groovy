//use vars for hostname
//use vars for dirname
pipeline {
    environment {
      REV_MAJOR = 1 //manually increment for NON-backwards compatible changes
      REV_MINOR = 0 //manually increment for backwards compatible changes
      REV_PATCH = 0 //build will compute
      BRANCH_BUILDS = 'main'
    }
    agent any

    tools {
        // Install the Maven version configured as "M3" and add it to the path.
        maven 'maven384'
    }
    
    stages {
        stage('Compute Prior Semantic Version') { // for display purposes
            steps {
                sh "rm -f build.current.txt rev.current.txt"
                script {
                  //def host = ...
                  try {
                    sh "curl -s -S -f -o build.last.txt http://${host}/view/Development/job/Terry4/lastSuccessfulBuild/artifact/build.last.txt || touch build.last.txt"
                  } finally {
                  }
                  try {
                    sh "curl -s -S -f -o rev.last.txt http://${host}/view/Development/job/Terry4/lastSuccessfulBuild/artifact/rev.last.txt || echo '${REV_MAJOR} ${REV_MINOR} ${REV_PATCH}' > rev.last.txt"
                  } finally {
                  }
                  def major = sh(script: "cut -d' ' -f1 rev.last.txt", returnStdout: true).toString().trim()        
                  def minor = sh(script: "cut -d' ' -f2 rev.last.txt", returnStdout: true).toString().trim()        
                  def patch = sh(script: "cut -d' ' -f3 rev.last.txt", returnStdout: true).toString().trim()        

                }
                //sh "echo ${major}.${minor}.${patch} > rev.current.txt"
                //sh "echo ${REV_MAJOR} ${REV_MINOR} ${REV_PATCH} > rev.current.txt"
                
                // Get some code from a GitHub repository
                git branch: "${env.BRANCH_BUILDS}", url: 'https://github.com/CDLUC3/mrt-builds.git'
                sh "git remote get-url origin >> build.current.txt"
                sh "git rev-parse HEAD >> build.current.txt"

                sh "touch ARTIFACT.TXT"
            }
        }
        stage('Compute New Semantic Version') { // for display purposes
            steps {
                sh "diff build.last.txt build.current.txt >> /dev/null | echo 'bump $patch'"
                //sh "echo ${major} ${minor} ${patch} > rev.current.txt"
                //rename ARTIFACT.TXT with ${major}.${minor}.${patch}
                sh "mv build.current.txt build.last.txt"
                sh "mv rev.current.txt rev.last.txt"
                archiveArtifacts artifacts: 'build.last.txt, rev.last.txt'
            }
        }
    }
}