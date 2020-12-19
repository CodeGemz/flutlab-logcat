import 'dart:async';

import 'package:flutter/services.dart';

class FlutLabLogcat {
  static const MethodChannel _channel =
      const MethodChannel('com.codegemz.flutlab/logcat');

  static Future<void> init() async {
    try {
      await _channel.invokeMethod('init');
      print("FlutLabLogcat initialized");
    } on PlatformException catch (e) {
      print("Failed initialize FlutLabLogcat: $e");
    }
  }
}
