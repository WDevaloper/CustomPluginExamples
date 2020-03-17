package com.github.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.github.plugin.asm.CustomInjectClassVisitor
import com.github.plugin.asm.WeaveSingleClass
import com.github.plugin.utils.TypeUtil
import com.github.plugin.utils.ZipFileUtils
import com.github.plugin.utils.eachFileRecurse
import com.github.plugin.utils.getUniqueJarName
import org.apache.commons.io.FileUtils
import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter
import java.io.*
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream


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
                    // file===> build\intermediates\javac\debug\compileDebugJavaWithJavac\classes\com\github\plugin\examlple\MainActivity.class javac 编译生成的字节码


                    //现在来认证一下，通过asm修改的字节码，是否在javac 或 transforms中？
                    //确实会存在于transforms目录中，但是javac中不存在
                    if (TypeUtil.isMatchCondition(file.name)) {
                        val outputFile = File(file.absolutePath.replace(dirInput.file.absolutePath, dest.absolutePath))
                        FileUtils.touch(outputFile)

                        //Dest目录: build\intermediates\transforms\ModuleTransformKt\debug\0
                        //输入文件:  build\intermediates\javac\debug\compileDebugJavaWithJavac\classes\com\github\plugin\examlple\Inject.class
                        //输出文件： build\intermediates\transforms\ModuleTransformKt\debug\0\com\github\plugin\exalple\Inject.class
                        KLogger.e("inputFile: ${file.absolutePath}   outputFile: ${outputFile.absolutePath}   destFile: ${dest.absolutePath}")

                        val inputStream = FileInputStream(file)
                        // 开始织入代码，修改这些文件，即：对输入的文件进行修改
                        val bytes = WeaveSingleClass.weaveSingleClassToByteArray(inputStream)//需要织入代码
                        //修改输入文件完毕复制输出文件中
                        val fos = FileOutputStream(outputFile)
                        fos.write(bytes)
                        fos.close()
                        inputStream.close()
                    }
                }
                //这里和上面的处理是一样的，将目录中的文件复制到dest目录中
//                FileUtils.copyDirectory(dirInput.file, dest)
            }


            // TODO 多模块需要处理Jar，因为lib最后打包是已jar形式引入
            //common\build\intermediates\runtime_library_classes\debug\classes.jar
            //usercenter\build\intermediates\runtime_library_classes\debug\classes.jar
            input.jarInputs.forEach { jarInput ->
                val dest = transformInvocation.outputProvider.getContentLocation(
                        jarInput.file.getUniqueJarName(),
                        jarInput.contentTypes,
                        jarInput.scopes,
                        Format.JAR)
                weaveJarTask(jarInput.file, dest)
            }
        }
    }


    private fun weaveJarTask(input: File, output: File) {
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
                if (TypeUtil.isMatchCondition(zipEntryName)) {

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