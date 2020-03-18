package com.github.plugin

//收集组件类名称
object ComponentNameCollection : ArrayList<String>() {
    override fun add(element: String): Boolean {
        if (contains(element)) return false
        return super.add(element)
    }
}