package cordova.plugin.reactnative;

import android.content.SharedPreferences;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import th.co.scblife.easy.BuildConfig;

public class ActivityDataUploader {
    private static String LOG_TAG = "ActivityDataUploader";
    public static void upload(
            ArrayList<Integer> stepsPerMinute,
            ArrayList<Integer> caloriesPerMinute,
            ArrayList<HashMap<String,Object>> workouts,
            SharedPreferences sharedPreferences,
            ReactApplicationContext context,
            String dataSource,
            Set<String> dataSources,
            long dateTimestamp,
            long lastUploadTimestamp) {
        Thread thread = new Thread(()->{
            String date = (new SimpleDateFormat("yyyyMMdd",Locale.ENGLISH)).format(dateTimestamp);
            try {
                URL url = new URL("https://scblife.api.activelife.io/activity_data_upload");
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type","application/json");
                conn.setRequestProperty("Insurer-ID",sharedPreferences.getString("insurerID",""));
                conn.setRequestProperty("User-ID",sharedPreferences.getString("userID",""));
                conn.setRequestProperty("Access-Token",sharedPreferences.getString("accessToken",""));
                conn.setRequestProperty("Language",Locale.getDefault().getLanguage());
                conn.setRequestProperty("Timezone-ID",TimeZone.getDefault().getID());
                conn.setRequestProperty("Version",BuildConfig.VERSION_NAME);
                conn.setRequestProperty("Build",""+BuildConfig.VERSION_CODE);
                conn.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("data_source",dataSource);
                jsonParam.put("date",date);

                JSONArray data_sources = new JSONArray();
                for (String item : dataSources) {
                    data_sources.put(item);
                }
                jsonParam.put("data_sources",data_sources);

                JSONArray workoutsJSON = new JSONArray();
                for (Map<String,Object> workout : workouts) {
                    JSONObject workoutJSON = new JSONObject();
                    for (String key : workout.keySet()) {
                        workoutJSON.put(key,workout.get(key));
                    }
                    workoutsJSON.put(workoutJSON);
                }
                jsonParam.put("workouts",workoutsJSON);

                JSONArray per_minute_steps = new JSONArray();
                for (int value : stepsPerMinute) {per_minute_steps.put(value);}
                jsonParam.put("per_minute_steps",per_minute_steps);

                JSONArray per_minute_calories = new JSONArray();
                for (int value : caloriesPerMinute) {per_minute_calories.put(value);}
                jsonParam.put("per_minute_calories",per_minute_calories);

                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(jsonParam.toString());
                os.flush();
                os.close();
                Log.e(LOG_TAG,"Done uploading activity data for "+dataSource+" on "+date+": "+conn.getResponseCode());
                if (conn.getResponseCode() == 200) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putLong("lastUpload",lastUploadTimestamp);
                    editor.apply();
                }
                conn.disconnect();
            } catch (Exception e) {
                String reason = "";
                if (e instanceof UnknownHostException) {reason = "Offline";}
                if (e instanceof SocketTimeoutException) {reason = "TimeOut";}
                context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(reason,null);
                Log.e(LOG_TAG,"Error uploading activity data for "+dataSource+" on "+date+": "+e);
            }
        });
        thread.start();
    }
}