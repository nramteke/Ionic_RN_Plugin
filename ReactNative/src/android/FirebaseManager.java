package cordova.plugin.reactnative;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.util.HashMap;

public class FirebaseManager extends ReactContextBaseJavaModule {
    private String LOG_TAG = "FirebaseManager";
    private ReactApplicationContext context;
    public FirebaseManager(ReactApplicationContext reactContext) {
        super(reactContext);
        context = reactContext;
    }
    @Override
    public String getName(){return LOG_TAG;}
    @ReactMethod
    public void setPath(String path) {
        Log.e(LOG_TAG,">>> setPath: "+path);
        final Intent intent = new Intent("reactNativeNavigation");
        Bundle b = new Bundle();
        b.putString("screenID",path);
        intent.putExtras(b);
        LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
        Activity whichActivity = MainApplication.getAppInstance().getCurrentActivity();
        if (whichActivity != null && whichActivity.getLocalClassName().equals("cordova.plugin.reactnative.ReactMainActivity")) {
            ReactMainActivity reactMainActivity = (ReactMainActivity)whichActivity;
            reactMainActivity.screenID = path;
        }
    }
    @ReactMethod
    public void fitsenseAnalytics(String eventName) {
        Log.e(LOG_TAG,">>> fitsenseAnalytics: "+eventName);
        HashMap<String,String> events = new HashMap<>();
        events.put("tracking_connected","Click \"บันทึก\" button in Activity - Connect page");
        events.put("tracking_goal_met","Landing in Activity - Goal met page");
        events.put("tracking_goal_share","Share goal met to facebook");
        events.put("rewards_view_all","View All Rewards items");
        events.put("rewards_view_redeem","View redeemed reward");
        events.put("rewards_view_detail","View reward detail");
        events.put("rewards_redeemed","Landing in Rewards receipt page");
        events.put("challenge_listing","Visit challenge page");
        events.put("challenge_detail","View challenge detail");
        events.put("challenge_leaderboard","View leaderboard page");
        events.put("challenge_result","View result page");
        events.put("challenge_share","Share challenge to facebook");
        events.put("challenge_share_joined","Share challenge to facebook");
        events.put("challenge_start","Start challenge popup");
        events.put("challenge_leave","Leave challenge");
        events.put("challenge_complete","Complete challenge and get the point");
        final Intent intent = new Intent("fitsenseAnalytics");
        Bundle b = new Bundle();
        b.putString("eventName",eventName);
        b.putString("measurableMetric",events.get(eventName));
        intent.putExtras(b);
        LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
    }
    @ReactMethod
    public void setOnboardingResult(boolean result) {
        Log.e(LOG_TAG,">>> setOnboardingResult: "+result);
        final Intent intent = new Intent("onboarding");
        Bundle b = new Bundle();
        b.putBoolean("result",result);
        intent.putExtras(b);
        LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent);
    }
    @ReactMethod
    public void setActivityScore(Integer score) {
        Activity whichActivity = MainApplication.getAppInstance().getCurrentActivity();
        if (whichActivity != null && whichActivity.getLocalClassName().equals("cordova.plugin.reactnative.ReactMainActivity")) {
            ReactMainActivity reactMainActivity = (ReactMainActivity)whichActivity;
            reactMainActivity.activityScore = score;
            Log.e(LOG_TAG,">>> setActivityScore: "+score);
        }
    }
    @ReactMethod
    public void setStatusLevel(String level) {
        Activity whichActivity = MainApplication.getAppInstance().getCurrentActivity();
        if (whichActivity != null && whichActivity.getLocalClassName().equals("cordova.plugin.reactnative.ReactMainActivity")) {
            ReactMainActivity reactMainActivity = (ReactMainActivity)whichActivity;
            reactMainActivity.statusLevel = level;
            Log.e(LOG_TAG,">>> setStatusLevel: "+level);
        }
    }
    @ReactMethod
    public void setPointsBalance(Integer points) {
        Activity whichActivity = MainApplication.getAppInstance().getCurrentActivity();
        if (whichActivity != null && whichActivity.getLocalClassName().equals("cordova.plugin.reactnative.ReactMainActivity")) {
            ReactMainActivity reactMainActivity = (ReactMainActivity)whichActivity;
            reactMainActivity.pointsBalance = points;
            Log.e(LOG_TAG,">>> setPointsBalance: "+points);
        }
    }
    @ReactMethod
    public void setOngoingChallenges(Integer challenges) {
        Activity whichActivity = MainApplication.getAppInstance().getCurrentActivity();
        if (whichActivity != null && whichActivity.getLocalClassName().equals("cordova.plugin.reactnative.ReactMainActivity")) {
            ReactMainActivity reactMainActivity = (ReactMainActivity)whichActivity;
            reactMainActivity.ongoingChallenges = challenges;
            Log.e(LOG_TAG,">>> setOngoingChallenges: "+challenges);
        }
    }
}
