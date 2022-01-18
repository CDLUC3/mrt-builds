import groovy.json.JsonSlurper
pipeline {
    /*
     * build.last.txt - contains the commit hashes used for the last build that was run
     * rev.last.txt - contains the semantic version for the prior build
     * build.current.txt - contains commit hashes used in this current build
     * rev.current.txt - calculated semantic version for this build 
     */
    environment {
      REV_MAJOR = 1 //manually increment for NON-backwards compatible changes
      REV_MINOR = 0 //manually increment for backwards compatible changes
      REV_PATCH = 0 //build will compute
      
      //Branch/tag names to incorporate into the build.  Create one var for each repo.
      BRANCH_BUILDS = 'main'

      //working vars
      major = ''
      minor = ''
      patch = ''
    }
    agent any

    tools {
        // Install the Maven version configured as "M3" and add it to the path.
        maven 'maven384'
    }
    
    stages {
        stage('Last build') { // for display purposes
            steps {
                //sh "env"
                sh "rm -f build.current.txt rev.current.txt"
                script {
                  try {
                    sh "curl -s -S -f -o build.last.txt ${JENKINS_URL}view/Development/job/Terry4/lastSuccessfulBuild/artifact/build.last.txt || touch build.last.txt"
                  } finally {
                  }
                  try {
                    sh "curl -s -S -f -o rev.last.txt ${JENKINS_URL}view/Development/job/Terry4/lastSuccessfulBuild/artifact/rev.last.txt || echo '${REV_MAJOR} ${REV_MINOR} ${REV_PATCH}' > rev.last.txt"
                  } finally {
                  }
                  major = sh(script: "cut -d' ' -f1 rev.last.txt", returnStdout: true).toString().trim()        
                  minor = sh(script: "cut -d' ' -f2 rev.last.txt", returnStdout: true).toString().trim()        
                  patch = sh(script: "cut -d' ' -f3 rev.last.txt", returnStdout: true).toString().trim()        

                  if (major == '') {
                      major = REV_MAJOR
                  }
                  if (minor == '') {
                      minor = REV_MINOR
                  }
                  if (patch == '') {
                      patch = REV_PATCH
                  }
                }
            }
        }
                  
        stage('Current commits') { // for display purposes
            steps {
                script {
                  // Get some code from a GitHub repository
                  git branch: "${env.BRANCH_BUILDS}", url: 'https://github.com/CDLUC3/mrt-builds.git'
                  sh "git remote get-url origin >> build.current.txt"
                  sh "git rev-parse HEAD >> build.current.txt"

                  if (major < REV_MAJOR || minor < REV_MINOR) {
                      major = REV_MAJOR
                      minor = REV_MINOR
                      patch = 0
                  }
                }
            }
        }

        stage('Compute semantic ver') { // for display purposes
            steps {
                script {
                  try {
                    sh "diff build.last.txt build.current.txt"
                  } catch (err) {
                    sh "echo 'diff found'"
                    patch = patch.toInteger() + 1
                  } 
                  sh "echo ${major} ${minor} ${patch} > rev.current.txt"
                  sh "touch sample-${major}.${minor}.${patch}.war"
                }
            }
        }

        stage('Archive Resources') { // for display purposes
            steps {
                script {
                  sh "mv build.current.txt build.last.txt"
                  sh "mv rev.current.txt rev.last.txt"
                  archiveArtifacts artifacts: "build.last.txt, rev.last.txt, sample-${major}.${minor}.${patch}.war"
                }
 
            }
        }
    }
}
