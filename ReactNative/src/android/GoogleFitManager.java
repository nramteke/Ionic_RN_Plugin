package cordova.plugin.reactnative;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.HistoryClient;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class GoogleFitManager extends ReactContextBaseJavaModule {
    private String LOG_TAG = "GoogleFitManager";
    private SharedPreferences sharedPreferences;
    private ReactApplicationContext context;
    private long lastSync = 0;

    public GoogleFitManager(ReactApplicationContext reactContext) {
        super(reactContext);
        context = reactContext;
        reactContext.addActivityEventListener(activityEventListener);
        sharedPreferences = reactContext.getSharedPreferences(LOG_TAG,Context.MODE_PRIVATE);
    }

    @Override
    public String getName() {return LOG_TAG;}

    @ReactMethod
    private void isGoogleFitInstalled(Promise promise) {
        try {
            context.getPackageManager().getPackageInfo("com.google.android.apps.fitness", PackageManager.GET_ACTIVITIES);
            promise.resolve(true);
        } catch (PackageManager.NameNotFoundException e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void setCredentials(String insurerID,String userID,String accessToken) {
        Log.e(LOG_TAG,">>> userID: "+userID);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("insurerID",insurerID);
        editor.putString("userID",userID);
        editor.putString("accessToken",accessToken);
        editor.apply();
        authorizeGoogleFit();
    }

    @ReactMethod
    public void authorizeGoogleFit() {
        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_BASAL_METABOLIC_RATE, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_BASAL_METABOLIC_RATE_SUMMARY, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_LOCATION_SAMPLE, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_ACTIVITY_SAMPLES, FitnessOptions.ACCESS_READ)
                .build();
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(context), fitnessOptions)) {
            if (context.getCurrentActivity() != null) {
                GoogleSignIn.requestPermissions(context.getCurrentActivity(), 1, GoogleSignIn.getLastSignedInAccount(context), fitnessOptions);
            }
        } else {
            accessGoogleFit();
            OnDataPointListener newDataListener = (dataPoint)->{
                for (Field field : dataPoint.getDataType().getFields()) {
                    Log.e(LOG_TAG, ">>> DataPointListener new calorie data: "+field.getName()+" | "+dataPoint.getValue(field));
                }
            };
            Fitness.getSensorsClient(context, GoogleSignIn.getLastSignedInAccount(context))
                    .add(new SensorRequest.Builder()
                            .setDataType(DataType.TYPE_CALORIES_EXPENDED)
                            .setSamplingRate(1, TimeUnit.MINUTES)
                            .build(),newDataListener)
                    .addOnCompleteListener((task)->{
                        if (task.isSuccessful()) {
//                            Log.e(LOG_TAG, ">>> DataPointListener successfully registered");
                        } else {
                            Log.e(LOG_TAG, ">>> DataPointListener not registered", task.getException());
                        }
                    });
        }
    }

    private final ActivityEventListener activityEventListener = new BaseActivityEventListener() {
        @Override
        public void onActivityResult(Activity activity,int requestCode,int resultCode,Intent data) {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == 1) {accessGoogleFit();}
            }
        }
    };

    private void accessGoogleFit() {
        if (System.currentTimeMillis() - lastSync < 10000) {
            return;
        }
        lastSync = System.currentTimeMillis();
        try {context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("Uploading", null);}
        catch(Exception e){}
        Map<String,String> bundleIDs = new HashMap<>();
        bundleIDs.put("com.google.android.gms","google_fit");
        bundleIDs.put("com.xiaomi.hm.health","mi_fit");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfToday = calendar.getTimeInMillis();
        long lastUpload = sharedPreferences.getLong("lastUpload", startOfToday);
        int daysToUpload = (int) TimeUnit.DAYS.convert(startOfToday - lastUpload, TimeUnit.MILLISECONDS) + 1;
        for (int daysAgo = 0; daysAgo < daysToUpload; daysAgo++) {
            retrieveSingleDayActivityData(startOfToday,daysAgo,bundleIDs,calendar);
        }
    }

    private void retrieveSingleDayActivityData(long startOfToday,int daysAgo,Map<String,String> bundleIDs,Calendar calendar) {
        int ONE_DAY = 24 * 60 * 60 * 1000;
        long startTime = startOfToday - daysAgo * ONE_DAY;
        long endTime = startTime + ONE_DAY;
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .bucketByTime(1, TimeUnit.MINUTES)
                .build();
        HistoryClient historyClient = Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context));
        historyClient.readData(readRequest).addOnSuccessListener((dataReadResponse) -> {
            Set<String> dataSources = new HashSet<>();
            ArrayList<Integer> stepsPerMinute = new ArrayList<>();
            ArrayList<Integer> caloriesBMRPerMinute = new ArrayList<>();
            for (Bucket bucket : dataReadResponse.getBuckets()) {
                try {
                    DataPoint dpSteps = bucket.getDataSet(DataType.TYPE_STEP_COUNT_DELTA).getDataPoints().get(0);
                    if (dpSteps.getOriginalDataSource().getAppPackageName() != null) {
                        dataSources.add(bundleIDs.get(dpSteps.getOriginalDataSource().getAppPackageName()));
                    }
                    if (dpSteps.getOriginalDataSource().getStreamName().equals("user_input")) {
                        throw new Exception();
                    }
                    int steps = dpSteps.getValue(dpSteps.getDataType().getFields().get(0)).asInt();
                    stepsPerMinute.add(steps);
                } catch (Exception e) {
                    stepsPerMinute.add(0);
                }
                try {
                    DataPoint dpCalories = bucket.getDataSet(DataType.TYPE_CALORIES_EXPENDED).getDataPoints().get(0);
                    if (dpCalories.getOriginalDataSource().getAppPackageName() != null) {
                        dataSources.add(bundleIDs.get(dpCalories.getOriginalDataSource().getAppPackageName()));
                    }
                    if (dpCalories.getOriginalDataSource().getStreamName().equals("user_input")) {
                        throw new Exception();
                    }
                    Float calories = dpCalories.getValue(dpCalories.getDataType().getFields().get(0)).asFloat();
                    caloriesBMRPerMinute.add((int) (calories * 100));
                } catch (Exception e) {
                    caloriesBMRPerMinute.add(0);
                }
            }
            int bmr = 0;
            for (int i = 0; i < stepsPerMinute.size(); i++) {
                if (stepsPerMinute.get(i) == 0) {
                    bmr = caloriesBMRPerMinute.get(i);
                    break;
                }
            }
            Log.e(LOG_TAG, "BMR: " + bmr);
            ArrayList<Integer> caloriesPerMinute = new ArrayList<>();
            for (int calories : caloriesBMRPerMinute) {
                caloriesPerMinute.add(Math.max(calories - bmr, 0));
            }
            SessionReadRequest readRequestWorkouts = new SessionReadRequest.Builder()
                    .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                    .read(DataType.TYPE_CALORIES_EXPENDED)
                    .read(DataType.TYPE_DISTANCE_DELTA)
                    .readSessionsFromAllApps()
                    .build();
            Fitness.getSessionsClient(context, GoogleSignIn.getLastSignedInAccount(context))
                    .readSession(readRequestWorkouts)
                    .addOnSuccessListener((sessionReadResponse) -> {
                        ArrayList<HashMap<String, Object>> workouts = new ArrayList<>();
                        for (Session session : sessionReadResponse.getSessions()) {
                            calendar.setTime(new Date(session.getStartTime(TimeUnit.MILLISECONDS)));
                            int start = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
                            calendar.setTime(new Date(session.getEndTime(TimeUnit.MILLISECONDS)));
                            int end = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
                            float calories = 0;
                            float distance = 0;
                            boolean manual_user_log = false;
                            for (DataSet dataSet : sessionReadResponse.getDataSet(session)) {
                                if (dataSet.getDataType().equals(DataType.TYPE_CALORIES_EXPENDED)) {
                                    for (DataPoint dataPoint : dataSet.getDataPoints()) {
                                        calories += dataPoint.getValue(dataPoint.getDataType().getFields().get(0)).asFloat();
                                        if (dataPoint.getOriginalDataSource().getStreamName().equals("user_input")) {
                                            manual_user_log = true;
                                        }
                                    }
                                }
                                if (dataSet.getDataType().equals(DataType.TYPE_DISTANCE_DELTA)) {
                                    for (DataPoint dataPoint : dataSet.getDataPoints()) {
                                        distance += dataPoint.getValue(dataPoint.getDataType().getFields().get(0)).asFloat();
                                        if (dataPoint.getOriginalDataSource().getStreamName().equals("user_input")) {
                                            manual_user_log = true;
                                        }
                                    }
                                }
                            }
                            HashMap<String, Object> workout = new HashMap<>();
                            workout.put("start_time", start);
                            workout.put("end_time", end);
                            workout.put("distance", (int) distance);
                            workout.put("calories", (int) calories);
                            workout.put("type", session.getActivity());
                            workout.put("bundle_identifier", session.getAppPackageName());
                            if (session.hasActiveTime()) {
                                workout.put("duration", session.getActiveTime(TimeUnit.SECONDS));
                            }
                            if (manual_user_log) {
                                workout.put("manual_user_log", true);
                            }
                            workouts.add(workout);
                            Log.e(LOG_TAG, "google_fit workout: " + workout);
                        }
                        Log.e(LOG_TAG, "dataSources: " + dataSources);
                        ActivityDataUploader.upload(stepsPerMinute, caloriesPerMinute, workouts, sharedPreferences, context, "google_fit", dataSources, startTime, startOfToday);
                    });
        });
    }
}