#import "AppDelegate.h"
#import "MainViewController.h"
#import "SCBLIFE-Swift.h"

@implementation AppDelegate
@synthesize activeLife;
@synthesize screenID;
@synthesize statusLevel;
@synthesize activityScore;
@synthesize pointsBalance;
@synthesize ongoingChallenges;

- (BOOL)application:(UIApplication*)application didFinishLaunchingWithOptions:(NSDictionary*)launchOptions {
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(reactNativeNavigation:) name:@"reactNativeNavigation" object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(activityScore:) name:@"activityScore" object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(statusLevel:) name:@"statusLevel" object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(pointsBalance:) name:@"pointsBalance" object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(ongoingChallenges:) name:@"ongoingChallenges" object:nil];
    [[HealthKitManager getSharedInstance] startObservingHealthKitStore];
    self.viewController = [[MainViewController alloc] init];
    
     [self setStatusBarBackgroundColor];
    return [super application:application didFinishLaunchingWithOptions:launchOptions];
}

- (void)setStatusBarBackgroundColor{
    UIView *statusBar = [[[UIApplication sharedApplication] valueForKey:@"statusBarWindow"] valueForKey:@"statusBar"];
    UIBlurEffect *blur = [UIBlurEffect effectWithStyle:UIBlurEffectStyleLight];
    UIVisualEffectView *effectView = [[UIVisualEffectView alloc] initWithEffect:blur];
    [effectView setFrame:statusBar.bounds];
    [statusBar addSubview:effectView];
    [statusBar sendSubviewToBack:effectView];
}

- (void)reactNativeNavigation:(NSNotification *)notification {
    screenID = [notification.userInfo objectForKey:@"screenID"];
    NSLog(@">>> AppDelegate reactNativeNavigation: %@",screenID);
}

- (void)activityScore:(NSNotification *)notification {
    activityScore = [notification.userInfo objectForKey:@"score"];
    NSLog(@">>> AppDelegate activityScore: %@",activityScore);
}

- (void)statusLevel:(NSNotification *)notification {
    statusLevel = [notification.userInfo objectForKey:@"level"];
    NSLog(@">>> AppDelegate statusLevel: %@",statusLevel);
}

- (void)pointsBalance:(NSNotification *)notification {
    pointsBalance = [notification.userInfo objectForKey:@"points"];
    NSLog(@">>> AppDelegate pointsBalance: %@",pointsBalance);
}

- (void)ongoingChallenges:(NSNotification *)notification {
    ongoingChallenges = [notification.userInfo objectForKey:@"challenges"];
    NSLog(@">>> AppDelegate ongoingChallenges: %@",ongoingChallenges);
}

@end
