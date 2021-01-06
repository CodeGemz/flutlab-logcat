import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

import 'package:flutter/foundation.dart' show kIsWeb;

class FlutLabLogcat {
  static const MethodChannel _channel =
      const MethodChannel('com.codegemz.flutlab/logcat');

  static Future<void> init() async {
    if (kIsWeb) {
      return;
    }
    if (!Platform.isAndroid) {
      return;
    }
    try {
      await _channel.invokeMethod('init');
      print("FlutLabLogcat initialized");
    } on PlatformException catch (e) {
      print("Failed initialize FlutLabLogcat: $e");
    }
  }

  static Future<void> throwNativeCrash() {
    if (kIsWeb) {
      return;
    }
    if (!Platform.isAndroid) {
      return;
    }
    return _channel.invokeMethod('throwNativeCrash');
  }
}
