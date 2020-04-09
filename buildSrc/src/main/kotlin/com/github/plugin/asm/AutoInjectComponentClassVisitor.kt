package com.github.plugin.asm

import com.github.plugin.utils.KLogger
import com.github.plugin.PluginInitializer
import com.github.plugin.ComponentNameCollection
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class AutoInjectComponentClassVisitor(classVisitor: ClassVisitor) : ClassVisitor(Opcodes.ASM7, classVisitor) {
    //如果是实现了IComponent接口的话，将所有组件类收集起来，通过修改字节码的方式生成注册代码到组件管理类中
    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
        KLogger.e("${interfaces?.joinToString { it }}")
        if (interfaces?.contains(PluginInitializer.getComponentInterfaceName()) == true && name != "") {
            ComponentNameCollection.add("$name")
        }
        super.visit(version, access, name, signature, superName, interfaces)
    }
}