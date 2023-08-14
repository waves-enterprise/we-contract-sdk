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
        create<MavenPublication>("we-node-client-bom") {
            from(components["javaPlatform"])
        }
    }
}
