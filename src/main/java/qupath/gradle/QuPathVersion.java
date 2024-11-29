package qupath.gradle;

import org.gradle.api.provider.Property;

/**
 * Gradle extension to help configure a QuPath extension that targets a specific QuPath version.
 */
public interface QuPathVersion {

    /**
     * The version of QuPath for which this extension is written.
     * This is used to determine which version catalog to use for dependencies.
     * @return the property
     */
    Property<String> getVersion();

    /**
     * The name of the version catalog to use for QuPath dependencies (default="libs")
     * @return the property
     */
    Property<String> getCatalogName();

}
