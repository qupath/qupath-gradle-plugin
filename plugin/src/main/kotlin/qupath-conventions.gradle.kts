import org.gradle.api.attributes.Usage
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.nativeplatform.MachineArchitecture
import org.gradle.nativeplatform.OperatingSystemFamily
import kotlin.jvm.optionals.getOrNull

plugins {
    // Main gradle plugin for building a Java library
    `java-library`
    groovy
    // Support writing the extension in Groovy (remove this if you don't want to)
    // To create a shadow/fat jar that bundle up all dependencies
    id("com.gradleup.shadow")
    // Include this plugin to avoid downloading JavaCPP dependencies for all platforms
    id("org.bytedeco.gradle-javacpp-platform")
    id("org.openjfx.javafxplugin")
}


base {
    archivesName = rootProject.name
    println("ARCHIVE: ${archivesName.get()}")
    println(gradle.extra.properties)
    val requestedVersion = gradle.extra["extension.version"]
    if (requestedVersion is String) {
        version = requestedVersion
    }
    val requestedDescription = gradle.extra["extension.description"]
    if (requestedDescription is String) {
        description = requestedDescription
    }
}


/*
 * Manifest info
 */
tasks.withType<Jar> {
    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to project.name,
                "Implementation-Version" to archiveVersion,
                "Automatic-Module-Name" to gradle.extra["extension.module"]
            )
        )
    }

    /*
    * Avoid 'Entry .gitkeep is a duplicate but no duplicate handling strategy has been set.'
    * when using withSourcesJar()
    */
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

/**
 * Copy necessary attributes, see
 * - https://github.com/qupath/qupath-extension-template/issues/9
 */
configurations.shadow {
    attributes {
        attribute(
            Usage.USAGE_ATTRIBUTE, objects.named(
                Usage::class.java,
            Usage.JAVA_RUNTIME))
        attribute(
            OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE,
            objects.named(OperatingSystemFamily::class.java, "org.gradle.native.operatingSystem"))
        attribute(
            MachineArchitecture.ARCHITECTURE_ATTRIBUTE,
            objects.named(MachineArchitecture::class.java, "org.gradle.native.architecture"))
    }
}


/*
 * Copy the LICENSE file into the jar... if we have one (we should!)
 */
tasks.processResources {
    from("${projectDir}/LICENSE") {
        into("licenses/")
    }
}

/*
 * Define extra 'copyDependencies' task to copy dependencies into the build directory.
 */
tasks.register<Copy>("copyDependencies") {
    description = "Copy dependencies into the build directory for use elsewhere"
    group = "QuPath"

    from(configurations.runtimeClasspath)
    into("build/libs")
}


java {
    withSourcesJar()
    withJavadocJar()
}

javafx {
    if (project.configurations.names.contains("shadow"))
        configuration = "shadow"
    modules = listOf(
        "javafx.base",
        "javafx.controls",
        "javafx.graphics",
        "javafx.media",
        "javafx.fxml",
        "javafx.web",
        "javafx.swing"
    )
}

// Try to get JDK and JavaFX versions from the version catalog
val libs = versionCatalogs.find("libs").getOrNull()
if (libs is VersionCatalog) {
    println("Using version catalog: ${libs.name}")
    val jdkVersion = libs.findVersion("jdk").getOrNull()?.requiredVersion
    if (!jdkVersion.isNullOrEmpty()) {
        java {
            toolchain {
                languageVersion = JavaLanguageVersion.of(libs.findVersion("jdk").get().requiredVersion)
            }
        }
    }
    val jfxVersion = libs.findVersion("javafx").getOrNull()?.requiredVersion
    if (!jfxVersion.isNullOrEmpty()) {
        javafx {
            version = jfxVersion
        }
    }
}

/*
 * Create javadocs for all modules/packages in one place.
 * Use -PstrictJavadoc=true to fail on error with doclint (which is rather strict).
 */
tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
    val strictJavadoc = providers.gradleProperty("strictJavadoc").getOrElse("false")
    if ("true" == strictJavadoc) {
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }
}

/*
 * Specify that the encoding should be UTF-8 for source files
 */
tasks.compileJava {
    options.encoding = "UTF-8"
}

/*
 * Support tests with JUnit.
 */
tasks.test {
    useJUnitPlatform()
}

// Looks redundant to include this here and in settings.gradle.kts,
// but helps overcome some gradle trouble when including this as a subproject
// within QuPath itself (which is useful during development).
repositories {
    // Add this if you need access to dependencies only installed locally
    //  mavenLocal()

    mavenCentral()

    // Add scijava - which is where QuPath's jars are hosted
    maven {
        url = uri("https://maven.scijava.org/content/repositories/releases")
    }

    maven {
        url = uri("https://maven.scijava.org/content/repositories/snapshots")
    }

}
