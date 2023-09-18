val gitHubProject: String by project
val githubUrl: String by project

plugins {
    signing
    `maven-publish`
    `java-platform`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    constraints {
        project.rootProject.subprojects.forEach { project ->
            api(project)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("projectBom") {
            from(components["javaPlatform"])

            pom {
                packaging = "pom"
                name.set(project.name)
                url.set(githubUrl + gitHubProject)
                description.set("WE Contract SDK BOM")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                scm {
                    connection.set("scm:$githubUrl$gitHubProject")
                    developerConnection.set("scm:git@github.com:$gitHubProject.git")
                    url.set(githubUrl + gitHubProject)
                }

                developers {
                    developer {
                        id.set("kt3")
                        name.set("Stepan Kashintsev")
                        email.set("kpote3@gmail.com")
                    }
                    developer {
                        id.set("donyfutura")
                        name.set("Daniil Georgiev")
                        email.set("donyfutura@gmail.com")
                    }
                }
            }
        }
    }
}

signing {
    afterEvaluate {
        if (!project.version.toString().endsWith("SNAPSHOT")) {
            sign(publishing.publications["projectBom"])
        }
    }
}
