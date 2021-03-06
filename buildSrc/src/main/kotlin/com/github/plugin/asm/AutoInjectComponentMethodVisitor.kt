package com.github.plugin.asm

import com.github.plugin.utils.KLogger
import com.github.plugin.PluginInitializer
import com.github.plugin.ComponentNameCollection
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

class AutoInjectComponentMethodVisitor(methodVisitor: MethodVisitor?, access: Int, name: String?, descriptor: String?)
    : AdviceAdapter(Opcodes.ASM7, methodVisitor, access, name, descriptor) {
    override fun onMethodExit(opcode: Int) {
        KLogger.e("${ComponentNameCollection.size}    $opcode")

        mv.visitVarInsn(ALOAD, 0)
        mv.visitFieldInsn(GETFIELD, PluginInitializer.getComponentManagerTypeName(), "components", "Ljava/util/List;")
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "clear", "()V", true)

        ComponentNameCollection.forEach { name ->
            KLogger.e(">><<<>>>>>>${name}")
            // 加载this
            mv.visitVarInsn(ALOAD, 0)
            //拿到类的成员变量     坑，你需要注意的类名不要写错了
            mv.visitFieldInsn(GETFIELD, PluginInitializer.getComponentManagerTypeName().replace(".", "/"), "components", "Ljava/util/List;")
            //用无参构造方法创建一个组件实例
            mv.visitTypeInsn(Opcodes.NEW, name)
            mv.visitInsn(Opcodes.DUP)
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, name, "<init>", "()V", false)
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true)
            mv.visitInsn(POP)
        }
    }
}