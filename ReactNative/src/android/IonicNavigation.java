package cordova.plugin.reactnative;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class IonicNavigation extends ReactContextBaseJavaModule {
    private String LOG_TAG = "ReactNative";
    private ReactApplicationContext context;
    public IonicNavigation(ReactApplicationContext reactContext) {
        super(reactContext);
        context = reactContext;
    }
    @Override
    public String getName() {
        return LOG_TAG;
    }
    @ReactMethod
    public void openSCBLife(String screenID) {
        Log.e(LOG_TAG,">>> openSCBLife: "+screenID);
        Intent intent1 = new Intent("ionicNavigation");
        Bundle bundle = new Bundle();
        bundle.putString("screenID",screenID);
        intent1.putExtras(bundle);
        LocalBroadcastManager.getInstance(context).sendBroadcastSync(intent1);
        Intent intent2 = new Intent(context, ReactMainActivity.class);
        intent2.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.getCurrentActivity().startActivity(intent2);
    }
}
