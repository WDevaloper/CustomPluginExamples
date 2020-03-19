package com.github.plugin.utils

import org.gradle.api.Project
import org.gradle.api.logging.Logger

object KLogger {
    private lateinit var mLogger: Logger

    fun inject(project: Project) {
        mLogger = project.logger
    }

    fun e(msg: String) = mLogger.error(">>>>>>>>>>>>$msg")
}