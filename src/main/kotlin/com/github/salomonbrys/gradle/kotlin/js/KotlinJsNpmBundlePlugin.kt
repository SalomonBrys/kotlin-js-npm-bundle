package com.github.salomonbrys.gradle.kotlin.js

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinJsProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsTarget
import org.jetbrains.kotlin.gradle.targets.js.nodejs.nodeJs

class KotlinJsNpmBundlePlugin : Plugin<Project> {

    lateinit var yarnImportedWorkspaces: List<List<String>>

    private fun Project.createResolveYarnImportedWorkspacesTask() {
        task<Task>("resolveYarnImportedWorkspaces") {
            dependsOn(nodeJs.root.npmResolveTask)
            group = "build"
            doLast {
                yarnImportedWorkspaces = project.file("$buildDir/js/package.json").reader().use { JsonParser().parse(it).asJsonObject.getAsJsonArray("workspaces") }
                    .map { it.asString }
                    .filter { it.startsWith("packages_imported/") }
                    .map { it.substring(18) }
                    .map { it.split("/", limit = 2) }
            }
        }
    }

    override fun apply(project: Project) {
        project.createResolveYarnImportedWorkspacesTask()

        val kotlin = project.extensions["kotlin"]
        when (kotlin) {
            is KotlinMultiplatformExtension -> project.applyMultiplatformPlugin(kotlin)
            is KotlinJsProjectExtension -> project.applyJsPlugin(kotlin)
            else -> {
                project.logger.error("The Kotlin JS NPM Bundle plugin needs either kotlin-multiplatform or kotlin-js plugin.")
            }
        }

    }

    private fun Project.applyMultiplatformPlugin(kotlin: KotlinMultiplatformExtension) {
        val bundles = container<NpmBundle>()
        extensions.add("npmBundles", bundles)
        extensions.add("npmBundle", KotlinMultiplatformNpmBundleExtension(bundles))

        afterEvaluate {
            kotlin.targets
                .filterIsInstance<KotlinJsTarget>()
                .forEach { target ->
                    registerTarget(target, bundles.maybeCreate(target.name), target.name == "js")
                }
        }
    }

    private fun Project.applyJsPlugin(kotlin: KotlinJsProjectExtension) {
        val bundle = NpmBundle("js")
        extensions.add("npmBundle", bundle)

        afterEvaluate {
            registerTarget(kotlin.target, bundle, true)
        }
    }

    private fun Project.registerTarget(target: KotlinJsTarget, config: NpmBundle, isMain: Boolean) {
        val packageName = if (isMain) project.name else "${project.name}-${target.name}"

        val archiveNpmPack = task<Tar>("${target.name}ArchiveNpmPack") {
            group = "build"
            compression = Compression.GZIP
            archiveName = "${project.name}-${target.name}-npm-${project.version}.tgz"

            into("package/kotlin") {
                it.from("$buildDir/js/packages/$packageName/kotlin")
            }

            into("package/") {
                it.from("$buildDir/publications/${target.name}-npm/package.json")
            }
        }

        task<Task>("${target.name}NpmPack") {
            group = "build"
            dependsOn("resolveYarnImportedWorkspaces", target.compilations["main"].compileAllTaskName)
            finalizedBy(archiveNpmPack)
            archiveNpmPack.dependsOn(this)

            doLast {
                val imported = yarnImportedWorkspaces
                    .filterNot { (name, _) -> name in config.excludeFromBundle }

                imported.forEach { (name, version) ->
                    archiveNpmPack.into("package/node_modules/$name") {
                        it.from("$buildDir/js/packages_imported/$name/$version")
                    }
                }

                val json = file("$buildDir/js/packages/$packageName/package.json").reader().use { JsonParser().parse(it).asJsonObject }
                json.add("bundledDependencies", JsonArray())
                imported.forEach { (name, _) ->
                    json.getAsJsonObject("dependencies").remove(name)
                    json.getAsJsonArray("bundledDependencies").add(name)
                }
                mkdir("$buildDir/publications/${target.name}-npm")
                file("$buildDir/publications/${target.name}-npm/package.json").writer().use { output ->
                    GsonBuilder().setPrettyPrinting().create().toJson(json, output)
                }
            }
        }

        project.pluginManager.withPlugin("maven-publish") {
            project.extensions.configure<PublishingExtension> {
                publications {
                    it.create<MavenPublication>("${target.name}-npm") {
                        artifact(archiveNpmPack)
                        artifactId = "${project.name}-${target.name}-npm"
                    }
                }
            }
        }

    }

}