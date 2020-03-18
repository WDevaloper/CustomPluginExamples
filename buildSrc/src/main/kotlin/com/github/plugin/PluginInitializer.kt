package com.github.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.GradleException
import org.gradle.api.Project

object PluginInitializer {

    fun initial(project: Project) {
        val hasAppPlugin = project.plugins.hasPlugin(AppPlugin::class.java)
        val hasLibPlugin = project.plugins.hasPlugin(LibraryPlugin::class.java)
        if (!hasAppPlugin && !hasLibPlugin) {
            throw  GradleException("Component: The 'com.android.application' or 'com.android.library' plugin is required.")
        }
        this.project = project
        project.extensions.create(COMPONENT_CONFIG, ComponentExtension::class.java)  //创建extensions
    }

    private fun getComponentConfig(): ComponentExtension = project.extensions.getByType(ComponentExtension::class.java)
    fun getComponentInterfaceName(): String = getComponentConfig().matcherInterfaceType.replace(".", "/")
    fun getComponentManagerTypeName(): String = getComponentConfig().matcherManagerType.replace(".", "/")
    fun getComponentManagerTypeInitMethodName(): String = getComponentConfig().matcherManagerTypeMethod

    //gradle 拓展名称
    private const val COMPONENT_CONFIG = "componentExt"

    lateinit var project: Project

}