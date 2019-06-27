package com.github.salomonbrys.gradle.kotlin.js

import org.gradle.api.NamedDomainObjectContainer
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsTarget

class KotlinMultiplatformNpmBundleExtension(private val bundles: NamedDomainObjectContainer<NpmBundle>) {

    fun KotlinJsTarget.excludeFromBundle(vararg packages: String) = bundles.maybeCreate(name).excludeFromBundle(*packages)

}
