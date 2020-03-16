package com.github.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project


//自定义插件：
// 1、可以自定义buildSrc，该插件会自动在 classPath 加入插件
class ModuleComponentPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println("自定义插件 >>>>> ModuleComponentPlugin")
        def android = project.extensions.getByType(AppExtension.class)
        android.registerTransform(new ModuleTransform())
    }
}