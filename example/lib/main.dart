import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutlab_logcat/flutlab_logcat.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  FlutLabLogcat.init();
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Text('Running...'),
        ),
      ),
    );
  }
}
