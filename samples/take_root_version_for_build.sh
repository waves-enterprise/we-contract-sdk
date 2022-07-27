#!/bin/bash

version="$(cut -d ' ' -f 5 <<< $(./gradlew version))"
echo $version

cd samples
./gradlew clean check -PweContractSdkVersion="$version"
