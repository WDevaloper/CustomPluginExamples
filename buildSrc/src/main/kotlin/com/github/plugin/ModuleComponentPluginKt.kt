package com.github.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.github.plugin.intener.BuildTimeListener
import com.github.plugin.transforms.ScannerAfterTransform
import com.github.plugin.transforms.ScannerComponentTransformKt
import com.github.plugin.utils.KLogger
import org.gradle.api.Plugin
import org.gradle.api.Project


//注意点：1、使用ASM时，一定要注意asm 字节码格式写对，不然很难找到出错的原因


//自定义Gradle 插件
class ModuleComponentPluginKt : Plugin<Project> {
    private lateinit var mProject: Project
    override fun apply(project: Project) {
        this.mProject = project
        KLogger.inject(project.logger)
        KLogger.e("自定义插件ModuleComponentPluginKt")
        PluginInitializer.initial(project)

        if (project.plugins.hasPlugin(AppPlugin::class.java)) {
            // 监听每个任务的执行时间
            project.gradle.addListener(BuildTimeListener())
            val android = project.extensions.getByType(AppExtension::class.java)
            android.registerTransform(ScannerComponentTransformKt())
            android.registerTransform(ScannerAfterTransform())
        }
    }
}