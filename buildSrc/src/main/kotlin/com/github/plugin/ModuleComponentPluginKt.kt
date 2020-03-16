package com.github.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class ModuleComponentPluginKt : Plugin<Project> {
    override fun apply(project: Project) {
        println("自定义插件 >>>>> ModuleComponentPluginKt")
        val android = project.extensions.getByType(AppExtension::class.java)
        android.registerTransform(ModuleTransformKt())
    }
}