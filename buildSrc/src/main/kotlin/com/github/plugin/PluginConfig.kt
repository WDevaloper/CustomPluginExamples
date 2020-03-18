package com.github.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.GradleException
import org.gradle.api.Project

object PluginConfig {
    const val COMPONENT_CONFIG = "componentConfig"
    lateinit var project: Project
    fun init(project: Project) {
        val hasAppPlugin = project.plugins.hasPlugin(AppPlugin::class.java)
        val hasLibPlugin = project.plugins.hasPlugin(LibraryPlugin::class.java)
        if (!hasAppPlugin && !hasLibPlugin) {
            throw  GradleException("Component: The 'com.android.application' or 'com.android.library' plugin is required.")
        }
        this.project = project
    }

    fun getComponentConfig(): ComponentConfig {
        return project.extensions.getByType(ComponentConfig::class.java)
    }
}