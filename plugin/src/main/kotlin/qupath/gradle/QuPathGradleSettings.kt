package qupath.gradle

import org.gradle.api.GradleException
import org.gradle.api.initialization.Settings
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import java.net.URI

interface QuPathSettingsExtension {

    /**
     * The version of QuPath for which this extension is written.
     * This is used to determine which version catalog to use for dependencies.
     */
    val qupathVersion: Property<String>

    /**
     * The name of the extension, e.g. "qupath-extension-demo"
     */
    val name: Property<String>

    /**
     * The version of the extension, e.g. "0.1.0"
     */
    val version: Property<String>

    /**
     * A short description of the extension.
     */
    val description: Property<String>

    /**
     * The automatic module name for the extension, for insertion into the MANIFEST.MF file.
     */
    val automaticModule: Property<String>

    /**
     * The name of the version catalog to use for QuPath dependencies (default="libs")
     */
    val catalogName: Property<String>

}

class QuPathGradleSettings: Plugin<Settings> {

    override fun apply(settings: Settings) {

        addSciJavaRepositories(settings)

        val extension = createExtension(settings)

        settings.gradle.settingsEvaluated { s ->
            createCatalog(extension, s)
        }
        settings.gradle.rootProject { p ->
            applyToProject(extension, p)
        }

    }

    fun createExtension(settings: Settings): QuPathSettingsExtension {
        val extension = settings.extensions.create(
            "qupathExtension",
            QuPathSettingsExtension::class.java)
        // Default catalog name
        extension.catalogName.set("libs")
        return extension
    }


    fun applyToProject(extension: QuPathSettingsExtension, project: Project) {
        println("APPLYING TO PROJECT NOW")

        with (project.gradle.extensions.extraProperties) {
            if (extension.automaticModule.isPresent)
                set("extension.module", extension.automaticModule.get())

            if (extension.name.isPresent)
                set("extension.name", extension.name.get())
            else
                throw GradleException("QuPath extension name must be set")

            if (extension.version.isPresent)
                set("extension.version", extension.version.get())
            else
                throw GradleException("QuPath extension version must be set (e.g. 0.1.0)")

            if (extension.description.isPresent)
                set("extension.description", extension.description.get())

            if (extension.qupathVersion.isPresent)
                set("qupath.app.version", extension.qupathVersion.get())
            else
                throw GradleException("QuPath version must be set (e.g. 0.6.0)")
        }
    }

    fun addSciJavaRepositories(settings: Settings) {
        settings.dependencyResolutionManagement.repositories {
            println("Adding SciJava Maven repositories")
            it.maven {
                it.url = URI("https://maven.scijava.org/content/repositories/releases")
            }

            it.maven {
                it.url = URI("https://maven.scijava.org/content/repositories/snapshots")
            }
        }
    }

    fun createCatalog(extension: QuPathSettingsExtension, settings: Settings) {
        val catalogName: String = extension.catalogName.orNull ?: return
        with (settings.dependencyResolutionManagement.versionCatalogs) {
            if (names.contains(catalogName)) {
                println("QuPath version catalog named \"$catalogName\" already exists")
                return
            } else {
                create(catalogName) {
                    println("Creating QuPath version catalog named \"$catalogName\"")
                    it.from("io.github.qupath:qupath-catalog:${extension.qupathVersion.get()}")
                }
            }
        }
    }

}
