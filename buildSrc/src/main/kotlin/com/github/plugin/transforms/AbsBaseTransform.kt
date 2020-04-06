package com.github.plugin.transforms

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager

abstract class AbsBaseTransform : Transform() {
    override fun getName(): String {
        return AbsBaseTransform::class.java.simpleName
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS//操作字节码
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT//范围
    }

    override fun isIncremental(): Boolean {
        return false//是否增量更新
    }


    override fun transform(transformInvocation: TransformInvocation) {
        if (!transformInvocation.isIncremental) transformInvocation.outputProvider.deleteAll()
        transformInvocation.inputs.forEach { input ->
            input.directoryInputs.forEach { dirInput -> handleDir(transformInvocation, dirInput) }
            input.jarInputs.forEach { jarInput -> handledJar(transformInvocation, jarInput) }
        }
        handleAfter(transformInvocation)
    }

    abstract fun handleDir(transformInvocation: TransformInvocation, dirInput: DirectoryInput)
    abstract fun handledJar(transformInvocation: TransformInvocation, jarInput: JarInput)
    abstract fun handleAfter(transformInvocation: TransformInvocation)
}