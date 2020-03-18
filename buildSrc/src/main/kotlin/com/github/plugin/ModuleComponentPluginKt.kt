package com.github.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.github.plugin.intener.BuildTimeListener
import org.gradle.api.Plugin
import org.gradle.api.Project


//自定义Gradle 插件
class ModuleComponentPluginKt : Plugin<Project> {
    private lateinit var mProject: Project
    override fun apply(project: Project) {
        this.mProject = project
        KLogger.inject(project.logger)
        KLogger.e("自定义插件ModuleComponentPluginKt")
        //创建extensions
        project.extensions.create(PluginConfig.COMPONENT_CONFIG, ComponentConfig::class.java)
        PluginConfig.init(project)

        if (project.plugins.hasPlugin(AppPlugin::class.java)) {
            // 监听每个任务的执行时间
            project.gradle.addListener(BuildTimeListener())
            val android = project.extensions.getByType(AppExtension::class.java)
            android.registerTransform(ModuleTransformKt())
            android.registerTransform(AfterTransform())
        }
    }
}