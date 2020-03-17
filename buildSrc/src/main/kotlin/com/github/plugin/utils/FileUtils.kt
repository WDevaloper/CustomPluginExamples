package com.github.plugin.utils

import java.io.File


//递归遍历文件夹的文件
fun File.eachFileRecurse(action: (File) -> Unit) {
    if (!isDirectory) {
        action(this)
    } else {
        listFiles()?.forEach { file ->
            if (file.isDirectory) {
                file.eachFileRecurse(action)
            } else {
                action(file)
            }
        }
    }
}