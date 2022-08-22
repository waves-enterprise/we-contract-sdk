dependencies {
    implementation(kotlin("stdlib"))

    api(project(":we-contract-sdk-core"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}
