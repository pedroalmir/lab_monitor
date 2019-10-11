#import "ClientWithProtobufPlugin.h"
#import <client_with_protobuf/client_with_protobuf-Swift.h>

@implementation ClientWithProtobufPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftClientWithProtobufPlugin registerWithRegistrar:registrar];
}
@end
