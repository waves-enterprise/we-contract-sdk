plugins {
    application
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val weContractSdkVersion: String by project
val jacksonVersion: String by project
val junitVersion: String by project
val apacheCommonsCodecVersion: String by project

dependencies {
    implementation("com.wavesenterprise:we-contract-sdk-grpc:$weContractSdkVersion")
    implementation("com.wavesenterprise:we-contract-sdk-test:$weContractSdkVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
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
        attributes["Main-Class"] = "my.sample.java17.contract.rockps.MainDispatch"
    }
}

project.setProperty("mainClassName", "my.sample.java17.contract.rockps.MainDispatch")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}