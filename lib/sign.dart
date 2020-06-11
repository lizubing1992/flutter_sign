import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/services.dart';

class FlutterSign {
  static const MethodChannel _channel = const MethodChannel('flutter_sign');

  /*
  * 联网授权
  * */
  static Future<SignResult> get initSign async {
    final dynamic result = await _channel.invokeMethod('initSign');
    return SignResult.fromMap(result);
  }

  /*
  * 启动检测器
  * detectionWithMovier:活体检测中是否开启录像
  *     必须启动检测器之前设置，启动检测器之后是无法在开启录像的，默认 关闭
  * detectionWithSound:活体检测中是否开启录制声音
  *     必须启动检测器之前设置，启动检测器之后是无法在开启的，并且需要开启录像，设置才会有用。默认 关闭，
  * actionCount:设置活体动作数量，最多4个，最少1个，默认3个
  * actionTimeOut:每个活动动作的超时时间，单位秒， 默认10秒
  * randomAction:是否动作为随机 默认 YES(随机)
  * */
  static Future<SignResult> startSign({
    bool detectionWithMovier = false,
    bool detectionWithSound = false,
    int actionCount = 2,
    int actionTimeOut = 10,
    bool randomAction = true,
  }) async {
    final dynamic result = await _channel.invokeMethod('startSign'/*, {
      "detectionWithMovier": detectionWithMovier,
      "detectionWithSound": detectionWithSound,
      "actionCount": actionCount,
      "actionTimeOut": actionTimeOut,
      "randomAction": randomAction
    }*/);
    return SignResult.fromMap(result);
  }
}

class SignResult {
  ///活体识别是否成功
  final bool success;

  ///活体检测中输入的图片
  final Map<String, Object> images;

  ///识别成功才有：用于数据校验的字符串
  final String delta;

  ///失败的错误编码
  final String errorCode;

  ///失败的文本说明
  final String description;

  SignResult({
    this.success,
    this.images,
    this.delta,
    this.errorCode,
    this.description,
  });

  static SignResult fromMap(dynamic map) {
    if (map == null) {
      return null;
    }

    var fromImages = map["images"];

    final Map<String, Object> toImages = <String, Object>{};

    if (fromImages != null) {
      for (String key in fromImages.keys) {
        toImages[key] = fromImages[key];
      }
    }

    return SignResult(
      success: map["success"],
      delta: map["delta"],
      images: toImages,
      errorCode: map["errorCode"],
      description: map["description"],
    );
  }

  ///获取image_best
  Uint8List getImageBest() {
    return images["image_best"] ?? Uint8List(0);
  }


  @override
  String toString() {
    return '{"success" = $success, "images" = $images, "delta" = $delta, "errorCode" = $errorCode, "description" = $description}';
  }
}
