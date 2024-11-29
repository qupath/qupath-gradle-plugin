package qupath.gradle;

import org.gradle.api.provider.Property;

/**
 * Gradle extension to configure a QuPath extension.
 */
public interface QuPathExtension {

    /**
     * The name of the extension, e.g. "qupath-extension-example"
     * @return the property
     */
    Property<String> getName();

    /**
     * The version of the extension, e.g. "0.1.0"
     * @return the property
     */
    Property<String> getVersion();

    /**
     * The group of the extension, e.g. "io.github.yourrepo"
     * @return the property
     */
    Property<String> getGroup();

    /**
     * A short description of the extension.
     * @return the property
     */
    Property<String> getDescription();

    /**
     * The automatic module name for the extension, for insertion into the MANIFEST.MF file.
     * @return the property
     */
    Property<String> getAutomaticModule();

}