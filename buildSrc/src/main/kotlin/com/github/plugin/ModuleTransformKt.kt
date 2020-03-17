package com.github.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.github.plugin.utils.TypeUtil
import com.github.plugin.utils.eachFileRecurse
import org.apache.commons.io.FileUtils
import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


//Android Gradle Transform
class ModuleTransformKt : Transform() {
    override fun getName(): String {
        return ModuleTransformKt::class.simpleName ?: ""
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

                //1、遍历目录中的文件;
                //2、修改这些文件;
                //3、然后将这些修改过的文件，复制到transforms的输出目录，那么为什么将这些修改过的文件放到transforms，
                // 就会被打包到apk中呢？因为我们自定义的transforms会优先于其他transform执行并且是优先于其他的执行，详细的
                //可以去看看BaseExtension的构造方法
                dirInput.file.eachFileRecurse { file ->
                    // dest===> transforms\ModuleTransformKt\debug\0 D8编译成dex文件
                    // file===> build\intermediates\javac\debug\compileDebugJavaWithJavac\classes\com\github\plugin\exalple\MainActivity.class javac 编译生成的字节码


                    //现在来认证一下，通过asm修改的字节码，是否在javac 或 transforms中？
                    //确实会存在于transforms目录中，但是javac中不存在

                    KLogger.e("dest: $dest   file: ${file.absolutePath}")


                    // 开始织入代码，修改这些文件;
                    if (TypeUtil.isMatchCondition(file.name)) {
                        // 第一版本========
                        val outputFile = File(file.absolutePath.replace(dirInput.file.absolutePath, dest.absolutePath))
                        FileUtils.touch(outputFile)

                        val inputStream = FileInputStream(file)
                        val bytes = weaveSingleClassToByteArray(inputStream)//需要织入代码
                        //修改完毕复制到transforms的输出目录
                        val fos = FileOutputStream(outputFile)
                        fos.write(bytes)
                        fos.close()
                        inputStream.close()
                    }
//                    val bytes = if (file.name == "MainActivity.class") {
//                        weaveSingleClassToByteArray(inputStream)//需要织入代码
//                    } else {
//                        //必须这样写，不然在transforms目录中就没有这些文件，如果是处理的目录，可以不能写出去
//                        //但是如果处理的是jar，你就必须得写出去了，不然就相当把Jar给忽略掉了
//                        inputStream.readBytes()
//                    }
                }
                //这里和上面的处理是一样的，将目录中的文件复制到dest目录中
//                FileUtils.copyDirectory(dirInput.file, dest)
            }
        }
    }


    //----------------------------ASM 代码--------------------------------
    private fun weaveSingleClassToByteArray(inputStream: FileInputStream): ByteArray {
        val classReader = ClassReader(inputStream)
        val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
        val customClassVisitor = CustomClassVisitor(classWriter)
        classReader.accept(customClassVisitor, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }


    //访问类
    class CustomClassVisitor(classVisitor: ClassVisitor?) : ClassVisitor(Opcodes.ASM7, classVisitor) {
        override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
            val visitMethod = super.visitMethod(access, name, descriptor, signature, exceptions)
            return CustomMethodVisitor(visitMethod, access, name, descriptor)
        }
    }

    //访问类的方法
    class CustomMethodVisitor(methodVisitor: MethodVisitor?,
                              access: Int, name: String?, descriptor: String?)
        : AdviceAdapter(Opcodes.ASM7, methodVisitor, access, name, descriptor) {


        private var isInject = false

        override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
            if ("Lcom/github/plugin/exalple/Inject;" == descriptor) isInject = true
            return super.visitAnnotation(descriptor, visible)
        }

        override fun onMethodEnter() {
            if (isInject) {
                //方法执行前插入
                mv.visitLdcInsn("tag")
                mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
                mv.visitInsn(Opcodes.DUP)
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
                mv.visitLdcInsn("-------> onCreate : ")
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
                mv.visitVarInsn(Opcodes.ALOAD, 0)
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false)
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false)
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "e", "(Ljava/lang/String;Ljava/lang/String;)I", false)
                mv.visitInsn(Opcodes.POP)
            }
        }

        override fun onMethodExit(opcode: Int) {
            if (isInject) {
                mv.visitLdcInsn("tag");
                mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
                mv.visitInsn(Opcodes.DUP);
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
                mv.visitLdcInsn("-------> onCreate aaa: ");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "e", "(Ljava/lang/String;Ljava/lang/String;)I", false);
                mv.visitInsn(Opcodes.POP);
                isInject = false
            }
        }
    }
}