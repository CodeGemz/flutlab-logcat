package com.codegemz.flutlab.logcat

class GlobalExceptionHandler(
        private val callback: (t: Thread, e: Throwable) -> Unit
) : Thread.UncaughtExceptionHandler {
    private val defaultCatcher: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    /**
     * Method invoked when the given thread terminates due to the
     * given uncaught exception.
     *
     * Any exception thrown by this method will be ignored by the
     * Java Virtual Machine.
     * @param t the thread
     * @param e the exception
     */
    override fun uncaughtException(t: Thread, e: Throwable) {
        callback(t, e)
        defaultCatcher?.uncaughtException(t, e) ?: throw e
    }
}