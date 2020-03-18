package com.github.plugin.asm

import com.github.plugin.ComponentNameCollection
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

//访问类,这个类是为了收集实现了IComponent接口的类，然后注入到InjectManager，还有完成
class CustomComponentClassVisitor(classVisitor: ClassVisitor?) : ClassVisitor(Opcodes.ASM7, classVisitor) {
    //是否需要收集类信息
    private var injectClass = false

    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
        super.visit(version, access, name, signature, superName, interfaces)
        injectClass = interfaces?.contains(MATCH_CLASS) ?: false
        if (injectClass && name != "") ComponentNameCollection.add(name ?: "")
    }

    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        val visitMethod = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (!injectClass) {
            return visitMethod
        }
        return CustomMethodVisitor(visitMethod, access, name, descriptor)
    }


    companion object {
        private const val MATCH_CLASS = "com/github/plugin/exalple/test/IComponent"
    }
}