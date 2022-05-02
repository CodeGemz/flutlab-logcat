import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutlab_logcat/flutlab_logcat.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  // If you want to wait while LogCat will connect to FlutLab than use async approach 'await FlutLabLogcat.init();'
  await FlutLabLogcat.init();
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  MyApp({Key? key}) : super(key: key);

  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('FlutLab Logcat plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              Text(FlutLabLogcat.isSupports
                  ? 'The app is running successfully'
                  : 'FlutLab Logcat running on non-supported platform'),
              ElevatedButton(
                onPressed: () => FlutLabLogcat.throwNativeCrash(),
                child: Text("Throw native exception"),
              )
            ],
          ),
        ),
      ),
    );
  }
}
