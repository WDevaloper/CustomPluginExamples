package com.github.plugin.inject

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.github.plugin.KLogger
import com.github.plugin.asm.WeaveSingleClass
import com.github.plugin.utils.TypeUtil
import com.github.plugin.utils.eachFileRecurse
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

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
                val dest = transformInvocation.outputProvider.getContentLocation(DigestUtils.md5Hex(dirInput.name),
                        dirInput.contentTypes,
                        dirInput.scopes,
                        Format.DIRECTORY).also(FileUtils::forceMkdir)
                dirInput.file.eachFileRecurse { file ->
                    if (TypeUtil.isMatchCondition(file.name)) {//不能只仅仅操作InjectManager.class,会奔溃，具体我还不知道为甚么
                        val outputFile = File(file.absolutePath.replace(dirInput.file.absolutePath, dest.absolutePath))
                        FileUtils.touch(outputFile)
                        val inputStream = FileInputStream(file)
                        val bytes = WeaveSingleClass.weaveSingleClassToByteArrayAutoInject(inputStream)//需要织入代码
                        val fos = FileOutputStream(outputFile)
                        fos.write(bytes)
                        fos.close()
                        inputStream.close()
                    }
                }
            }


            input.jarInputs.forEach { jarInput ->
                if (jarInput.file.absolutePath.endsWith(".jar")) {
                    val jarFile = JarFile(jarInput.file)
                    val enumeration = jarFile.entries()

                    //用于存放临时操作的class文件，当操作完毕，便将临时文件拷贝到dest文件即可
                    val tmpFile = File(jarInput.file.parent + File.separator + "classes_temp.jar")
                    if (tmpFile.exists()) tmpFile.delete() //避免上次的缓存被重复插入
                    val tmpJarOutputStream = JarOutputStream(FileOutputStream(tmpFile))

                    //用于保存JAR文件，修改JAR中的class
                    while (enumeration.hasMoreElements()) {
                        val jarEntry = enumeration.nextElement() as JarEntry
                        val entryName = jarEntry.name
                        val zipEntry = ZipEntry(entryName)
                        val inputStream = jarFile.getInputStream(jarEntry)
                        //插桩class
                        if (TypeUtil.isMatchCondition(entryName)) {
                            KLogger.e("ASM 开始处理Jar文件中${entryName}文件")
                            tmpJarOutputStream.putNextEntry(zipEntry)
                            val updateCodeBytes = WeaveSingleClass.weaveSingleClassToByteArrayAutoInject(inputStream)
                            tmpJarOutputStream.write(updateCodeBytes)
                            KLogger.e("ASM 结束处理Jar文件中${entryName}文件")
                        } else {
                            KLogger.e("不满足条件Jar文件中${entryName}文件")
                            tmpJarOutputStream.putNextEntry(zipEntry)
                            tmpJarOutputStream.write(IOUtils.toByteArray(inputStream))
                        }
                        tmpJarOutputStream.closeEntry()
                    }
                    //结束
                    tmpJarOutputStream.close()
                    jarFile.close()

                    // 将临时class文件拷贝到目标dest文件
                    var jarName = jarInput.name//重名名输出文件,因为可能同名,会覆盖
                    val md5Name = DigestUtils.md5Hex(jarInput.file.absolutePath)
                    //截取.jar，即 去掉.jar
                    if (jarName.endsWith(".jar")) jarName = jarName.substring(0, jarName.length - 4)
                    val dest = transformInvocation.outputProvider.getContentLocation(jarName + md5Name,
                            jarInput.contentTypes, jarInput.scopes, Format.JAR)

                    //input: build\intermediates\runtime_library_classes\debug\classes.jar
                    //output: build\intermediates\transforms\ModuleTransformKt\debug\0.jar
                    //KLogger.e("input: ${jarInput.file.absolutePath}  output: ${dest.absolutePath}")
                    //KLogger.e("${jarInput.name}   $jarName     ${jarName + md5Name}")

                    FileUtils.copyFile(tmpFile, dest)
                    tmpFile.delete()
                }
            }
        }
    }


}