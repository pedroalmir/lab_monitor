import 'dart:async';

import 'package:flutter/services.dart';

class ClientWithProtobuf {
  static const MethodChannel _channel =
      const MethodChannel('client_with_protobuf');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
