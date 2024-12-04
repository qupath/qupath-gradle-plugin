package qupath.gradle;

import org.gradle.api.initialization.Settings;
import org.gradle.api.Plugin;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * Plugin to configure QuPath-specific settings for a Gradle build.
 */
public class QuPathGradleSettings implements Plugin<Settings> {

    private static final Logger logger = Logging.getLogger(QuPathGradleSettings.class);

    @Override
    public void apply(@NotNull final Settings settings) {

        addSciJavaRepositories(settings);

        var qupathVersion = createExtension(settings);

        var gradle = settings.getGradle();

        gradle.settingsEvaluated(s -> {
            createCatalog(qupathVersion, s);
        });

    }

    private QuPathVersion createExtension(Settings settings) {
        var extension = settings.getExtensions().create("qupath", QuPathVersion.class);
        // Default catalog name
        extension.getCatalogName().set("libs");
        return extension;
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

    private void createCatalog(QuPathVersion extension, Settings settings) {
        String catalogName = extension.getCatalogName().getOrNull();
        if (catalogName == null || catalogName.isBlank()) {
            logger.info("Version catalog not created (catalog name is missing)");
            return;
        }
        String qupathVersion = extension.getVersion().getOrNull();
        if (qupathVersion == null || qupathVersion.isBlank()) {
            logger.warn("Version catalog not created (QuPath version is missing)");
            return;
        }

        var catalogBuilder = settings.getDependencyResolutionManagement().getVersionCatalogs();
        if (catalogBuilder.findByName(catalogName) != null) {
            logger.warn("QuPath version catalog named \"{}\" already exists - no new catalog will be created", catalogName);
            return;
        }
        catalogBuilder.create(catalogName, catalog -> {
            logger.info("Creating QuPath version catalog named \"{}\"", catalogName);
            catalog.from("io.github.qupath:qupath-catalog:" + qupathVersion);

            if (addQuPathToCatalog(qupathVersion)) {
                catalog.version("qupath", qupathVersion);
                catalog.library("qupath.gui.fx", "io.github.qupath", "qupath-gui-fx").versionRef("qupath");
                catalog.library("qupath.core", "io.github.qupath", "qupath-core").versionRef("qupath");
                catalog.library("qupath.core.processing", "io.github.qupath", "qupath-core-processing").versionRef("qupath");
                catalog.bundle("qupath", List.of("qupath.gui.fx", "qupath.core", "qupath.core.processing"));
                }
        });
    }

    /**
     * Before QuPath v0.6.0, the versions for the QuPath jars weren't included in the catalog -
     * so we need to add these here
     * @param qupathVersion the QuPath version, e.g. "0.5.2"
     * @return
     */
    private static boolean addQuPathToCatalog(String qupathVersion) {
        return qupathVersion.startsWith("0.4") ||
                qupathVersion.startsWith("0.5") ||
                Set.of("0.6.0-rc1", "0.6.0-rc2", "0.6.0-rc3").contains(qupathVersion);
    }

}
