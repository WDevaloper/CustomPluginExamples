package com.github.plugin

import org.gradle.api.logging.Logger

object KLogger {
    private lateinit var mLogger: Logger

    fun inject(logger: Logger) {
        this.mLogger = logger
    }

    fun e(msg: String) = mLogger.error(">>>>>>>>>>>>$msg")
}