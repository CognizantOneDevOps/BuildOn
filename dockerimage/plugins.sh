#! /bin/bash

set -e

REF=/var/jenkins/plugins
mkdir -p $REF

while read spec || [ -n "$spec" ]; do
    plugin=(${spec//:/ });
    [[ ${plugin[0]} =~ ^# ]] && continue
    [[ ${plugin[0]} =~ ^\s*$ ]] && continue
    [[ -z ${plugin[1]} ]] && plugin[1]="latest"
    echo "Downloading ${plugin[0]}:${plugin[1]}"

    if [ -z "$JENKINS_UC_DOWNLOAD" ]; then
      JENKINS_UC_DOWNLOAD=$JENKINS_UC/download
    fi
    curl -sSL  ${JENKINS_UC_DOWNLOAD}/plugins/${plugin[0]}/${plugin[1]}/${plugin[0]}.hpi -o $REF/${plugin[0]}.hpi
#    unzip -qqt $REF/${plugin[0]}.jpi
done  < $1
