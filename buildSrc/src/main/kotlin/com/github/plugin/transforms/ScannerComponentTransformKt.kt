package com.github.plugin.transforms

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.github.plugin.PluginInitializer
import com.github.plugin.asm.WeaveSingleClass
import com.github.plugin.utils.KLogger
import com.github.plugin.utils.TypeUtil
import com.github.plugin.utils.eachFileRecurse
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.jar.JarFile
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream



//需要注意的是Transform 不能扫描到android.jar中的class
//Android Gradle Transform
class ScannerComponentTransformKt : AbsBaseTransform() {
    override fun getName(): String = "module_cp"


    override fun handleDir(transformInvocation: TransformInvocation, dirInput: DirectoryInput) {

        val dest = transformInvocation.outputProvider
                .getContentLocation(DigestUtils.md5Hex(dirInput.name),
                        dirInput.contentTypes, dirInput.scopes, Format.DIRECTORY)
                .also(FileUtils::forceMkdir)

        dirInput.file.eachFileRecurse { file ->
            if (TypeUtil.isMatchCondition(file.name)) {
                val outputFile = File(file.absolutePath.replace(dirInput.file.absolutePath, dest.absolutePath))
                FileUtils.touch(outputFile)
                val inputStream = FileInputStream(file)
                val bytes = WeaveSingleClass.weaveSingleClassToByteArray(inputStream)//需要织入代码
                val fos = FileOutputStream(outputFile)
                fos.write(bytes)
                fos.close()
                inputStream.close()
            }
        }
    }


    private lateinit var mInjectJarFile: File

    override fun handledJar(transformInvocation: TransformInvocation, jarInput: JarInput) {
        // 将临时class文件拷贝到目标dest文件
        var jarName = jarInput.name//重名名输出文件,因为可能同名,会覆盖
        val md5Name = DigestUtils.md5Hex(jarInput.file.absolutePath)
        //截取.jar，即 去掉.jar       name就是会在__content__.json文件中的name，唯一的
        // name就是会在__content__.json文件中的name，唯一的,随便取，但是一定要保证唯一
        if (jarName.endsWith(".jar")) jarName = jarName.substring(0, jarName.length - 4)


        val dest = transformInvocation.outputProvider.getContentLocation(jarName + md5Name,
                jarInput.contentTypes, jarInput.scopes, Format.JAR)


        if (jarInput.file.absolutePath.endsWith(".jar")) {
            val jarFile = JarFile(jarInput.file)
            val enumeration = jarFile.entries()

            val tmpFile = File(jarInput.file.parent + File.separator + "classes_temp.jar")
            if (tmpFile.exists()) tmpFile.delete() //避免上次的缓存被重复插入
            val tmpJarOutputStream = JarOutputStream(FileOutputStream(tmpFile))

            while (enumeration.hasMoreElements()) {
                val jarEntry = enumeration.nextElement()
                val entryName = jarEntry.name
                val zipEntry = ZipEntry(entryName)

                if (zipEntry.isDirectory) continue

                val inputStream = jarFile.getInputStream(jarEntry)
                //插桩class
                if (TypeUtil.isMatchCondition(entryName)) {
                    if ("com/github/plugin/common/InjectManager.class" == entryName) {
                        KLogger.e(">>>>>>>>>>>----------------")
                        mInjectJarFile = dest
                    }

                    KLogger.e("ASM 开始处理Jar文件中${entryName}文件")
                    tmpJarOutputStream.putNextEntry(zipEntry)
                    val updateCodeBytes = WeaveSingleClass.weaveSingleClassToByteArrayAutoInject(inputStream)
                    tmpJarOutputStream.write(updateCodeBytes)
                    KLogger.e("ASM 结束处理Jar文件中${entryName}文件")
                } else {
//                            KLogger.e("不满足条件Jar文件中${entryName}文件")
                    tmpJarOutputStream.putNextEntry(zipEntry)
                    tmpJarOutputStream.write(IOUtils.toByteArray(inputStream))
                }
                tmpJarOutputStream.closeEntry()
            }
            //结束
            tmpJarOutputStream.close()
            jarFile.close()



            FileUtils.copyFile(tmpFile, dest)
            tmpFile.delete()
        }
    }

    override fun handleAfter(transformInvocation: TransformInvocation) {
        //com/github/plugin/common/InjectManager.class
        KLogger.e("handleAfter")
        /**
         * 写入生成class文件
         */

        KLogger.e("${mInjectJarFile.absolutePath}")
        //扫描之后的JAR文件
        val jarFile = JarFile(mInjectJarFile)
        val zipEntry = ZipEntry("com/github/plugin/common/InjectManager.class")
        val jarInputStream = jarFile.getInputStream(zipEntry)
        val updateCodeBytes = WeaveSingleClass.weaveSingleClassToByteArrayAutoInject(jarInputStream)

        val fos = FileOutputStream(mInjectJarFile)
        val jarOutputStream = JarOutputStream(fos)
        jarOutputStream.putNextEntry(zipEntry)
        jarOutputStream.write(updateCodeBytes)
        jarOutputStream.closeEntry()
        jarOutputStream.close()
        fos.close()
        jarInputStream.close()
    }
}