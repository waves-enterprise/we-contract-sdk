plugins {
    `maven-publish`
    `java-platform`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    constraints {
        project.rootProject.subprojects.forEach {
            api(it)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("we-contract-sdk-bom") {
            from(components["javaPlatform"])
        }
    }
}
