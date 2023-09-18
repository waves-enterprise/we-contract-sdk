pluginManagement {
    val kotlinVersion: String by settings
    val gradleDependencyManagementVersion: String by settings
    val detektVersion: String by settings
    val ktlintVersion: String by settings
    val gitPropertiesVersion: String by settings
    val palantirGitVersion: String by settings
    val jGitVerVersion: String by settings
    val dokkaVersion: String by settings
    val nexusStagingVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion apply false
        `maven-publish`
        id("io.codearte.nexus-staging") version nexusStagingVersion apply false
        id("io.spring.dependency-management") version gradleDependencyManagementVersion apply false
        id("io.gitlab.arturbosch.detekt") version detektVersion apply false
        id("org.jlleitschuh.gradle.ktlint") version ktlintVersion apply false
        id("com.palantir.git-version") version palantirGitVersion apply false
        id("com.gorylenko.gradle-git-properties") version gitPropertiesVersion apply false
        id("jacoco")
        id("fr.brouillard.oss.gradle.jgitver") version jGitVerVersion
        id("org.jetbrains.dokka") version dokkaVersion
    }

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "we-contract-sdk"

include(
    "we-contract-sdk-bom",

    "we-contract-sdk-api",
    "we-contract-sdk-core",
    "we-contract-sdk-grpc",
    "we-contract-sdk-jackson",
    "we-contract-sdk-test",

    "we-contract-sdk-client:we-contract-sdk-blocking-client", // todo move to other repo
    "we-contract-sdk-client:we-contract-sdk-client-local-validator"
)
