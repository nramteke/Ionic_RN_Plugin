#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
@interface RCT_EXTERN_MODULE(FirebaseManager,NSObject)
RCT_EXTERN_METHOD(setPath:(NSString*)path)
RCT_EXTERN_METHOD(fitsenseAnalytics:(NSString*)eventName)
RCT_EXTERN_METHOD(setOnboardingResult:(BOOL)result)
RCT_EXTERN_METHOD(setActivityScore:(nonnull NSNumber*)score)
RCT_EXTERN_METHOD(setStatusLevel:(NSString*)level)
RCT_EXTERN_METHOD(setPointsBalance:(nonnull NSNumber*)points)
RCT_EXTERN_METHOD(setOngoingChallenges:(nonnull NSNumber*)challenges)
@end
