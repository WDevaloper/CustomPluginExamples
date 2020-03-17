package com.github.plugin

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.github.plugin.asm.CustomInjectClassVisitor
import com.github.plugin.asm.ExtendClassWriter
import com.github.plugin.asm.WeaveSingleClass
import com.github.plugin.utils.TypeUtil
import com.github.plugin.utils.eachFileRecurse
import org.apache.commons.io.FileUtils
import org.gradle.internal.impldep.aQute.bnd.osgi.OpCodes
import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class AfterTransform : Transform() {
    override fun getName(): String {
        return AfterTransform::class.simpleName ?: ""
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun isIncremental(): Boolean {
        return false
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }


    override fun transform(transformInvocation: TransformInvocation) {
        KLogger.e(">>>>>>>>>>>>>AfterTransform")

        if (!transformInvocation.isIncremental) {
            transformInvocation.outputProvider.deleteAll()
        }


        transformInvocation.inputs.forEach { input ->


            input.directoryInputs.forEach { dirInput ->
                //处理完输入文件之后，要把输出给下一个任务,就是在：transforms\ModuleTransformKt\debug\0目录中
                val dest = transformInvocation.outputProvider.getContentLocation(dirInput.name,
                        dirInput.contentTypes,
                        dirInput.scopes,
                        Format.DIRECTORY).also(FileUtils::forceMkdir)
                dirInput.file.eachFileRecurse { file ->
                    if (TypeUtil.isMatchCondition(file.name)) {//不能只仅仅操作InjectManager.class,会奔溃，具体我还不知道为甚么
                        val outputFile = File(file.absolutePath.replace(dirInput.file.absolutePath, dest.absolutePath))
                        FileUtils.touch(outputFile)
                        val inputStream = FileInputStream(file)
                        val bytes = weaveSingleClassToByteArray2(inputStream)//需要织入代码
                        val fos = FileOutputStream(outputFile)
                        fos.write(bytes)
                        fos.close()
                        inputStream.close()
                    }
                }
            }
        }
    }

    private fun weaveSingleClassToByteArray2(inputStream: FileInputStream): ByteArray {

        //1、解析字节码
        val classReader = ClassReader(inputStream)

        //2、修改字节码
        val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
        val customClassVisitor = CustomScannerInjectClassVisitor(classWriter)

        //3、开始解析字节码
        classReader.accept(customClassVisitor, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }
}

class CustomScannerInjectClassVisitor(classVisitor: ClassVisitor) : ClassVisitor(Opcodes.ASM7, classVisitor) {
    override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        KLogger.e("name:$name     descriptor:$descriptor")
        val visitMethod = super.visitMethod(access, name, descriptor, signature, exceptions)
        if ("init" != name) {
            return visitMethod
        }
        return CustomScannerMethod(visitMethod, access, name, descriptor)
    }
}

class CustomScannerMethod(methodVisitor: MethodVisitor?, access: Int, name: String?, descriptor: String?) : AdviceAdapter(Opcodes.ASM7, methodVisitor, access, name, descriptor) {

    override fun onMethodEnter() {
        KLogger.e("${ScanerCollections.size}")
        ScanerCollections.forEach { name ->
            mv.visitVarInsn(ALOAD, 0)
            mv.visitFieldInsn(GETFIELD, "com/github/plugin/exalple/test/InjectManager", "components", "Ljava/util/List;")
            //用无参构造方法创建一个组件实例
            mv.visitTypeInsn(Opcodes.NEW, name)
            mv.visitInsn(Opcodes.DUP)
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, name, "<init>", "()V", false)
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true)
            mv.visitInsn(POP)
        }
    }
}