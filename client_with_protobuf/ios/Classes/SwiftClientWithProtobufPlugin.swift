import Flutter
import UIKit

public class SwiftClientWithProtobufPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "client_with_protobuf", binaryMessenger: registrar.messenger())
    let instance = SwiftClientWithProtobufPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    result("iOS " + UIDevice.current.systemVersion)
  }
}
