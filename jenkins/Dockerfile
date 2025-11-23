FROM jenkins/jenkins:lts-jdk17

USER root
RUN apt-get update && \
    apt-get install -y maven nodejs npm git && \
    rm -rf /var/lib/apt/lists/*

USER jenkins
