#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
@interface ReactNativeEventEmitter:RCTEventEmitter<RCTBridgeModule>
- (void)sendEvent:(NSString*)screenID;
@end
