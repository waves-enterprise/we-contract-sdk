plugins {
    application
    kotlin("jvm") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.wavesenterprise:we-contract-sdk-grpc:1.0.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
}

application {
    mainClass.set("com.example.demo.contract.MainKt")
}