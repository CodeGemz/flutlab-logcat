package com.codegemz.flutlab.logcat

import android.content.Context
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry.Registrar

class FlutLabLogcatPlugin : FlutterPlugin {
  private var channel: MethodChannel? = null

  override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    setupChannel(binding.binaryMessenger, binding.applicationContext);
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    teardownChannel()
  }

  private fun setupChannel(messenger: BinaryMessenger, context: Context) {
    channel = MethodChannel(messenger, CHANNEL).apply {
      setMethodCallHandler(MethodCallHandlerImpl(context))
    }
  }

  private fun teardownChannel() {
    channel?.setMethodCallHandler(null)
    channel = null
  }

  companion object {
    private const val CHANNEL = "com.codegemz.flutlab/logcat"
  }
}