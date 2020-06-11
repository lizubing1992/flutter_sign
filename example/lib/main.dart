import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:sign/sign.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  SignResult _signResult;

  @override
  void initState() {
    super.initState();
//    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      SignResult result = await FlutterSign.initSign;

      platformVersion = result.success ? "联网成功" : "联网失败";
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    ///测试使用
    if (_signResult != null) {
      print("信手签：${_signResult.description}");

      print(_signResult.images.toString());
    }
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('信手签'),
        ),
        body: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            RaisedButton(
              onPressed: () async {
                SignResult result = await FlutterSign.initSign;
              },
              child: Text("初始化信手签"),
            ),
            Center(
              child: _signResult == null
                  ? Text('Running on: $_platformVersion\n')
                  : Container(
                      color: Colors.white,
                      child: Image.memory(
                        _signResult.getImageBest(),
                      )),
            )
          ],
        ),
        floatingActionButton: FloatingActionButton(
          onPressed: () async {
            _signResult = await FlutterSign.startSign();
            setState(() {});
          },
        ),
      ),
    );
  }
}
