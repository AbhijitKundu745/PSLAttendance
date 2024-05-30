package com.psl.pslattendance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.android.gms.common.api.Api;
import com.psl.pslattendance.databinding.ActivityDashboardBinding;
import com.psl.pslattendance.helper.ApiConstants;
import com.psl.pslattendance.helper.AssetUtils;
import com.psl.pslattendance.helper.ConnectionDetector;
import com.psl.pslattendance.helper.SharedPreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class DashboardActivity extends AppCompatActivity {
private Context context = this;
    private ConnectionDetector cd;
    private static final int REQUEST_LOCATION_PERMISSION = 200;
    private boolean LocationPermissionGranted = false;
     String Activity;
    Boolean IsMoreThan8Hours = false;


ActivityDashboardBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_dashboard);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard);
        cd = new ConnectionDetector(context);
        if(!SharedPreferenceManager.getLocationPermission(context)){
            // Request location permission
            requestLocationPermission();
        }

        binding.btnpnchIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IsMoreThan8Hours =  IsMoreThan8Hours();
                Log.e("MoreThan8",IsMoreThan8Hours.toString());
                if(IsMoreThan8Hours){
                        setDefault();
                        SharedPreferenceManager.setIsCheckedIn(context, true);
                        SharedPreferenceManager.setOutDateTime(context, "");
                        Intent intent = new Intent(DashboardActivity.this, PunchInActivity.class);
                        startActivity(intent);

                }
                else if(!SharedPreferenceManager.getIsFullChecked(context)){
                    if(!SharedPreferenceManager.getIsCheckedIn(context)){
                        Intent intent = new Intent(DashboardActivity.this, PunchInActivity.class);
                        startActivity(intent);

                    }
                    else{
                        AssetUtils.showAlertDialog(context, "", "You have already checked IN");
                    }
                } else{
                    AssetUtils.showAlertDialog(context, "", "You have already checked for the day");
               }
            }
        });
        binding.btnpnchOUT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IsMoreThan8Hours =  IsMoreThan8Hours();
                if(IsMoreThan8Hours){
                    setDefault();
                    if(SharedPreferenceManager.getIsCheckedIn(context)){
                        Intent intent1 = new Intent(DashboardActivity.this, PunchOutActivity.class);
                        startActivity(intent1);
                    } else{
                        AssetUtils.showAlertDialog(context, "", "You haven't checked IN yet");
                    }
                }
               else if(!SharedPreferenceManager.getIsFullChecked(context)){

                   if(SharedPreferenceManager.getIsCheckedIn(context)){
                    Intent intent1 = new Intent(DashboardActivity.this, PunchOutActivity.class);
                    startActivity(intent1);
                    } else{
                        AssetUtils.showAlertDialog(context, "", "You haven't checked IN yet");
                    }
                } else{
                    AssetUtils.showAlertDialog(context, "", "You have already checked for the day");
                }
            }
        });
        binding.userGreet.setText("Hello, "+ SharedPreferenceManager.getUserFirstname(context));

        Handler handler = new Handler(Looper.getMainLooper());
        if(cd.isConnectingToInternet()){
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
            handler.postDelayed(runnable, 1000);
        }

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
            Log.e("JSONreq",jsonObject.toString());

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
                                binding.textActivity.setVisibility(View.GONE);
                                binding.linearInReport.setVisibility(View.GONE);
                                binding.linearOutReport.setVisibility(View.GONE);
                                try {
                                    Log.e("FetchRes", result.toString());
                                    String status = result.getString(ApiConstants.K_STATUS).trim();
                                    String message = result.getString(ApiConstants.K_MESSAGE).trim();
                                    if (status.equalsIgnoreCase("true")) {

                                        if (result.has(ApiConstants.K_DATA)) {

                                            JSONArray dataArray = result.getJSONArray(ApiConstants.K_DATA);
                                            if (dataArray.length() > 0){
                                                SharedPreferenceManager.setIsCheckedIn(context, false);
                                                SharedPreferenceManager.setIsFullChecked(context, false);
                                                setDashboardReport(dataArray);
                                            }
                                            else{
                                                setDefault();
                                            }
                                        }
                                    } else {
                                       setDefault();
                                        AssetUtils.showAlertDialog(context, "", message);
                                    }
                                } catch (JSONException e) {
                                    AssetUtils.showAlertDialog(context, "", getResources().getString(R.string.something_went_wrong_error));
                                }
                            } else {
                                setDefault();
                                AssetUtils.showAlertDialog(context, "", getResources().getString(R.string.communication_error));
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.e("ERROR", anError.getErrorDetail());
                            if (anError.getErrorDetail().equalsIgnoreCase("responseFromServerError")) {
                                AssetUtils.showAlertDialog(context, "", getResources().getString(R.string.communication_error));
                            } else if (anError.getErrorDetail().equalsIgnoreCase("connectionError")) {
                               Log.e("No Internet", "No internet connection found.");
                            }
                        }
                    });
        }
        catch (JSONException e){

        }
}
    String InTime = null;
    String OutTime = null;
    String InDate = null;
    String InDay = null;
    String OutDate = null;
    String OutDay = null;
    String InDateTime = null;
    private void setDashboardReport(JSONArray dataArray){
        try{
            String  Type = null;
            boolean hasIN = false;
            boolean hasOUT = false;
            binding.linearReport.setVisibility(View.VISIBLE);
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject dataObject = dataArray.getJSONObject(i);
                String ActivityDateTime = null;
                if(dataObject.has(ApiConstants.K_PUNCH_DATE_TIME)){
                    ActivityDateTime = dataObject.getString(ApiConstants.K_PUNCH_DATE_TIME);
                }
                if(dataObject.has(ApiConstants.K_ACTIVITY_TYPE)){
                    Type = dataObject.getString(ApiConstants.K_ACTIVITY_TYPE);
                    if(Type.equalsIgnoreCase("IN")){
                        SharedPreferenceManager.setIsCheckedIn(context, true);
                        hasIN = true;
                        InTime = AssetUtils.getTime(ActivityDateTime);
                        InDate = AssetUtils.getDate(ActivityDateTime);
                        InDay = AssetUtils.getDayOfWeek(ActivityDateTime);
                        binding.linearInReport.setVisibility(View.VISIBLE);
                        binding.textInDate.setText(InDate);
                        binding.textInDay.setText(InDay);
                        binding.textInTime.setText(InTime);
                        InDateTime = ActivityDateTime;
                        Log.e("InTime",InTime);

                    }
                    else if(Type.equalsIgnoreCase("OUT")){
                        hasOUT = true;
                        binding.textActivity.setVisibility(View.VISIBLE);
                        OutTime = AssetUtils.getTime(ActivityDateTime);
                        OutDate = AssetUtils.getDate(ActivityDateTime);
                        OutDay = AssetUtils.getDayOfWeek(ActivityDateTime);
                        binding.linearInReport.setVisibility(View.VISIBLE);
                        binding.linearOutReport.setVisibility(View.VISIBLE);
                        binding.textOutDate.setText(OutDate);
                        binding.textOutDay.setText(OutDay);
                        binding.textOutTime.setText(OutTime);
                        Log.e("OutTime",OutTime);
                        SharedPreferenceManager.setOutDateTime(context, ActivityDateTime);
                        Log.e("ActivityDateTime",ActivityDateTime);
                    }
                    if(InTime!=null && OutTime!=null) {
                        boolean isDifferenceGreaterThanEightHours = isTimeDifferenceGreaterThanEightHours();
                        boolean isDifferenceGreaterThanFourHours = isTimeDifferenceGreaterThanFourHours();
                        boolean isDifferenceLessThanFourHours = isTimeDifferenceLessThanFourHours();
                        if (isDifferenceGreaterThanEightHours) {
                            Activity = "Present";
                        } else if (isDifferenceGreaterThanFourHours) {
                            Activity = "HalfDay";
                        } else if (isDifferenceLessThanFourHours){
                            Activity = "Early Check Out";
                        } else{
                            Activity = "";
                        }
                        binding.textActivity.setText(Activity);
                    }
                }
                if(dataObject.has(ApiConstants.K_TRANS_ID) && !dataObject.getString(ApiConstants.K_TRANS_ID).equalsIgnoreCase("")){
                    SharedPreferenceManager.setTransId(context, dataObject.getString(ApiConstants.K_TRANS_ID));
                }
            }
            if(hasIN&&hasOUT){
                SharedPreferenceManager.setIsFullChecked(context, true);
            }
        }
        catch(JSONException ex){
            AssetUtils.showAlertDialog(context, "", getResources().getString(R.string.something_went_wrong_error));
        }
    }
    public boolean isTimeDifferenceGreaterThanEightHours() {
        boolean retVal = false;
        if(InDateTime!=null && SharedPreferenceManager.getOutDateTime(context)!=""){
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            OffsetDateTime outDateTime1 = OffsetDateTime.parse(SharedPreferenceManager.getOutDateTime(context), formatter.withZone(ZoneOffset.UTC));
            OffsetDateTime inDateTime1 = OffsetDateTime.parse(InDateTime, formatter.withZone(ZoneOffset.UTC));
            Instant outInstant = outDateTime1.toInstant();
            Instant inInstant = inDateTime1.toInstant();
            Duration duration = Duration.between(inInstant, outInstant);
            retVal =  duration.toMinutes() > 475;
        }
        return retVal;
    }
    public boolean isTimeDifferenceGreaterThanFourHours() {
        boolean retVal = false;
        if(InDateTime!=null && SharedPreferenceManager.getOutDateTime(context)!=""){
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            OffsetDateTime outDateTime1 = OffsetDateTime.parse(SharedPreferenceManager.getOutDateTime(context), formatter.withZone(ZoneOffset.UTC));
            OffsetDateTime inDateTime1 = OffsetDateTime.parse(InDateTime, formatter.withZone(ZoneOffset.UTC));
            Instant outInstant = outDateTime1.toInstant();
            Instant inInstant = inDateTime1.toInstant();
            Duration duration = Duration.between(inInstant, outInstant);
            retVal =  duration.toHours() >= 4;
        }
        return retVal;
    }
    public boolean isTimeDifferenceLessThanFourHours() {
        boolean retVal = false;
        if(InDateTime!=null && SharedPreferenceManager.getOutDateTime(context)!=""){
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            OffsetDateTime outDateTime1 = OffsetDateTime.parse(SharedPreferenceManager.getOutDateTime(context), formatter.withZone(ZoneOffset.UTC));
            OffsetDateTime inDateTime1 = OffsetDateTime.parse(InDateTime, formatter.withZone(ZoneOffset.UTC));
            Instant outInstant = outDateTime1.toInstant();
            Instant inInstant = inDateTime1.toInstant();
            Duration duration = Duration.between(inInstant, outInstant);
            retVal =  duration.toHours() < 4;
        }
        return retVal;
    }
    private void setDefault(){
        SharedPreferenceManager.setIsCheckedIn(context, false);
        SharedPreferenceManager.setIsFullChecked(context, false);
        binding.textActivity.setVisibility(View.GONE);
        binding.linearInReport.setVisibility(View.GONE);
        binding.linearOutReport.setVisibility(View.GONE);
        InTime = null;
        OutTime = null;
        Activity = "";
        InDay = "";
        OutDay = "";
        InDate = null;
        OutDate = null;
        SharedPreferenceManager.setOutDateTime(context, "");
        InDateTime = null;
    }
    private boolean IsMoreThan8Hours(){
        boolean retVal = false;
        if(SharedPreferenceManager.getOutDateTime(context)!=""){
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            OffsetDateTime outDateTime = OffsetDateTime.parse(SharedPreferenceManager.getOutDateTime(context), formatter.withZone(ZoneOffset.UTC));
            // Convert outDateTime to ZonedDateTime in the local system time zone
            Instant outInstant = outDateTime.toInstant();
            LocalDateTime currentDateTime = LocalDateTime.now();
            String formattedDateTime = currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            OffsetDateTime currentTime1 = OffsetDateTime.parse(formattedDateTime, formatter.withZone(ZoneOffset.UTC));
            Instant currentTime = currentTime1.toInstant();
            Log.e("OutInstant", outInstant.toString());
            Log.e("currentTimeInstant", currentTime.toString());
            Duration duration = Duration.between(outInstant, currentTime);
            Log.e("Duration", duration.toString());
            retVal =  duration.toMinutes() > 475;
        }
        return retVal;
    }
}