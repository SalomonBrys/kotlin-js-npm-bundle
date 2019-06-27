
plugins {
    kotlin("jvm") version "1.3.40"
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.9.10"
}

group = "com.github.salomonbrys.gradle.kotlin.js"
version = "1.0.0"

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("gradle-plugin"))
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
}

kotlin {
    target {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

pluginBundle {
    website = "https://github.com/SalomonBrys/kotlin-js-npm-bundle"
    vcsUrl = "https://github.com/SalomonBrys/kotlin-js-npm-bundle.git"
    tags = listOf("kotlin", "kotlin-js", "kotlin-multiplatform", "npm")

    plugins {
        create(project.name) {
            id = "com.github.salomonbrys.gradle.kotlin.js.npm-bundle"
            description = "A Gradle plugin that generates a package that can be used as-is in an npm JavaScript project."
            displayName = project.name
        }
    }
}
