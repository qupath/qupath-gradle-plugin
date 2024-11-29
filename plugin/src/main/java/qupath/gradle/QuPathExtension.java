package qupath.gradle;

import org.gradle.api.provider.Property;

/**
 * Gradle extension to configure a QuPath extension.
 */
public interface QuPathExtension {

    /**
     * The name of the extension, e.g. "qupath-extension-demo"
     */
    Property<String> getName();

    /**
     * The version of the extension, e.g. "0.1.0"
     */
    Property<String> getVersion();

    /**
     * The group of the extension, e.g. "io.github.yourrepo"
     */
    Property<String> getGroup();

    /**
     * A short description of the extension.
     */
    Property<String> getDescription();

    /**
     * The automatic module name for the extension, for insertion into the MANIFEST.MF file.
     */
    Property<String> getAutomaticModule();

}