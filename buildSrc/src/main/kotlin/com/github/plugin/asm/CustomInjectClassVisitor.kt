package com.github.plugin.asm

import com.github.plugin.ScanerCollections
import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter

//访问类,主要是为了注入一些统计信息到方法上的
class CustomInjectClassVisitor(private val classWriter: ClassWriter) : ClassVisitor(Opcodes.ASM7, classWriter) {

    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
        if (interfaces?.contains("com/github/plugin/exalple/test/IComponent") == true
                && name != "") ScanerCollections.add(name ?: "")
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        val visitMethod = super.visitMethod(access, name, descriptor, signature, exceptions)
        return CustomMethodVisitor(visitMethod, access, name, descriptor)
    }
}

//访问类的方法
class CustomMethodVisitor(methodVisitor: MethodVisitor?,
                          access: Int, name: String?, descriptor: String?)
    : AdviceAdapter(Opcodes.ASM7, methodVisitor, access, name, descriptor) {

    //满足指定注解的方法，才会织入代码
    private var isInjectMethod = false

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
        if (MATCH_INJECT_ANNOTATION == descriptor) isInjectMethod = true
        return super.visitAnnotation(descriptor, visible)
    }

    override fun onMethodEnter() {
        if (isInjectMethod) {
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
        if (isInjectMethod) {
            mv.visitLdcInsn("tag")
            mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
            mv.visitLdcInsn("-------> onCreate aaa: ")
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false)
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false)
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "e", "(Ljava/lang/String;Ljava/lang/String;)I", false)
            mv.visitInsn(Opcodes.POP)
            isInjectMethod = false
        }
    }

    companion object {
        private const val MATCH_INJECT_ANNOTATION = "Lcom/github/plugin/exalple/Inject;"
    }
}


