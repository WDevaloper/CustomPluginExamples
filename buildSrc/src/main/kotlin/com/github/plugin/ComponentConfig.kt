package com.github.plugin

open class ComponentConfig {
    var matcherInterfaceType: String = "" //组件实现接口 如：com/github/plugin/common/IComponent
    var matcherManagerTypeMethod: String = "" //管理类初始化方法  如： initComponent
    var matcherManagerType: String = "" //管理类的全类名  如：com/github/plugin/common/InjectManager
}