package cordova.plugin.reactnative;

import android.content.Intent;
import android.net.Uri;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class ExternalAppLauncher extends ReactContextBaseJavaModule {
    private ReactApplicationContext context;
    ExternalAppLauncher(ReactApplicationContext reactContext) {
        super(reactContext);
        context = reactContext;
    }
    @Override
    public String getName() {return "ExternalAppLauncher";}
    @ReactMethod
    public void launchExternalApp(String bundleID) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(bundleID);
        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+bundleID)));
        }
    }
}