plugins {
    application
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val weContractSdkVersion = "1.1.0"

val jacksonVersion = "2.13.2"
val junitVersion = "5.8.2"
val apacheCommonsCodecVersion = "1.15"

dependencies {
    implementation("com.wavesenterprise:we-contract-sdk-grpc:$weContractSdkVersion")

    implementation("com.wavesenterprise:we-contract-sdk-test:$weContractSdkVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    // https://mvnrepository.com/artifact/commons-codec/commons-codec
    implementation("commons-codec:commons-codec:$apacheCommonsCodecVersion")


    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
        )
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes["Main-Class"] = "my.sample.java8.contract.rockps.MainDispatch"
    }
}

project.setProperty("mainClassName", "my.sample.java8.contract.rockps.MainDispatch")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}