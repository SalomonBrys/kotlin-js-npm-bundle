package com.github.salomonbrys.gradle.kotlin.js

import org.gradle.api.Named

class NpmBundle(private val name: String): Named {

    override fun getName() = name

    val excludeFromBundle = mutableSetOf("kotlin")

    fun excludeFromBundle(vararg packages: String) { excludeFromBundle.addAll(packages) }

}
