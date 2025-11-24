#FROM jenkins/jenkins:lts-jdk17
#
#USER root
#RUN apt-get update && \
#    apt-get install -y maven nodejs npm git && \
#    rm -rf /var/lib/apt/lists/*
#
#USER jenkins


FROM jenkins/jenkins:lts-jdk17

USER root

# Install Maven + Docker CLI + Git
RUN apt-get update && \
    apt-get install -y \
        maven \
        git \
        docker.io && \
    rm -rf /var/lib/apt/lists/*

# Allow Jenkins to use Docker socket
RUN usermod -aG docker jenkins

USER jenkins


