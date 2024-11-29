import org.gradle.api.attributes.Usage
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Copy
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.nativeplatform.MachineArchitecture
import org.gradle.nativeplatform.OperatingSystemFamily
import qupath.gradle.QuPathExtension
import kotlin.jvm.optionals.getOrNull

plugins {
    // Main gradle plugin for building a Java library
    `java-library`
    // Include this plugin to avoid downloading JavaCPP dependencies for all platforms
    id("org.bytedeco.gradle-javacpp-platform")
    // Include this plugin to manage JavaFX dependencies
    id("org.openjfx.javafxplugin")
}

/*
 * Create an extension for easy configuration
 */
var qupathExtension = extensions.create("qupathExtension", QuPathExtension::class.java)

/*
 * Set properties that depend upon qupathExtension
 */
afterEvaluate {
    base {
        archivesName = qupathExtension.name.get()
        version = qupathExtension.version.get()
        description = qupathExtension.description.get()
        group = qupathExtension.group.get()
    }

    tasks.jar {
        manifest {
            attributes(
                mapOf(
                    "Implementation-Title" to qupathExtension.name.get(),
                    "Implementation-Version" to qupathExtension.version.get(),
                    "Automatic-Module-Name" to qupathExtension.automaticModule.get()
                )
            )
        }
    }
}

/*
 * Avoid problems with .gitkeep
 */
tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.WARN
}

/*
 * Handle potential JavaFX trouble when using Gradle Shadow; see
 * - https://github.com/qupath/qupath-extension-template/issues/9
 */
val usingShadowJar = project.configurations.names.contains("shadow")
if (usingShadowJar) {
    configurations.named("shadow") {
        attributes {
            attribute(
                Usage.USAGE_ATTRIBUTE, objects.named(
                    Usage::class.java,
                    Usage.JAVA_RUNTIME
                )
            )
            attribute(
                OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE,
                objects.named(OperatingSystemFamily::class.java, "org.gradle.native.operatingSystem")
            )
            attribute(
                MachineArchitecture.ARCHITECTURE_ATTRIBUTE,
                objects.named(MachineArchitecture::class.java, "org.gradle.native.architecture")
            )
        }
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

/*
 * Set the toolchain if we know the required version.
 * Ensure we include sources and javadocs when building.
 */
java {
    val jdkVersion = findRequiredVersionInCatalog("jdk")
    if (!jdkVersion.isNullOrEmpty()) {
        toolchain {
            languageVersion = JavaLanguageVersion.of(jdkVersion)
        }
    }
    withSourcesJar()
    withJavadocJar()
}

/*
 * Set the JavaFX version if we can find it in a version catalog,
 * and include the 'standard' modules required by QuPath.
 */
javafx {
    val jfxVersion = findRequiredVersionInCatalog("javafx")
    if (!jfxVersion.isNullOrEmpty()) {
        version = jfxVersion
    }
    if (usingShadowJar) {
        configuration = "shadow"
    }
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

/*
 * Create javadocs for all modules/packages in one place.
 * Use -PstrictJavadoc=true to fail on error with doclint (which is rather strict).
 */
tasks.javadoc {
    val strictJavadoc = providers.gradleProperty("strictJavadoc").getOrElse("false")
    if ("true" == strictJavadoc) {
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }
}

/*
 * Support tests with JUnit.
 */
tasks.test {
    useJUnitPlatform()
}

/*
 * Ensure we can access the required repositories.
 */
repositories {
    // Add this if you need access to dependencies only installed locally
    if ("true" == providers.gradleProperty("use-maven-local").getOrElse("false")) {
        mavenLocal()
    }

    mavenCentral()

    // Add SciJava - which is where QuPath's jars are hosted
    maven {
        url = uri("https://maven.scijava.org/content/repositories/releases")
    }

    maven {
        url = uri("https://maven.scijava.org/content/repositories/snapshots")
    }

}

/**
 * Check all version catalogs for the required version of a library
 */
fun findRequiredVersionInCatalog(name: String): String? {
    versionCatalogs.forEach { catalog ->
        val version = catalog.findVersion(name).getOrNull()?.requiredVersion
        logger.debug("Found version for {} in {}: {}", name, catalog.name, version)
        if (!version.isNullOrEmpty()) {
            return version
        }
    }
    logger.warn("No version found for {} in any catalogs", name)
    return null
}