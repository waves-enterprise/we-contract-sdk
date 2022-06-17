plugins {
    application
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val weContractSdkVersion = "0.0.11-75cbce0f-feature_wtch_41-SNAPSHOT"

dependencies {
    implementation("com.wavesenterprise:we-contract-sdk-grpc:$weContractSdkVersion")

}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes["Main-Class"] = "my.sample.java8.contract.MainDispatch"
    }
}

project.setProperty("mainClassName", "my.sample.java8.contract.MainDispatch")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}