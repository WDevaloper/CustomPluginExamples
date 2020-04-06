package com.github.plugin.transforms

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
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
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry


//Android Gradle Transform
class ScannerComponentTransformKt1 : Transform() {
    override fun getName(): String {
        return "scanner_component_result"
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
        KLogger.e("${PluginInitializer.getComponentInterfaceName()}   ${PluginInitializer.getComponentManagerTypeInitMethodName()}  ${PluginInitializer.getComponentManagerTypeName()}")
        if (!transformInvocation.isIncremental) {
            transformInvocation.outputProvider.deleteAll()
        }

        transformInvocation.inputs.forEach { input ->
            input.directoryInputs.forEach { dirInput ->
                //处理完输入文件之后，要把输出给下一个任务,就是在：transforms\ScannerComponentTransformKt\debug\0目录中
                // name就是会在__content__.json文件中的name，唯一的,随便取，但是一定要保证唯一
                val dest = transformInvocation.outputProvider.getContentLocation(DigestUtils.md5Hex(dirInput.name),
                        dirInput.contentTypes, dirInput.scopes, Format.DIRECTORY).also(FileUtils::forceMkdir)

                //1、遍历目录中的文件;
                //2、修改这些文件;
                //3、然后将这些修改过的文件，复制到transforms的输出目录，那么为什么将这些修改过的文件放到transforms，
                // 就会被打包到apk中呢？因为我们自定义的transforms会优先于其他transform执行并且是优先于其他的执行，详细的
                //可以去看看BaseExtension的构造方法
                dirInput.file.eachFileRecurse { file ->
                    // dest===> transforms\ScannerComponentTransformKt\debug\0 D8编译成dex文件
                    // file===> build\intermediates\javac\debug\compileDebugJavaWithJavac\classes\com\github\plugin\examlple\MainActivity.class javac 编译生成的字节码


                    //现在来认证一下，通过asm修改的字节码，是否在javac 或 transforms中？
                    //确实会存在于transforms目录中，但是javac中不存在
                    if (TypeUtil.isMatchCondition(file.name)) {
                        val outputFile = File(file.absolutePath.replace(dirInput.file.absolutePath, dest.absolutePath))
                        FileUtils.touch(outputFile)

                        // Dest目录: build\intermediates\transforms\ScannerComponentTransformKt\debug\0
                        // 输入文件:  build\intermediates\javac\debug\compileDebugJavaWithJavac\classes\com\github\plugin\examlple\Inject.class
                        // 输出文件： build\intermediates\transforms\ScannerComponentTransformKt\debug\0\com\github\plugin\exalple\Inject.class
                        // build\tmp\kotlin-classes\debug\com\github\plugin\exalple\test\MainComponentKt.class
                        // build\intermediates\transforms\scanner_component_result\debug\9\com\github\plugin\exalple\test\MainComponentKt.class
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
                if (jarInput.file.absolutePath.endsWith(".jar")) {
                    val jarFile = JarFile(jarInput.file)
                    val enumeration = jarFile.entries()

                    //用于存放临时操作的class文件，当操作完毕，便将临时文件拷贝到dest文件即可
                    val tmpFile = File(jarInput.file.parent + File.separator + "classes_temp.jar")
                    if (tmpFile.exists()) tmpFile.delete() //避免上次的缓存被重复插入
                    val tmpJarOutputStream = JarOutputStream(FileOutputStream(tmpFile))

                    //用于保存JAR文件，修改JAR中的class
                    while (enumeration.hasMoreElements()) {
                        val jarEntry = enumeration.nextElement()
                        val entryName = jarEntry.name
                        val zipEntry = ZipEntry(entryName)

                        if (zipEntry.isDirectory) continue

                        val inputStream = jarFile.getInputStream(jarEntry)
                        //插桩class
                        if (TypeUtil.isMatchCondition(entryName)) {
                            KLogger.e("ASM 开始处理Jar文件中${entryName}文件")
                            tmpJarOutputStream.putNextEntry(zipEntry)
                            val updateCodeBytes = WeaveSingleClass.weaveSingleClassToByteArray(inputStream)
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

                    // 将临时class文件拷贝到目标dest文件
                    var jarName = jarInput.name//重名名输出文件,因为可能同名,会覆盖
                    val md5Name = DigestUtils.md5Hex(jarInput.file.absolutePath)
                    //截取.jar，即 去掉.jar       name就是会在__content__.json文件中的name，唯一的
                    // name就是会在__content__.json文件中的name，唯一的,随便取，但是一定要保证唯一
                    if (jarName.endsWith(".jar")) jarName = jarName.substring(0, jarName.length - 4)
                    val dest = transformInvocation.outputProvider.getContentLocation(jarName + md5Name,
                            jarInput.contentTypes, jarInput.scopes, Format.JAR)


                    //input: build\intermediates\runtime_library_classes\debug\classes.jar
                    //                    //output: build\intermediates\transforms\ScannerComponentTransformKt\debug\0.jar
                    //                    //KLogger.e("input: ${jarInput.file.absolutePath}  output: ${dest.absolutePath}")
                    //                    //KLogger.e("${jarInput.name}   $jarName     ${jarName + md5Name}")

                    FileUtils.copyFile(tmpFile, dest)
                    tmpFile.delete()
                }
            }
        }
    }
}