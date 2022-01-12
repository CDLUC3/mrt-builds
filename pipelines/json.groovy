import groovy.json.JsonSlurper
pipeline {
    agent any

    tools {
        // Install the Maven version configured as "M3" and add it to the path.
        maven 'maven384'
    }
    
    stages {
        stage('Preparation') { // for display purposes
            steps {
                script {
                    println 'hello'
                    println "${env.WORKSPACE}/test.json"
                    println readFile("${env.WORKSPACE}/test.json")
                    def txt = readFile("${env.WORKSPACE}/test.json")
                    def jsonx = new JsonSlurper().parseText(txt)
                    println jsonx
                }
            }
        }
    }
}
