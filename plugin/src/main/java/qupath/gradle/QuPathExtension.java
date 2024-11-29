package qupath.gradle;

import org.gradle.api.provider.Property;

public interface QuPathExtension {

    /**
     * The version of QuPath for which this extension is written.
     * This is used to determine which version catalog to use for dependencies.
     */
    Property<String> getQupathVersion();

    /**
     * The name of the extension, e.g. "qupath-extension-demo"
     */
    Property<String> getName();

    /**
     * The version of the extension, e.g. "0.1.0"
     */
    Property<String> getVersion();

    /**
     * A short description of the extension.
     */
    Property<String> getDescription();

    /**
     * The automatic module name for the extension, for insertion into the MANIFEST.MF file.
     */
    Property<String> getAutomaticModule();

    /**
     * The name of the version catalog to use for QuPath dependencies (default="libs")
     */
    Property<String> getCatalogName();

}