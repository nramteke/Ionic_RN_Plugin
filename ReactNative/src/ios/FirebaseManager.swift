import Foundation
@objc(FirebaseManager)
public class FirebaseManager:NSObject {
    @objc(setPath:)
    func setPath(path:String)->Void {
        NotificationCenter.default.post(name:Notification.Name("reactNativeNavigation"),object:nil,userInfo:["screenID":path])
        print(">>> FirebaseManager setPath: \(path)")
    }
    @objc(fitsenseAnalytics:)
    func fitsenseAnalytics(eventName:String)->Void {
        let events = [
            "tracking_connected":"Click \"บันทึก\" button in Activity - Connect page",
            "tracking_goal_met":"Landing in Activity - Goal met page",
            "tracking_goal_share":"Share goal met to facebook",
            "rewards_view_all":"View All Rewards items",
            "rewards_view_redeem":"View redeemed reward",
            "rewards_view_detail":"View reward detail",
            "rewards_redeemed":"Landing in Rewards receipt page",
            "challenge_listing":"Visit challenge page",
            "challenge_detail":"View challenge detail",
            "challenge_leaderboard":"View leaderboard page",
            "challenge_result":"View result page",
            "challenge_share":"Share challenge to facebook",
            "challenge_share_joined":"Share challenge to facebook after join",
            "challenge_start":"Start challenge popup",
            "challenge_leave":"Leave challenge",
            "challenge_complete":"Complete challenge and get the point",
            "challenge_countdown":"View countdown timer page"
        ]
        print(">>> FirebaseManager fitsenseAnalytics: \(eventName)")
        NotificationCenter.default.post(name:Notification.Name("fitsenseAnalytics"),object:nil,userInfo:["eventName":eventName,"measurableMetric":events[eventName]!])
    }
    @objc(setOnboardingResult:)
    func setOnboardingResult(result:Bool)->Void {
        NotificationCenter.default.post(name:Notification.Name("onboarding"),object:nil,userInfo:["result":result])
        print(">>> FirebaseManager setOnboardingResult: \(result)")
    }
    @objc(setActivityScore:)
    func setActivityScore(score:NSNumber)->Void {
        NotificationCenter.default.post(name:Notification.Name("activityScore"),object:nil,userInfo:["score":score])
        print(">>> FirebaseManager setActivityScore: \(score)")
    }
    @objc(setStatusLevel:)
    func setStatusLevel(level:String)->Void {
        NotificationCenter.default.post(name:Notification.Name("statusLevel"),object:nil,userInfo:["level":level])
        print(">>> FirebaseManager setStatusLevel: \(level)")
    }
    @objc(setPointsBalance:)
    func setPointsBalance(points:NSNumber)->Void {
        NotificationCenter.default.post(name:Notification.Name("pointsBalance"),object:nil,userInfo:["points":points])
        print(">>> FirebaseManager setPointsBalance: \(points)")
    }
    @objc(setOngoingChallenges:)
    func setOngoingChallenges(challenges:NSNumber)->Void {
        NotificationCenter.default.post(name:Notification.Name("ongoingChallenges"),object:nil,userInfo:["challenges":challenges])
        print(">>> FirebaseManager setOngoingChallenges: \(challenges)")
    }
}
