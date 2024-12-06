plugins {
    `java-library`
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
}

base {
    group = "io.github.qupath"
    version = "0.2.1"
    description = "Gradle plugin for developing QuPath extensions"
}

java {
    withSourcesJar()
    withJavadocJar()

    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.bytedeco:gradle-javacpp:1.5.10")
    implementation("org.openjfx:javafx-plugin:0.1.0")
}

gradlePlugin {
    val settingsPlugin by plugins.creating {
        id = "io.github.qupath.qupath-extension-settings"
        implementationClass = "qupath.gradle.QuPathGradleSettings"
        group = "io.github.qupath"
    }
}


publishing {
    repositories {
        maven {
            name = "SciJava"
            val releasesRepoUrl = uri("https://maven.scijava.org/content/repositories/releases")
            val snapshotsRepoUrl = uri("https://maven.scijava.org/content/repositories/snapshots")
            // Use gradle -Prelease publish
            url = if (project.hasProperty("release")) releasesRepoUrl else snapshotsRepoUrl
            credentials {
                username = System.getenv("MAVEN_USER")
                password = System.getenv("MAVEN_PASS")
            }
        }
    }

    publications {
        create<MavenPublication>("pluginMaven") {
            pom {
                licenses {
                    license {
                        name = "Apache License v2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0"
                    }
                }
            }
        }
    }
}
