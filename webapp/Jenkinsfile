env.dockerimagename="devopsbasservice/buildonframework:buildon-jenkinsfile"
node {
   stage ('BuildOn_Build') {
    checkout scm
    sh 'mvn clean package -DskipTests=True'
  } 
 //Test Buildon  Test
}
