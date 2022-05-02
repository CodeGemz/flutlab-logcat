import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

import 'package:flutter/foundation.dart' show kIsWeb;

class FlutLabLogcat {
  static const MethodChannel _channel =
      const MethodChannel('com.flutlab/logcat');

  static bool get isSupports {
    return Platform.isAndroid;
  }

  static Future<bool> init() async {
    if (!isSupports) {
      return false;
    }
    try {
      await _channel.invokeMethod('init');
      print("FlutLabLogcat initialized");
      return true;
    } on PlatformException catch (e) {
      print("Failed initialize FlutLabLogcat: $e");
    }
    return false;
  }

  static Future<void> throwNativeCrash() {
    if (kIsWeb) {
      return Future.value();
    }
    if (!Platform.isAndroid) {
      return Future.value();
    }
    return _channel.invokeMethod('throwNativeCrash');
  }
}
