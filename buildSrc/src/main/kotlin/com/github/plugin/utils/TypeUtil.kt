package com.github.plugin.utils

import org.objectweb.asm.Opcodes

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
}