package qupath.gradle;

import org.gradle.api.provider.Property;

public abstract class QuPathVersion {

    public abstract Property<String> getVersion();

    public abstract Property<String> getCatalogName();

}
