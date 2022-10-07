val weMavenUser: String by project
val weMavenPassword: String by project

allprojects {
    group = "com.wavesenterprise.sdk.samples"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenLocal()
        mavenCentral()
        if (weMavenUser != null && weMavenPassword != null) {
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
}
