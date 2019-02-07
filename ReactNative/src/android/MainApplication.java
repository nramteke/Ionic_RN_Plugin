package cordova.plugin.reactnative;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import com.BV.LinearGradient.LinearGradientPackage;
import com.babisoft.ReactNativeLocalization.ReactNativeLocalizationPackage;
import fr.bamlab.rnimageresizer.ImageResizerPackage;

import com.facebook.FacebookSdk;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;
import com.facebook.CallbackManager;
import com.facebook.reactnative.androidsdk.FBSDKPackage;
import com.learnium.RNDeviceInfo.RNDeviceInfo;
import com.imagepicker.ImagePickerPackage;
import android.support.multidex.MultiDex;
import android.content.Context;
import java.util.Arrays;
import java.util.List;

public class MainApplication extends Application implements ReactApplication,Application.ActivityLifecycleCallbacks {

  private static MainApplication instance;

  public static MainApplication getAppInstance() { return instance; }

  //to provide the current activity
  private Activity currentActivity;
  
  private static CallbackManager mCallbackManager = CallbackManager.Factory.create();
  protected static CallbackManager getCallbackManager() { return mCallbackManager; }

  private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
    @Override
    public boolean getUseDeveloperSupport() {
      return true;
    }

    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
              new MainReactPackage(),
              new IonicReactPackage(),
              new ReactNativeLocalizationPackage(),
              new LinearGradientPackage(),
				  new ImagePickerPackage(),
				  new ImageResizerPackage(),
              new RNDeviceInfo(),
              new FBSDKPackage(mCallbackManager)
      );
    }

    @Override
    protected String getJSMainModuleName() {
      return "index";
    }
  };

  @Override
  public ReactNativeHost getReactNativeHost() {
    return mReactNativeHost;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    instance=this;
    SoLoader.init(this, /* native exopackage */ false);
    registerActivityLifecycleCallbacks(this);
    FacebookSdk.setApplicationId("1211974458842671");
    FacebookSdk.sdkInitialize(getApplicationContext());
  }

   protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

  @Override
  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

  }

  @Override
  public void onActivityStarted(Activity activity) {
  }

  @Override
  public void onActivityResumed(Activity activity) {
    //to capture current Activity
    this.currentActivity=activity;
  }

  @Override
  public void onActivityPaused(Activity activity) {

  }

  @Override
  public void onActivityStopped(Activity activity) {
  }

  @Override
  public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
  }

  @Override
  public void onActivityDestroyed(Activity activity) {
  }

  //this method is used in for getting the current fitsene screen to kill the screen upon SCBLife tab button click  
  public Activity getCurrentActivity() {
    return currentActivity;
  }

}
