package cordova.plugin.reactnative;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.WindowManager;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;

import javax.annotation.Nullable;

public class ReactMainActivity extends ReactActivity {

    String screenID;
    String statusLevel;
    Integer activityScore;
    Integer pointsBalance;
    Integer ongoingChallenges;

    @Nullable
    @Override
    protected String getMainComponentName() {
        return "ActiveLife";
    }

    @Override
    protected ReactActivityDelegate createReactActivityDelegate() {
        return new ReactActivityDelegate(this, getMainComponentName()) {
            @Override
            protected Bundle getLaunchOptions() {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.height = getIntent().getExtras().getInt("usableContentHeight");
                params.y = getIntent().getExtras().getInt("yOffset");
                getWindow().setAttributes(params);
                Bundle initialProps = new Bundle();
                if (getIntent().getBundleExtra("screenID") != null) {
                    initialProps.putBundle("screenID",getIntent().getBundleExtra("screenID"));
                }
                if (getIntent().getStringExtra("screenID") != null) {
                    initialProps.putString("screenID",getIntent().getStringExtra("screenID"));
                }
                initialProps.putString("userID",getIntent().getStringExtra("userID"));
                return initialProps;
            }
        };
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        finish();
    }

    public void onDestroy() {
        super.onDestroy();
        Log.e("ReactMainActivity",">>> onDestroy: "+screenID+" "+statusLevel+" "+activityScore+" "+pointsBalance+" "+ongoingChallenges);

        final Intent screenIDIntent = new Intent("reactNativeNavigation");
        Bundle screenIDBundle = new Bundle();
        screenIDBundle.putString("screenID",screenID);
        screenIDIntent.putExtras(screenIDBundle);
        LocalBroadcastManager.getInstance(this).sendBroadcastSync(screenIDIntent);

        final Intent userStatusIntent = new Intent("userStatus");
        Bundle userStatusBundle = new Bundle();
        if (statusLevel != null) {userStatusBundle.putString("statusLevel",statusLevel);}
        if (activityScore != null) {userStatusBundle.putInt("activityScore",activityScore);}
        if (pointsBalance != null) {userStatusBundle.putInt("pointsBalance",pointsBalance);}
        if (ongoingChallenges != null) {userStatusBundle.putInt("ongoingChallenges",ongoingChallenges);}
        screenIDIntent.putExtras(userStatusBundle);
        LocalBroadcastManager.getInstance(this).sendBroadcastSync(userStatusIntent);
    }
}
