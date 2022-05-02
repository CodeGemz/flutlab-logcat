package com.flutlab.logcat

import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class LogsObserver {
    private var observeLogsProcess: Process? = null

    fun startObserve(callback: Callback) {
        if (observeLogsProcess != null) {
            return
        }
        Thread {
            if (observeLogsProcess != null) {
                return@Thread
            }
            val process = try {
                ProcessBuilder()
                        .command("logcat"/*, "-d"*/)
                        .redirectErrorStream(true)
                        .start()
            } catch (e: IOException) {
                Log.e(TAG, "Failed to start `logcat` command", e)
                return@Thread
            }
            observeLogsProcess = process
            val bufferedReader = BufferedReader(
                    InputStreamReader(process.inputStream)
            )
            var line: String
            try {
                while (bufferedReader.readLine().also { line = it ?: "" } != null) {
                    callback.onLogLine(line)
                }
            } catch (e: IOException) {
                Log.e(TAG, "readLine failed", e)
                return@Thread
            }
            Log.i(TAG, "Observe logs finished")
        }.start()
    }

    fun stopObserve() {
        observeLogsProcess?.destroy()
        observeLogsProcess = null
    }

    private companion object {
        const val TAG = "LogsObserver"
    }

    interface Callback {
        fun onLogLine(line: String)
    }
}