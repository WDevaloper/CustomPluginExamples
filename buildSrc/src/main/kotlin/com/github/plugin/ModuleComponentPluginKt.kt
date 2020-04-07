package com.github.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.LoggerWrapper
import com.android.build.gradle.internal.transforms.InstantRunVerifierTransform
import com.github.plugin.intener.BuildTimeListener
import com.github.plugin.transforms.ScannerAfterTransformKt
import com.github.plugin.transforms.ScannerComponentTransformKt
import com.github.plugin.utils.KLogger
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging


//注意点：1、使用ASM时，一定要注意asm 字节码格式写对，不然很难找到出错的原因


//自定义Gradle 插件


/**
 *
 * {https://juejin.im/post/5a2b95b96fb9a045284669a9}
 * {https://www.jianshu.com/p/811b0d0975ef}
 * {https://www.jianshu.com/p/16ed4d233fd1}
 * {https://github.com/Qihoo360/ArgusAPM}
 *
 * Android Gradle Transform API是从Gradle 1.5.0版本之后提供的,它允许第三方在打包Dex文件之前的编译过程中修改java字节码
 * 自定义插件注册的transform会在ProguardTransform和DexTransform之前执行，所以自动注册的类不需要考虑混淆的情况
 *
 *  两种方案：
 *
 *  1、可以在transform中使用容器中收集，然后等transform结束，通过遍历容器使用asm织入代码;
 *  2、可以定义两个transform，一个用于收集，一个用于织入代码
 *
 */
class ModuleComponentPluginKt : Plugin<Project> {
    private lateinit var mProject: Project
    override fun apply(project: Project) {
        this.mProject = project
        KLogger.inject(project)
        KLogger.e("自定义插件ModuleComponentPluginKt")
        if (project.plugins.hasPlugin(AppPlugin::class.java)) {
            PluginInitializer.initial(project)
            // 监听每个任务的执行时间
            project.gradle.addListener(BuildTimeListener())
            val android = project.extensions.getByType(AppExtension::class.java)
            //主要操作就是收集满足条件的类
            android.registerTransform(ScannerComponentTransformKt())
            //收集完毕，在这里完成代码的织入
//            android.registerTransform(ScannerAfterTransformKt())
            project.gradle.buildFinished {
                KLogger.e("buildFinished")
            }
        }
    }
}