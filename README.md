# Flutter Logcat plugin

## How to use:

You can initialize the plugin in 2 ways: synchronously and asynchronously
Using the synchronous method the app will wait for at least one connection

To run synchronously
```dart
import 'package:flutlab_logcat/flutlab_logcat.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await FlutLabLogcat.init();
  runApp(MyApp());
}
```

To run asynchronously
```dart
import 'package:flutlab_logcat/flutlab_logcat.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  FlutLabLogcat.init(); // async call
  runApp(MyApp());
}
```
