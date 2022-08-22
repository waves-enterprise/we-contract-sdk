
dependencies {
    implementation(kotlin("stdlib"))

    api(project(":we-contract-sdk-api"))
    api(project(":we-contract-sdk-client:we-contract-sdk-client-local-validator"))
    api("com.wavesenterprise:we-node-client-domain")
    api("com.wavesenterprise:we-node-client-blocking-client")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("io.mockk:mockk")
    testImplementation(project(":we-contract-sdk-jackson"))
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}
