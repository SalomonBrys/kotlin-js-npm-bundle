Kotlin-JS Npm-Bundle
====================

A Gradle plugin that generates a package that can be used as-is in an npm JavaScript project.

Requires the Kotlin 1.3.40+ plugin.

The plugin generates the `jsNpmPack` task (or `${target.name}NpmPack` for JS targets not named "js") that generates a tarball in `build/distribution` that:

- embeds all Kotlin/JS dependencies (these are maven dependencies that may not be deployed to the npm registry)
- still references regular npm dependencies
- can be declared as a dependency in an npm project

Usage with Gradle Script Kotlin
-------------------------------

Simply apply the plugin *after the kotlin plugin*:

**Kotlin-multiplatform:**

```kotlin
plugins {
    kotlin("multiplatform") version "1.3.40"
    id("com.github.salomonbrys.gradle.kotlin.js.KotlinJsNpmBundlePlugin") version "1.0.0"
}
```

**Kotlin-js**

```kotlin
plugins {
    kotlin("js") version "1.3.40"
    id("com.github.salomonbrys.gradle.kotlin.js.KotlinJsNpmBundlePlugin") version "1.0.0"
}
```

If you know for a fact that a Kotlin dependency is deployed to the npm registry, you can remove it from the embedded dependencies (reducing the tarball size):

**Kotlin-multiplatform:**

```kotlin
kotlin {
    js {
        npmBundle {
            excludeFromBundle("my-dependency")
        }
    }
}
```

**Kotlin-js**

```kotlin
npmBundle {
    excludeFromBundle("my-dependency")
}
```


Usage with Gradle Script Groovy
-------------------------------

Simply apply the plugin *after the kotlin plugin*:

**Kotlin-multiplatform:**

```groovy
plugins {
    id "org.jetbrains.kotlin.multiplatform" version "1.3.40"
    id "com.github.salomonbrys.gradle.kotlin.js.KotlinJsNpmBundlePlugin" version "1.0.0"
}
```

**Kotlin-js**

```groovy
plugins {
    id "org.jetbrains.kotlin.js" version "1.3.40"
    id "com.github.salomonbrys.gradle.kotlin.js.KotlinJsNpmBundlePlugin" version "1.0.0"
}
```

If you know for a fact that a Kotlin dependency is deployed to the npm registry, you can remove it from the embedded dependencies (reducing the tarball size):

**Kotlin-multiplatform:**

```groovy
npmBundles {
    js {
        excludeFromBundle "my-dependency"
    }
}
```

**Kotlin-js**

```groovy
npmBundle {
    excludeFromBundle "my-dependency"
}
```
