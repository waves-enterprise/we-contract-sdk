import io.gitlab.arturbosch.detekt.Detekt
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion: String by project
val kotlinCoroutinesVersion: String by project
val reactorVersion: String by project
val springBootVersion: String by project
val springCloudVersion: String by project
val jacocoToolVersion: String by project
val logbackVersion: String by project
val sl4jExtVersion: String by project
val javaxAnnotationApiVersion: String by project

val ioGrpcVersion: String by project
val ioGrpcKotlinVersion: String by project
val protobufVersion: String by project

val junitPlatformLauncherVersion: String by project
val mockkVersion: String by project
val springMockkVersion: String by project

val ktorVersion: String by project

val weNodeClientVersion: String by project

val weMavenUser: String? by project
val weMavenPassword: String? by project

val weMavenBasePath = "https://artifacts.wavesenterprise.com/repository/"
val sonaTypeBasePath = "https://s01.oss.sonatype.org"

plugins {
    kotlin("jvm") apply false
    `maven-publish`
    kotlin("plugin.spring") apply false
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management") apply false
    id("io.gitlab.arturbosch.detekt") apply false
    id("org.jlleitschuh.gradle.ktlint") apply false
    id("com.palantir.git-version") apply false
    id("com.gorylenko.gradle-git-properties") apply false
    id("fr.brouillard.oss.gradle.jgitver")
    id("jacoco")
}

jgitver {
    strategy = fr.brouillard.oss.jgitver.Strategies.PATTERN
    versionPattern =
        "\${M}.\${m}.\${meta.COMMIT_DISTANCE}-\${meta.GIT_SHA1_8}\${-~meta.QUALIFIED_BRANCH_NAME}-SNAPSHOT"
    nonQualifierBranches = "master,dev,main"
}

allprojects {
    group = "com.wavesenterprise"
    version = "-" // set by jgitver

    repositories {
        mavenCentral()
        maven {
            name = "we-snapshots"
            url = uri("https://artifacts.wavesenterprise.com/repository/maven-snapshots/")
            mavenContent {
                snapshotsOnly()
            }
            credentials {
                username = weMavenUser
                password = weMavenPassword
            }
        }

    }
}

subprojects {
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "kotlin")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "jacoco")
    apply(plugin = "maven-publish")

    val jacocoCoverageFile = "$buildDir/jacocoReports/test/jacocoTestReport.xml"

    tasks.withType<JacocoReport> {
        reports {
            xml.apply {
                required.set(true)
                outputLocation.set(file(jacocoCoverageFile))
            }
        }
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
        finalizedBy("jacocoTestReport")
    }

    val detektConfigFilePath = "$rootDir/gradle/detekt-config.yml"

    tasks.withType<Detekt> {
        exclude("resources/")
        exclude("build/")
        config.setFrom(detektConfigFilePath)
        buildUponDefaultConfig = true
    }

    val sourcesJar by tasks.creating(Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles sources JAR"
        archiveClassifier.set("sources")
        from(project.the<SourceSetContainer>()["main"].allSource)
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                versionMapping {
                    allVariants {
                        fromResolutionResult()
                    }
                }
                afterEvaluate {
                    artifact(sourcesJar)
                }
            }
        }
    }

    // todo bom without spring boot
    the<DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
            mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion") {
                bomProperty("kotlin.version", kotlinVersion)
            }
        }
        dependencies {
            dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$kotlinCoroutinesVersion")
            dependency("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinCoroutinesVersion")
            dependency("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:$kotlinCoroutinesVersion")

            dependency("javax.annotation:javax.annotation-api:$javaxAnnotationApiVersion")

            dependency("io.projectreactor:reactor-core:$reactorVersion")

            dependency("com.google.protobuf:protobuf-java:$protobufVersion")
            dependency("io.grpc:grpc-core:$ioGrpcVersion")
            dependency("io.grpc:grpc-stub:$ioGrpcVersion")
            dependency("io.grpc:grpc-netty:$ioGrpcVersion")
            dependency("io.grpc:grpc-protobuf:$ioGrpcVersion")

            dependency("com.google.protobuf:protobuf-kotlin:$protobufVersion")
            dependency("io.grpc:grpc-kotlin-stub:$ioGrpcKotlinVersion")

            dependency("ch.qos.logback:logback-classic:$logbackVersion")

            dependency("io.ktor:ktor-client-core:$ktorVersion")
            dependency("io.ktor:ktor-client-cio:$ktorVersion")
            dependency("io.ktor:ktor-client-logging:$ktorVersion")
            dependency("io.ktor:ktor-client-content-negotiation:$ktorVersion")
            dependency("io.ktor:ktor-serialization-jackson:$ktorVersion")

            dependency("org.junit.platform:junit-platform-launcher:$junitPlatformLauncherVersion")
            dependency("io.mockk:mockk:$mockkVersion")
            dependency("com.ninja-squad:springmockk:$springMockkVersion")
            dependency("com.frimastudio:slf4j-kotlin-extensions:$sl4jExtVersion")

            dependency("com.wavesenterprise:we-node-client-domain:$weNodeClientVersion")
            dependency("com.wavesenterprise:we-node-client-blocking-client:$weNodeClientVersion")
            dependency("com.wavesenterprise:we-node-client-grpc-blocking-client:$weNodeClientVersion")
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            freeCompilerArgs = listOf(
                "-Xjsr305=strict",
                "-Xjvm-default=all", // todo move to api only
            )
            jvmTarget = JavaVersion.VERSION_1_8.toString()
            targetCompatibility = JavaVersion.VERSION_1_8.toString()
        }
    }

    jacoco {
        toolVersion = jacocoToolVersion
        reportsDirectory.set(file("$buildDir/jacocoReports"))
    }
}
