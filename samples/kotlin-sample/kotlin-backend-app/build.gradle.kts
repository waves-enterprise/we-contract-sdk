import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("org.jetbrains.kotlin.plugin.spring") version "1.7.0"
    kotlin("jvm") version "1.7.0"
    application
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val weContractSdkVersion: String by project
val jacksonVersion: String by project
val junitVersion: String by project
val apacheCommonsCodecVersion: String by project

dependencies {
    api(project(":kotlin-sample:kotlin-contract"))
    implementation("com.wavesenterprise:we-contract-sdk-blocking-client:1.1.20-c0aee00c-feature_wtch_97-SNAPSHOT") // TODO: change after merge sdk in dev
    implementation("com.wavesenterprise:we-node-client-grpc-blocking-client:0.3.1")
    implementation("com.wavesenterprise:we-node-client-feign-client:0.3.1")
    implementation("com.wavesenterprise:we-tx-signer-node:0.3.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("commons-codec:commons-codec:$apacheCommonsCodecVersion")
    implementation("org.springframework.boot:spring-boot:2.2.3.RELEASE")
    implementation("org.springframework.boot:spring-boot-starter-web:2.2.3.RELEASE")
    implementation("io.github.openfeign:feign-core:11.9.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED
        )
        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}
