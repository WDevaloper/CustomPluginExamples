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
        //  创建extensions  ，可以通过extensions.getByType拿到这个拓展对象
        project.extensions.create(COMPONENT_CONFIG_NAME, ComponentExtension::class.java)
        project.extensions.create(EXCLUDE_CONFIG_NAME, ExcludeExt::class.java)
    }

    private fun getComponentConfig(): ComponentExtension = project.extensions.getByType(ComponentExtension::class.java)
    fun getComponentInterfaceName(): String = getComponentConfig().matcherInterfaceType.replace(".", "/")
    fun getComponentManagerTypeName(): String = getComponentConfig().matcherManagerType.replace(".", "/")
    fun getComponentManagerTypeInitMethodName(): String = getComponentConfig().matcherManagerTypeMethod

    fun getExclude(): ExcludeExt {
        return project.extensions.getByType(ExcludeExt::class.java)
    }


    //"com/github/plugin/common/InjectManager.class"

    //componentExt {
    //    matcherInterfaceType "com.github.plugin.common.IComponent"
    //    matcherManagerType "com.github.plugin.common.InjectManager"
    //    matcherManagerTypeMethod "initComponent"
    //}
    fun getManagerClassName(): String {
        return "${getComponentManagerTypeName()}.class"
    }

    //gradle 拓展名称
    private const val COMPONENT_CONFIG_NAME = "componentExt"
    private const val EXCLUDE_CONFIG_NAME = "moduleCP"

    lateinit var project: Project

}