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
    implementation("com.gradleup.shadow:shadow-gradle-plugin:8.3.5")
//    implementation("org.bytedeco:gradle-javacpp:${libs.plugins.javacpp.get().version}")
//    implementation("org.openjfx:javafx-plugin:${libs.plugins.javafx.get().version}")
//    implementation("com.github.jk1:gradle-license-report:${libs.plugins.license.report.get().version}")
}
