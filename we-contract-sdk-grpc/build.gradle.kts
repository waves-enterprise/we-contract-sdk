dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("io.grpc:grpc-core")
    implementation("ch.qos.logback:logback-classic")

    api(project(":we-contract-sdk-api"))
    api(project(":we-contract-sdk-core"))
    api("com.wavesenterprise:we-node-client-grpc-blocking-client")

    api("com.fasterxml.jackson.core:jackson-core")
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("io.mockk:mockk")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}
