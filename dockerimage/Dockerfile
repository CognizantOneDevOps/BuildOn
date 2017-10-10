FROM ubuntu:16.04
MAINTAINER BuildOnTeam 

# Jenkins_volume
VOLUME /var/jenkins_home

# Update & Install misc tools
RUN apt-get update
RUN apt-get install -y wget curl zip tree && apt-get install -y software-properties-common

# DEBUG Tools
RUN apt-get install -y vim elinks
# Install a basic SSH server
RUN apt-get install -y openssh-server
RUN sed -i 's|session required pam_loginuid.so|session optional pam_loginuid.so|g' /etc/pam.d/sshd
RUN mkdir -p /var/run/sshd

# Use tini as subreaper in Docker container to adopt zombie processes
COPY tini-static  /bin/tini
RUN  chmod +x /bin/tini  && echo "$TINI_SHA /bin/tini"

######################################################## BUILD TOOLS #########################################################
# GIT
#####
RUN apt-get install -y git

# JAVA
############
RUN \
  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
  add-apt-repository -y ppa:webupd8team/java && \
  apt-get update && \
  apt-get install -y oracle-java8-installer && \
  rm -rf /var/lib/apt/lists/* && \
  rm -rf /var/cache/oracle-jdk8-installer

# Define JAVA_HOME
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

# MAVEN
#######
ENV MAVEN_VERSION 3.3.9
RUN mkdir -p /usr/share/maven \
  && curl -fsSL http://archive.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz \
    | tar -xzC /usr/share/maven --strip-components=1 \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

# Define MAVEN_HOME
ENV MAVEN_HOME /usr/share/maven

# Copy Maven settings
RUN rm -rf /usr/share/maven/conf/settings.xml
COPY settings.xml /usr/share/maven/conf/settings.xml

########################################################### JENKINS  ###########################################################
ENV JENKINS_VERSION 2.60.1
##########################
RUN curl -fsSL http://repo.jenkins-ci.org/public/org/jenkins-ci/main/jenkins-war/${JENKINS_VERSION}/jenkins-war-${JENKINS_VERSION}.war -o /opt/jenkins.war
RUN chmod 644 /opt/jenkins.war

# Jenkins Variables
ENV JENKINS_HOME /var/jenkins
ENV JENKINS_PLUGINS_LOCAL $JENKINS_HOME/plugins
ENV JENKINS_UC https://updates.jenkins.io/
ENV COPY_REFERENCE_FILE_LOG $JENKINS_HOME/copy_reference_file.log
ENV JAVA_OPTS="-Xmx8192m"
# Create Directories
RUN mkdir -p /usr/share/jenkins/ref/
RUN mkdir -p $JENKINS_HOME
RUN mkdir -p $JENKINS_HOME/jobs
RUN mkdir -p $JENKINS_HOME/jobs/jenkinsfileJob
RUN mkdir -p $JENKINS_PLUGINS_LOCAL
RUN touch $JENKINS_HOME/copy_reference_file.log

##Pipeline job config.xml template
COPY config.xml $JENKINS_HOME/jobs/jenkinsfileJob/config.xml
# `/usr/share/jenkins/ref/` contains all reference configuration we want
# to set on a fresh new installation. Use it to bundle additional plugins
# or config file with your custom jenkins Docker image
RUN mkdir -p /usr/share/jenkins/ref/init.groovy.d

# Copy initialize script for Jenkins
COPY jenkins.sh /usr/local/bin/jenkins.sh
RUN chmod +x /usr/local/bin/jenkins.sh
COPY scripts/executors.groovy $JENKINS_HOME/init.groovy.d/1executors.groovy
COPY scripts/simple_user.groovy $JENKINS_HOME/init.groovy.d/2admin_user.groovy

# Plugins script add
COPY install-plugins.sh /usr/local/bin/install-plugins.sh
COPY jenkins-support /usr/local/bin/jenkins-support
COPY plugins.txt /usr/local/bin/plugins.txt
RUN chmod +x /usr/local/bin/install-plugins.sh

# Tini as the entry point to manage zombie processes
ENTRYPOINT ["/bin/tini", "--", "/usr/local/bin/jenkins.sh"]

# Expose Ports for web access and slave agents
EXPOSE 8080
EXPOSE 22
EXPOSE 5000

# To install the plugins
RUN /usr/local/bin/install-plugins.sh < /usr/local/bin/plugins.txt
