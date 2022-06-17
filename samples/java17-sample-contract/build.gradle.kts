plugins {
    application
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val weContractSdkVersion = "1.0.0"

dependencies {

    implementation("com.wavesenterprise:we-contract-sdk-grpc:$weContractSdkVersion")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes["Main-Class"] = "my.sample.java17.contract.MainDispatch"
    }
}

project.setProperty("mainClassName", "my.sample.java17.contract.MainDispatch")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}