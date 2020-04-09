package com.github.plugin.asm

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.InputStream

object WeaveSingleClass {
    fun weaveSingleClassToByteArray(inputStream: InputStream): ByteArray {
        //1、解析字节码
        val classReader = ClassReader(inputStream)
        //2、修改字节码
        val classWriter = ExtendClassWriter(ClassWriter.COMPUTE_MAXS)
        val customClassVisitor = CustomInjectClassVisitor(classWriter)
        //3、开始解析字节码
        classReader.accept(customClassVisitor, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }


    fun scannerAndCollectionComponentClassName(inputStream: InputStream): ByteArray {
        //1、解析字节码
        val classReader = ClassReader(inputStream)
        //2、修改字节码
        val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
        val customClassVisitor = AutoInjectComponentClassVisitor(classWriter)
        //3、开始解析字节码
        classReader.accept(customClassVisitor, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }




    fun realWeaveSingleClassToByteArrayByAutoInject(inputStream: InputStream): ByteArray {
        //1、解析字节码
        val classReader = ClassReader(inputStream)
        //2、修改字节码
        val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
        val customClassVisitor = RealAutoInjectComponentClassVisitor(classWriter)
        //3、开始解析字节码
        classReader.accept(customClassVisitor, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }
}