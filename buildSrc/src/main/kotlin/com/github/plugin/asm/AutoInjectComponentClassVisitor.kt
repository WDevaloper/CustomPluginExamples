package com.github.plugin.asm

import com.github.plugin.KLogger
import com.github.plugin.PluginConfig
import com.github.plugin.ScanerCollections
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class AutoInjectComponentClassVisitor(classVisitor: ClassVisitor) : ClassVisitor(Opcodes.ASM7, classVisitor) {
    //如果是实现了IComponent接口的话，将所有组件类收集起来，通过修改字节码的方式生成注册代码到组件管理类中
    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
        KLogger.e("${interfaces?.joinToString { it }}")
        if (interfaces?.contains(PluginConfig.getComponentInterfaceName()) == true && name != "") ScanerCollections.add("$name")
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        KLogger.e("name:$name     descriptor:$descriptor")

        val visitMethod = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (PluginConfig.getComponentManagerTypeInitMethodName() != name) {
            return visitMethod
        }
        return AutoInjectComponentMethodVisitor(visitMethod, access, name, descriptor)
    }
}