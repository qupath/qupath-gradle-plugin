plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
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
    }
}
