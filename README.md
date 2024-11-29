# QuPath Gradle Plugin

This repo contains two Gradle plugins to help create QuPath extensions:
1. A *Settings* plugin to remove boilerplate from `settings.gradle`
2. A *Project* plugin to help simplify and standardize configuration in `build.gradle`

## Settings plugin
This that can be applied to `settings.gradle.kts` to reduce boilerplate.

It adds a new extension to specify the target version of QuPath.

```kotlin
qupath {
    version = "0.6.0-SNAPSHOT"
    catalogName = "libs" // Optional ("libs" is the default)
}
```

Then, it adds the required SciJava repositories and dependencies, and attempts to import the corresponding 
[QuPath version catalog](https://docs.gradle.org/current/userguide/version_catalogs.html).

The catalog can then used in `build.gradle.kts` to declare dependencies, helping ensure they are compatible with the target
QuPath version.

> If you don't want to import the catalog, you can set `catalogName = null`.


## Project plugin
This is really a *conventions* plugin, which currently applies three other plugins:
* `java-library` (standard Gradle plugin)
* [Gradle JavaCPP Platform plugin](https://github.com/bytedeco/gradle-javacpp)
* [JavaFX Gradle plugin](https://github.com/openjfx/javafx-gradle-plugin)

The last two are mostly used to help ensure that the extension doesn't pull in unnecessary platform-specific 
jars for JavaCPP or JavaFX.

The plugin also adds a new extension for easy configuration:

```kotlin
qupathExtension {
    name = "your-extension-name"
    group = "your.group.id"
    version = "0.1.0-SNAPSHOT"
    description = "A simple QuPath extension"
    automaticModule = "your.group.id.extension.name"
}
```

Based on these contents, tasks will be configured to generate jars (including manifests) in a standard way.

### Handling dependencies

Ideally, extensions should use the same dependencies as QuPath itself, to avoid conflicts and ensure compatibility.

If an extension needs to add a different dependency, there are two main options.

#### 1. Use Gradle Shadow
[Gradle Shadow](https://github.com/GradleUp/shadow) makes it possible to create a 'fat jar' that bundles dependencies
into a single jar file.

> **Warning!** Be sure to check licenses are compatible before distributing the fat jar!

To use this, you need to make two changes to `build.gradle.kts`.

First, first add the shadow plugin, e.g.:

```kotlin
plugins {
    id("qupath-conventions")
    // To create a shadow/fat jar that bundle up all dependencies
    id("com.gradleup.shadow") version "8.3.5"
}
```

Then, modify the dependency configuration to use `shadow` instead of `implementation` for jars that you 
*don't* want to bundle, and `implementation` for jars that you *do* want to bundle.

This is important because you shouldn't include jars that are already part of QuPath.

```kotlin
dependencies {

    // Main dependencies for most QuPath extensions - 
    // use 'shadow' to avoid including these
    shadow(libs.bundles.qupath)
    shadow(libs.bundles.logging)
    shadow(libs.qupath.fxtras)

    // Use 'implementation' for the jar you want to bundle
    implementation(libs.bundles.groovy)

    // For testing - shadowed dependencies may need to be included
    testImplementation(libs.bundles.qupath)
    testImplementation(libs.junit)

}
```

#### 2. Distribute the dependencies separately
The second option is to distribute the dependencies separately.
Then the user will need to install them in QuPath along with the extension.

To copy these dependencies into a `build/libs`, you can use
```bash
./gradlew copyDependencies
```

Note that this will probably may copy too many (including ones QuPath already provides).
To overcome that, you can use it in combination with Gradle Shadow, as described above.