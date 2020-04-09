package com.github.plugin.asm

import com.github.plugin.PluginInitializer
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class RealAutoInjectComponentClassVisitor(classVisitor: ClassVisitor) : ClassVisitor(Opcodes.ASM7, classVisitor) {
    override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        val visitMethod = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (PluginInitializer.getComponentManagerTypeInitMethodName() != name) {
            return visitMethod
        }
        return AutoInjectComponentMethodVisitor(visitMethod, access, name, descriptor)
    }
}