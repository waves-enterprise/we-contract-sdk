#!/bin/bash

version="$(cut -d ' ' -f 5 <<< $(./gradlew version))"

cd samples
./gradlew clean check -PweContractSdkVersion="$version"
