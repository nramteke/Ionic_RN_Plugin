package cordova.plugin.reactnative;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.graphics.Point;
import android.view.View;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ReactNative extends CordovaPlugin {


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("startReact")) {
            DisplayMetrics displayMetrics = cordova.getActivity().getResources().getDisplayMetrics();
            cordova.getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int fullScreenHeight = cordova.getActivity().getWindow().getDecorView().getHeight();

            Rect rect = new Rect();
            cordova.getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            int screenHeight = rect.bottom;

            int tabBarHeight = (int)args.get(2);
            // converted to DP for multiple screen resolutions
            // value which is received by ionic need to consider as DP.
            // then using below line it will convert into actual pixel which need to be minus from screen pixel height.
            int tabBarHeightConvertedUsingDP = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, tabBarHeight, displayMetrics );

            // no need to multiply by screen density as above line will convert pixel to dp.
            int usableContentHeight = screenHeight-tabBarHeightConvertedUsingDP;
            int yOffset = -(fullScreenHeight-usableContentHeight)/2;
            Intent intent = new Intent(cordova.getActivity().getApplicationContext(),ReactMainActivity.class);
            if (args.get(0) instanceof JSONObject) {
                Bundle screenID = new Bundle();
                JSONObject jsonObject = (JSONObject)args.get(0);
                for (int i=0;i<jsonObject.names().length();i++) {
                    String key = jsonObject.names().getString(i);
                    String value = jsonObject.getString(key);
                    screenID.putString(key,value);
                }
                intent.putExtra("screenID",screenID);
            } else {
                intent.putExtra("screenID",(String)args.get(0));
            }
            intent.putExtra("userID",(String)args.get(1));
            intent.putExtra("usableContentHeight",usableContentHeight);
            intent.putExtra("yOffset",yOffset);
            cordova.getActivity().startActivity(intent);
        } else if (action.equals("switchReact")) {
            ReactContext context = MainApplication.getAppInstance().getReactNativeHost().getReactInstanceManager().getCurrentReactContext();
            ReactNativeEventEmitter emitter = new ReactNativeEventEmitter((ReactApplicationContext)context);
            if (args.get(0) instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject)args.get(0);
                if (jsonObject != null) {
                    emitter.sendEvent((String)jsonObject.get("ScreenIDLevel1"));
                }
            } else {
                emitter.sendEvent((String)args.get(0));
            }
        } else {
            //to kill Fitsense screen
            Activity whichActivity=MainApplication.getAppInstance().getCurrentActivity();
            if (whichActivity!=null && whichActivity.getLocalClassName().equals("cordova.plugin.reactnative.ReactMainActivity")) {
                MainApplication.getAppInstance().getCurrentActivity().finish();
            }
        }
        return true;
    }
}
