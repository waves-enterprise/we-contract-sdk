#!/bin/bash

inspectResult=$(./gradlew publishToMavenLocal version | grep 'Version: ')
version=$(awk -F'Version: |",' '{print $2}' <<< "$inspectResult")
echo $version

cd samples
./gradlew clean check -PweContractSdkVersion="$version"
