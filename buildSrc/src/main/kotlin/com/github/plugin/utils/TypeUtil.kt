package com.github.plugin.utils

import com.github.plugin.KLogger
import com.github.plugin.asm.WeaveSingleClass
import org.objectweb.asm.Opcodes
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * 类型判断工具类，用来区分是否是某个特定的类型
 *
 * @author ArgusAPM Team
 */
object TypeUtil {

    fun isMatchCondition(name: String): Boolean {
        return name.endsWith(".class") && !name.contains("R$")
                && !name.contains("R.class") && !name.contains("BuildConfig.class")
    }

    private fun isNeedVisit(access: Int): Boolean {
        //不对抽象方法、native方法、桥接方法、合成方法进行织入
        if (access and Opcodes.ACC_ABSTRACT !== 0
                || access and Opcodes.ACC_NATIVE !== 0
                || access and Opcodes.ACC_BRIDGE !== 0
                || access and Opcodes.ACC_SYNTHETIC !== 0) {
            return false
        }
        return true
    }


    fun weaveJarTask(input: File, output: File) {
        //input: build\intermediates\runtime_library_classes\debug\classes.jar
        //output: build\intermediates\transforms\ModuleTransformKt\debug\0.jar
        KLogger.e("input: ${input.absolutePath}  output: ${output.absolutePath}")
        var zipOutputStream: ZipOutputStream? = null
        var zipFile: ZipFile? = null
        try {
            zipOutputStream = ZipOutputStream(BufferedOutputStream(Files.newOutputStream(output.toPath())))
            zipFile = ZipFile(input)
            val enumeration = zipFile.entries()
            while (enumeration.hasMoreElements()) {
                val zipEntry = enumeration.nextElement()
                val zipEntryName = zipEntry.name
                //jar文件里面就是class文件的了
                // com/github/plugin/usercenter/UserComponent.class
                // com/github/plugin/common/BuildConfig.class
                KLogger.e("zipEntryName:$zipEntryName")
                if (isMatchCondition(zipEntryName)) {
                    val data = WeaveSingleClass.weaveSingleClassToByteArray(BufferedInputStream(zipFile.getInputStream(zipEntry)))
                    val byteArrayInputStream = ByteArrayInputStream(data)
                    val newZipEntry = ZipEntry(zipEntryName)
                    ZipFileUtils.addZipEntry(zipOutputStream, newZipEntry, byteArrayInputStream)
                } else {
                    val inputStream = zipFile.getInputStream(zipEntry)
                    val newZipEntry = ZipEntry(zipEntryName)
                    ZipFileUtils.addZipEntry(zipOutputStream, newZipEntry, inputStream)
                }
            }
        } catch (e: Exception) {
        } finally {
            try {
                if (zipOutputStream != null) {
                    zipOutputStream.finish()
                    zipOutputStream.flush()
                    zipOutputStream.close()
                }
                zipFile?.close()
            } catch (e: Exception) {
                KLogger.e("close stream err!")
            }
        }
    }
}