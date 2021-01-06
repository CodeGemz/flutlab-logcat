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
import org.json.JSONArray
import org.json.JSONObject
import java.lang.ref.WeakReference


class MethodCallHandlerImpl(private val context: Context) : MethodChannel.MethodCallHandler {
    private val globalExceptionHandler = GlobalExceptionHandler(::onUncaughtException)
    private var messenger: Messenger? = null
    private var connecting = false
    private var connected = false
    private val packageName = context.packageName
    private var initResultFuture: MethodChannel.Result? = null
    private val replyMessenger = Messenger(IncomingHandler(this))

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
                        putString(InstallerConstants.PACKAGE_NAME_KEY, packageName)
                    }
                    sendMessage(message)
                }
            })
            this.logsObserver = logsObserver
            val message = Message.obtain(null, InstallerConstants.INCOMING_MESSAGE_AWAIT_PEER_CONNECTION)
            message.replyTo = replyMessenger
            sendMessage(message)
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
            initResultFuture = result
        } else {
            result.error("bind_result_false", "Bind returned failed", null)
        }
        connecting = false
    }

    private fun onUncaughtException(t: Thread, e: Throwable) {
        val message = Message.obtain(null, InstallerConstants.INCOMING_MESSAGE_UNHANDLED_EXCEPTION)
        message.data = Bundle().apply {
            val exceptionJsonObject = wrapThrowableToJson(e)
            val threadJsonObject = JSONObject().put(InstallerConstants.THREAD_NAME_KEY, t.name)
            val fullJsonObject = JSONObject()
            fullJsonObject.put(InstallerConstants.EXCEPTION_KEY, exceptionJsonObject)
            fullJsonObject.put(InstallerConstants.THREAD_KEY, threadJsonObject)
            putString(InstallerConstants.EXCEPTION_KEY, fullJsonObject.toString())
        }
        Log.e("onUncaughtException", "onUncaughtException")
        sendMessage(message)
    }

    private fun sendMessage(message: Message) {
        if (!connected) {
            Log.e(TAG, "sendMessage failed: not connected")
            return
        }
        try {
            messenger!!.send(message)
        } catch (e: RemoteException) {
            Log.e(TAG, "sendMessage failed: ${e.message}")
        }
    }

    private fun onAwaitPeerConnectionSuccess() {
        val initResultFuture = initResultFuture ?: return
        initResultFuture.success("")
    }

    private fun onAwaitPeerConnectionFailure(errorMessage: String?) {
        val initResultFuture = initResultFuture ?: return
        initResultFuture.error("connection_failure", errorMessage, null)
    }

    private class IncomingHandler(methodCallHandler: MethodCallHandlerImpl) : Handler() {
        private val methodCallHandlerReference = WeakReference<MethodCallHandlerImpl>(methodCallHandler)

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                InstallerConstants.OUTCOMING_MESSAGE_AWAIT_PEER_CONNECTION_RESULT -> {
                    val methodCallHandlerImpl = methodCallHandlerReference.get() ?: return
                    when (msg.data.getInt(InstallerConstants.AWAIT_PEER_CONNECTION_RESULT_KEY, -1)) {
                        InstallerConstants.AWAIT_PEER_CONNECTION_RESULT_SUCCESS -> methodCallHandlerImpl.onAwaitPeerConnectionSuccess()
                        InstallerConstants.AWAIT_PEER_CONNECTION_RESULT_FAILURE -> {
                            val errorMessage = msg.data.getString(InstallerConstants.ERROR_MESSAGE_KEY)
                            methodCallHandlerImpl.onAwaitPeerConnectionFailure(errorMessage)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "MethodCallHandlerImpl"
        private const val APPLICATION_PACKAGE_NAME = "com.codegemz.flutlab.installer"
        private const val SERVICE_CLASS_NAME = "com.codegemz.flutlab.installer.log.LogService"

        fun wrapThrowableToJson(t: Throwable): JSONObject {
            val jsonObject = JSONObject()
            jsonObject.put(InstallerConstants.THROWABLE_CLASS_NAME_KEY, t::class.java.name)
            jsonObject.put(InstallerConstants.THROWABLE_MESSAGE_KEY, t.message)
            val cause = t.cause
            if (cause != null) {
                jsonObject.put(InstallerConstants.THROWABLE_CAUSE_KEY, wrapThrowableToJson(cause))
            }
            val stackTrace = t.stackTrace
            val jsonArray = JSONArray()
            stackTrace.forEach {
                val stackTraceElementJSONObject = JSONObject()
                stackTraceElementJSONObject.put(InstallerConstants.STACK_TRACE_ELEMENT_CLASS_NAME_KEY, it.className)
                stackTraceElementJSONObject.put(InstallerConstants.STACK_TRACE_ELEMENT_METHOD_NAME_KEY, it.methodName)
                stackTraceElementJSONObject.put(InstallerConstants.STACK_TRACE_ELEMENT_FILE_NAME_KEY, it.fileName)
                stackTraceElementJSONObject.put(InstallerConstants.STACK_TRACE_ELEMENT_LINE_NUMBER_KEY, it.lineNumber)
                stackTraceElementJSONObject.put(InstallerConstants.STACK_TRACE_ELEMENT_IS_NATIVE_METHOD_KEY, it.isNativeMethod)
                jsonArray.put(stackTraceElementJSONObject)
            }
            jsonObject.put(InstallerConstants.THROWABLE_STACK_TRACE_KEY, jsonArray)
            return jsonObject
        }
    }
}