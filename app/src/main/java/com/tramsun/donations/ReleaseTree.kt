package com.tramsun.donations

import android.util.Log.*
import timber.log.Timber

class ReleaseTree : Timber.Tree() {
    override fun log(priority: Int, tag: String, message: String, t: Throwable) {
        if (priority == VERBOSE || priority == DEBUG) {
            return
        }
        AppCrashLibrary.log(priority, tag, message)

        if (t != null) {
            if (priority == ERROR) {
                AppCrashLibrary.logError(t)
            } else if (priority == WARN) {
                AppCrashLibrary.logWarning(t)
            }
        }
    }
}