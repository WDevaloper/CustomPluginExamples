package com.github.plugin.utils

import org.gradle.api.logging.Logger

object KLogger {
    private lateinit var mLogger: Logger

    fun inject(logger: Logger) {
        mLogger = logger
    }

    fun e(msg: String) = mLogger.error(">>>>>>>>>>>>$msg")
}