package cordova.plugin.reactnative;

import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class ReactNativeEventEmitter extends ReactContextBaseJavaModule {
    private String LOG_TAG = "ReactNativeEventEmitter";
    private ReactApplicationContext context;
    public ReactNativeEventEmitter(ReactApplicationContext reactContext) {
        super(reactContext);
        context = reactContext;
    }
    @Override
    public String getName(){return LOG_TAG;}
    @ReactMethod
    void sendEvent(String screenID) {
        Log.e(LOG_TAG,">>> sendEvent: "+screenID);
        WritableMap params = Arguments.createMap();
        params.putString("screenID",screenID);
        if (context.hasActiveCatalystInstance()) {
            context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("navigation", params);
        } else {
            Log.e(LOG_TAG,">>> Could not navigate to "+screenID+" since no active React Native instance could be found.");
        }
    }
}
