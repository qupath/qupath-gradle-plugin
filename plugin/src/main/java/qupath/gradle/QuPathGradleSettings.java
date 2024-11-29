package qupath.gradle;

import org.gradle.api.GradleException;
import org.gradle.api.initialization.Settings;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Set;

public class QuPathGradleSettings implements Plugin<Settings> {

    private static final Logger logger = Logging.getLogger(QuPathGradleSettings.class);

    @Override
    public void apply(@NotNull final Settings settings) {

        addSciJavaRepositories(settings);

        var extension = createExtension(settings);
        var gradle = settings.getGradle();

        gradle.settingsEvaluated(s -> {
            createCatalog(extension, s);
        });

        gradle.rootProject( p -> {
            applyToProject(extension, p);
        });

    }

    private QuPathExtension createExtension(Settings settings) {
        var extension = settings.getExtensions().create(
            "qupathExtension",
            QuPathExtension.class);
        // Default catalog name
        extension.getCatalogName().set("libs");
        return extension;
    }


    private void applyToProject(QuPathExtension extension, Project project) {
        logger.quiet("APPLYING TO PROJECT FROM JAVA");
        
        var props = project.getGradle().getExtensions().getExtraProperties();

        if (extension.getAutomaticModule().isPresent())
            props.set("extension.module", extension.getAutomaticModule().get());

        if (extension.getName().isPresent())
            props.set("extension.name", extension.getName().get());
        else
            throw new GradleException("QuPath extension name must be set");

        if (extension.getVersion().isPresent())
            props.set("extension.version", extension.getVersion().get());
        else
            throw new GradleException("QuPath extension version must be set (e.g. 0.1.0)");

        if (extension.getDescription().isPresent())
            props.set("extension.description", extension.getDescription().get());

        if (extension.getQupathVersion().isPresent())
            props.set("qupath.app.version", extension.getQupathVersion().get());
        else
            throw new GradleException("QuPath version must be set (e.g. 0.6.0)");
    }

    private void addSciJavaRepositories(Settings settings) {
        settings.getDependencyResolutionManagement().repositories(handler -> {
            logger.info("Adding SciJava Maven repositories");
            var urls = Set.of(
                    "https://maven.scijava.org/content/repositories/releases",
                    "https://maven.scijava.org/content/repositories/snapshots"
            );
            for (var url : urls) {
                handler.maven(repo -> repo.setUrl(URI.create(url)));
            }
        });
    }

    private void createCatalog(QuPathExtension extension, Settings settings) {
        String catalogName = extension.getCatalogName().getOrNull();
        if (catalogName == null) {
            logger.info("Version catalog not created (catalog name is null)");
            return;
        }
        var catalogBuilder = settings.getDependencyResolutionManagement().getVersionCatalogs();
        if (catalogBuilder.findByName(catalogName) != null) {
            logger.warn("QuPath version catalog named \"{}\" already exists - no new catalog will be created", catalogName);
            return;
        }
        catalogBuilder.create(catalogName, catalog -> {
            logger.info("Creating QuPath version catalog named \"{}\"", catalogName);
            catalog.from("io.github.qupath:qupath-catalog:" + extension.getQupathVersion().get());
        });
    }

}
