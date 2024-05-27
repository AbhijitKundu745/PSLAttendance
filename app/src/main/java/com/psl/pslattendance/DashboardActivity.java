package com.psl.pslattendance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.psl.pslattendance.databinding.ActivityDashboardBinding;
import com.psl.pslattendance.helper.ApiConstants;
import com.psl.pslattendance.helper.AssetUtils;
import com.psl.pslattendance.helper.SharedPreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class DashboardActivity extends AppCompatActivity {
private Context context = this;

    private static final int REQUEST_LOCATION_PERMISSION = 200;
    private boolean LocationPermissionGranted = false;
ActivityDashboardBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_dashboard);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard);
        if(!SharedPreferenceManager.getLocationPermission(context)){
            // Request location permission
            requestLocationPermission();
        }
        binding.btnpnchIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Intent intent = new Intent(DashboardActivity.this, PunchInActivity.class);
                    startActivity(intent);

            }
        });
        binding.btnpnchOUT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Intent intent1 = new Intent(DashboardActivity.this, PunchOutActivity.class);
                    startActivity(intent1);

            }
        });
        binding.userGreet.setText("Hello, "+ SharedPreferenceManager.getUserFirstname(context));

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Set the date and time as the text of the TextView
                binding.dateTime.setText(AssetUtils.getSystemDateTimeInFormatt());
                getActivityDetails();

                // Schedule the Runnable to run again after 5 seconds
                handler.postDelayed(this, 5000);
            }
        };
        handler.postDelayed(runnable, 5000);
    }
    private void requestLocationPermission(){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            LocationPermissionGranted = true;
            SharedPreferenceManager.setLocationPermission(context, true);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                LocationPermissionGranted = true;
                SharedPreferenceManager.setLocationPermission(context, true);
            } else {
                // Permission denied
                LocationPermissionGranted = false;
                SharedPreferenceManager.setLocationPermission(context, false);
            }
        }
    }
private void getActivityDetails(){
        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(ApiConstants.K_USER_ID, SharedPreferenceManager.getUserId(context));
            jsonObject.put(ApiConstants.K_DEVICE_ID, SharedPreferenceManager.getDeviceId(context));

            OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(ApiConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(ApiConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(ApiConstants.API_TIMEOUT, TimeUnit.SECONDS)
                    .build();
            AndroidNetworking.post(ApiConstants.URL + ApiConstants.M_GET_ACTIVITY).addJSONObjectBody(jsonObject)
                    .setTag("test")
                    .setPriority(Priority.LOW)
                    .setOkHttpClient(okHttpClient) // passing a custom okHttpClient
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject result) {
                            if (result != null) {
                                try {
                                    Log.e("FetchRes", result.toString());
                                    String status = result.getString(ApiConstants.K_STATUS).trim();
                                    String message = result.getString(ApiConstants.K_MESSAGE).trim();
                                    if (status.equalsIgnoreCase("true")) {
                                        if (result.has(ApiConstants.K_DATA)) {

                                            JSONArray dataArray = result.getJSONArray(ApiConstants.K_DATA);
                                            if (dataArray.length() > 0){
                                                binding.linearReport.setVisibility(View.VISIBLE);
                                                for (int i = 0; i < dataArray.length(); i++) {
                                                    JSONObject dataObject = dataArray.getJSONObject(i);
                                                    String Type = dataObject.getString(ApiConstants.K_ACTIVITY_TYPE);
                                                    String ActivityDateTime = dataObject.getString(ApiConstants.K_PUNCH_DATE_TIME);
                                                    setDashboardActivity(Type, ActivityDateTime);
                                                }
                                            }
                                        }
                                    } else {
                                        AssetUtils.showAlertDialog(context, "", message);
                                    }
                                } catch (JSONException e) {
                                    AssetUtils.showAlertDialog(context, "", getResources().getString(R.string.something_went_wrong_error));
                                }
                            } else {
                                AssetUtils.showAlertDialog(context, "", getResources().getString(R.string.communication_error));
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.e("ERROR", anError.getErrorDetail());
                            if (anError.getErrorDetail().equalsIgnoreCase("responseFromServerError")) {
                                AssetUtils.showAlertDialog(context, "", getResources().getString(R.string.communication_error));
                            } else if (anError.getErrorDetail().equalsIgnoreCase("connectionError")) {
                                AssetUtils.showAlertDialog(context, "", getResources().getString(R.string.internet_error));
                            } else {
                                AssetUtils.showAlertDialog(context, "", getResources().getString(R.string.internet_error));
                            }
                        }
                    });
        }
        catch (JSONException e){

        }
}
String InTime = null;
String OutTime = null;
private void  setDashboardActivity(String Type, String ActivityDateTime){
        binding.textDate.setText(AssetUtils.getDate(ActivityDateTime));
        binding.textDay.setText(AssetUtils.getDayOfWeek(ActivityDateTime));
        if(Type.equalsIgnoreCase("IN")){
            binding.textInDate.setText(AssetUtils.getTime(ActivityDateTime));
            InTime = AssetUtils.getTime(ActivityDateTime);
        }
        else if(Type.equalsIgnoreCase("OUT")){
            binding.textOutDate.setText(AssetUtils.getTime(ActivityDateTime));
            OutTime = AssetUtils.getTime(ActivityDateTime);
        }
    if (InTime != null && OutTime != null) {
        boolean isDifferenceGreaterThanEightHours = isTimeDifferenceGreaterThanEightHours(InTime, OutTime);
        if (isDifferenceGreaterThanEightHours) {
            // Do something if the difference is greater than 8 hours
            Log.e("TimeDifference", "Time difference is greater than 8 hours");
            binding.textActivity.setText("Present");
        } else {
            Log.e("TimeDifference", "Time difference is not greater than 8 hours");
            binding.textActivity.setText("Half Day");
        }
    }else{
        Log.e("TimeDifference", "InTime or OutTime is null");
    }

}
    public static boolean isTimeDifferenceGreaterThanEightHours(String inTime, String outTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault());
        LocalTime inLocalTime = LocalTime.parse(inTime, formatter);
        LocalTime outLocalTime = LocalTime.parse(outTime, formatter);
        long diffInHours = ChronoUnit.HOURS.between(inLocalTime, outLocalTime);
        return diffInHours > 8;
    }
}