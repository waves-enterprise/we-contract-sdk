dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("com.frimastudio:slf4j-kotlin-extensions")

    api(project(":we-contract-sdk-api"))
    api("com.wavesenterprise:we-node-client-blocking-client")
    implementation("ch.qos.logback:logback-classic")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("io.mockk:mockk")
}
