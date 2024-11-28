plugins {
    `java-gradle-plugin`
    kotlin("jvm")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

gradlePlugin {
    // Define the plugin
    val settingsPlugin by plugins.creating {
        id = "io.github.qupath.qupath-extension-settings"
        implementationClass = "qupath.gradle.QuPathGradleSettings"
    }
}
