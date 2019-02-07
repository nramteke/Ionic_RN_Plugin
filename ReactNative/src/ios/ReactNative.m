#import <Cordova/CDV.h>
#import <React/RCTBundleURLProvider.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTRootView.h>
#import "AppDelegate.h"
#import "ReactNativeEventEmitter.h"

@interface ReactNative : CDVPlugin <RCTBridgeModule> {}

- (void)startReact:(CDVInvokedUrlCommand*)command;
@end

@implementation ReactNative

RCT_EXPORT_MODULE();
RCT_EXPORT_METHOD(openSCBLife:(NSString*)screenID) {
    dispatch_async(dispatch_get_main_queue(),^{
        AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
        [appDelegate.activeLife.view removeFromSuperview];
        [[NSNotificationCenter defaultCenter] postNotificationName:@"ionicNavigation" object:nil userInfo:@{@"screenID":screenID}];
        [[NSNotificationCenter defaultCenter] postNotificationName:@"reactNativeNavigation" object:nil userInfo:@{@"screenID":appDelegate.screenID}];
        NSMutableDictionary *userStatus = [NSMutableDictionary dictionary];
        if (appDelegate.statusLevel != nil) {[userStatus setObject:appDelegate.statusLevel forKey:@"statusLevel"];}
        if (appDelegate.activityScore != nil) {[userStatus setObject:appDelegate.activityScore forKey:@"activityScore"];}
        if (appDelegate.pointsBalance != nil) {[userStatus setObject:appDelegate.pointsBalance forKey:@"pointsBalance"];}
        if (appDelegate.ongoingChallenges != nil) {[userStatus setObject:appDelegate.ongoingChallenges forKey:@"ongoingChallenges"];}
        [[NSNotificationCenter defaultCenter] postNotificationName:@"userStatus" object:nil userInfo:userStatus];
        NSLog(@">>> openSCBLife: %@ | %@",screenID,userStatus);
    });
}

- (void)startReact:(CDVInvokedUrlCommand*)command {
    CDVPluginResult* pluginResult = nil;
    NSDictionary *props = @{@"screenID":[command.arguments objectAtIndex:0],@"userID":[command.arguments objectAtIndex:1]};
    
    NSURL *jsCodeLocation = [[RCTBundleURLProvider sharedSettings] jsBundleURLForBundleRoot:@"index" fallbackResource:nil];
    RCTRootView *rootView = [[RCTRootView alloc] initWithBundleURL:jsCodeLocation moduleName:@"ActiveLife" initialProperties:props launchOptions:nil];
    rootView.backgroundColor = UIColor.whiteColor;
    [rootView setClipsToBounds:YES];
    AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
    appDelegate.activeLife = [UIViewController new];
    appDelegate.activeLife.view = rootView;
    
    CGRect screen = [[UIScreen mainScreen] bounds];
    int height = screen.size.height-[[command.arguments objectAtIndex:2] intValue];
    [appDelegate.activeLife.view setFrame:CGRectMake(0,0,screen.size.width,height)];
    [self.viewController.view addSubview:appDelegate.activeLife.view];
    
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"echo"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


- (void)endReact:(CDVInvokedUrlCommand*)command {
    dispatch_async(dispatch_get_main_queue(),^{
        AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
        [appDelegate.activeLife.view removeFromSuperview];
        if(appDelegate.screenID){
        [[NSNotificationCenter defaultCenter] postNotificationName:@"reactNativeNavigation" object:nil userInfo:@{@"screenID":appDelegate.screenID}];
        NSMutableDictionary *userStatus = [NSMutableDictionary dictionary];
        if (appDelegate.statusLevel != nil) {[userStatus setObject:appDelegate.statusLevel forKey:@"statusLevel"];}
        if (appDelegate.activityScore != nil) {[userStatus setObject:appDelegate.activityScore forKey:@"activityScore"];}
        if (appDelegate.pointsBalance != nil) {[userStatus setObject:appDelegate.pointsBalance forKey:@"pointsBalance"];}
        if (appDelegate.ongoingChallenges != nil) {[userStatus setObject:appDelegate.ongoingChallenges forKey:@"ongoingChallenges"];}
        [[NSNotificationCenter defaultCenter] postNotificationName:@"userStatus" object:nil userInfo:userStatus];
        }
    });
}

- (void)switchReact:(CDVInvokedUrlCommand*)command {
    if([[command.arguments objectAtIndex:0] isKindOfClass:[NSString class]])
        [[ReactNativeEventEmitter allocWithZone:nil] sendEvent:[command.arguments objectAtIndex:0]];
    else
        [[ReactNativeEventEmitter allocWithZone:nil] sendEvent:[command.arguments objectAtIndex:0][@"ScreenIDLevel1"]];
}

@end
