#import "ReactNativeEventEmitter.h"
@implementation ReactNativeEventEmitter
RCT_EXPORT_MODULE();

+ (id)allocWithZone:(NSZone *)zone {
    static ReactNativeEventEmitter *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [super allocWithZone:zone];
    });
    return sharedInstance;
}

- (NSArray<NSString *> *)supportedEvents {return @[@"navigation"];}

- (void)sendEvent:(NSString*)screenID {
    NSLog(@">>> ReactNativeEventEmitter screenID %@",screenID);
    if (self.bridge != NULL) {[self sendEventWithName:@"navigation" body:@{@"screenID":screenID}];}
    else {NSLog(@">>> ReactNativeEventEmitter bridge not ready to navigate to %@",screenID);}
}
@end
