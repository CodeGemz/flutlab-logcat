package com.codegemz.flutlab.logcat

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import com.codegemz.flutlab.logcat.util.ApplicationUtils
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel


class MethodCallHandlerImpl(private val context: Context) : MethodChannel.MethodCallHandler {
    private var messenger: Messenger? = null
    private var connecting = false
    private var connected = false

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        var logsObserver: LogsObserver? = null
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            messenger = Messenger(service)
            connected = true
            val logsObserver = LogsObserver()
            logsObserver.startObserve(object : LogsObserver.Callback {
                override fun onLogLine(line: String) {
                    val message = Message.obtain(null, InstallerConstants.INCOMING_MESSAGE_LOG_LINE_KEY)
                    message.data = Bundle().apply {
                        putString(InstallerConstants.LINE_KEY, line)
                    }
                    sendMessage(message)
                }
            })
            this.logsObserver = logsObserver
        }

        override fun onServiceDisconnected(name: ComponentName) {
            messenger = null
            connected = false
            logsObserver?.stopObserve()
            logsObserver = null
        }
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "init" -> init(result)
            else -> result.notImplemented()
        }
    }

    private fun init(result: MethodChannel.Result) {
        if (connecting) {
            Log.e(TAG, "Connecting in progress")
            result.error("connecting", "Connecting in progress", null)
            return
        }
        if (connected) {
            Log.e(TAG, "Already connected")
            result.error("connected", "Already connected", null)
            return
        }
        connecting = true
        if (!ApplicationUtils.isApplicationInstalled(context, APPLICATION_PACKAGE_NAME)) {
            result.error("installer_not_installed", "FlutLab Installer not installed on the device", null)
            connecting = false
            return
        }
        val intent = Intent("com.codegemz.flutlab.installer.LOG_SERVICE")
        val componentName = ComponentName(
                APPLICATION_PACKAGE_NAME,
                SERVICE_CLASS_NAME
        )
        intent.component = componentName
        intent.putExtra("packageName", context.packageName)
        val bindSuccess = try {
            context.bindService(intent, serviceConnection,
                    Context.BIND_AUTO_CREATE or Context.BIND_IMPORTANT
            )
        } catch (e: Throwable) {
            val message = "connect bindService error: " + e.message
            Log.e(TAG, message, e)
            result.error("bind_failed", e.message, null)
            connecting = false
            return
        }
        Log.i(TAG, "Bind: $bindSuccess")

        if (bindSuccess) {
            result.success("Bind success")
        } else {
            result.error("bind_result_false", "Bind returned failed", null)
        }
        connecting = false
    }

    private fun sendMessage(message: Message) {
        if (!connected) {
            Log.e(TAG, "sendMessage failed: not connected")
            return
        }
//        message.replyTo = responseMessenger
        try {
            messenger!!.send(message)
        } catch (e: RemoteException) {
            Log.e(TAG, "sendMessage failed: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "MethodCallHandlerImpl"
        private const val APPLICATION_PACKAGE_NAME = "com.codegemz.flutlab.installer"
        private const val SERVICE_CLASS_NAME = "com.codegemz.flutlab.installer.log.LogService"
    }
}