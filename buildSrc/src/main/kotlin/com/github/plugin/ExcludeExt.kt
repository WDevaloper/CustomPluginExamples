package com.github.plugin


//moduleCP {
//    exclude 'leakcanary.internal'
//    exclude 'com.squareup'
//    exclude 'com.alipay'
//    exclude 'androidx'
//    exclude 'javax'
//    exclude 'org.jetbrains.kotlin'
//    exclude 'org.jetbrains.kotlinx'
//    exclude 'com.android.support'
//}
//exclude通过package路径匹配class文件及jar文件，不再支持通过jar物理文件路径匹配的方式
open class ExcludeExt : ArrayList<String>() {
    fun exclude(filters: String): ExcludeExt {
        add(filters)
        return this
    }

    fun getExcludes(): List<String> {
        return this.map { it.replace(".", "/") }
    }
}