#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(HealthKitManager,RCTEventEmitter)
RCT_EXTERN_METHOD(setCredentials:(NSString*)insurerID userID:(NSString*)userID accessToken:(NSString*)accessToken)
RCT_EXTERN_METHOD(startObservingHealthKitStore)
RCT_EXTERN_METHOD(supportedEvents)
@end
