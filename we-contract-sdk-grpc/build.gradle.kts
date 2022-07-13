dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("io.grpc:grpc-core")
    implementation("ch.qos.logback:logback-classic")

    api(project(":we-contract-sdk-api"))
    api(project(":we-contract-sdk-core"))
    api(project(":we-contract-sdk-jackson"))
    api("com.wavesenterprise:we-node-client-grpc-blocking-client")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("io.mockk:mockk")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}
