package com.psl.pslattendance;

import static com.psl.pslattendance.helper.AssetUtils.hideProgressDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.psl.pslattendance.databinding.ActivityPunchOutBinding;
import com.psl.pslattendance.helper.ApiConstants;
import com.psl.pslattendance.helper.AssetUtils;
import com.psl.pslattendance.helper.SharedPreferenceManager;
import com.psl.pslattendance.holder.CameraPreview;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class PunchOutActivity extends AppCompatActivity {
ActivityPunchOutBinding binding;
    private Context context = this;

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_LOCATION_PERMISSION = 200;
    private Camera camera;
    private CameraPreview mPreview;
    private boolean CameraPermissionGranted = false;
    private boolean LocationPermissionGranted = true;
    private boolean IsCaptured = false;
    String ImageData= null;
    String LocationData = null;
    String LocationCoordinates = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_punch_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_punch_out);
        // Request camera permission
        requestCameraPermission();
        // Request location permission
        requestLocationPermission();
        setDefault();
        if(SharedPreferenceManager.getCameraPermission(context)){
            Log.e("PermCamera", SharedPreferenceManager.getCameraPermission(context).toString());
            CameraPermissionGranted = true;
        }
        if(SharedPreferenceManager.getLocationPermission(context)){
            Log.e("PermLoc", SharedPreferenceManager.getLocationPermission(context).toString());
            LocationPermissionGranted = true;
        }
        if(CameraPermissionGranted){
            camera = getFrontFacingCamera();
            if (camera != null) {
                mPreview = new CameraPreview(this, camera);
                binding.cameraPreview.addView(mPreview);
            } else {
                // Handle the case where the camera is not available
            }
        }

        if(IsCaptured){
            binding.buttonCapture.setVisibility(View.GONE);
        } else{
            binding.buttonCapture.setVisibility(View.VISIBLE);
        }
        // Set up the capture button
        binding.buttonCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CameraPermissionGranted) {
                    takePicture();
                } else {
                    AssetUtils.showAlertDialog(context, "Access Error","Please allow the camera");
                }
            }
        });
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Set the date and time as the text of the TextView
                binding.dateTime.setText(AssetUtils.getSystemDateTimeInFormatt());

                // Schedule the Runnable to run again after 5 seconds
                handler.postDelayed(this, 5000);
            }
        };
        handler.postDelayed(runnable, 1000);

        binding.userGreet.setText(SharedPreferenceManager.getUserFirstname(context));

        binding.btnPunchOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(IsCaptured){
                    if(!TextUtils.isEmpty(LocationData)){
                        uploadData();
                    }
                    else {
                        AssetUtils.showAlertDialog(context, "", "Please enable your location permission");
                    }
                }
                else {
                    AssetUtils.showAlertDialog(context, "", "Please capture your Image");
                }
            }
        });
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
    //... Rest of the code

    private Camera getFrontFacingCamera() {
        int cameraId = Camera.getNumberOfCameras() - 1;
        Camera camera = Camera.open(cameraId);
        Camera.Parameters parameters = camera.getParameters();
        List<String> supportedFocusModes = parameters.getSupportedFocusModes();
        if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 180;
                break;
            case Surface.ROTATION_90:
                degrees = 270;
                break;
            case Surface.ROTATION_180:
                degrees = 0;
                break;
            case Surface.ROTATION_270:
                degrees = 90;
                break;
        }

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        int displayOrientation = (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) ?
                (cameraInfo.orientation + degrees) % 360 :
                (cameraInfo.orientation - degrees + 360) % 360;

        // Set the display orientation
        camera.setDisplayOrientation(displayOrientation);

        camera.setParameters(parameters);
        return camera;
    }

    private void takePicture() {
        if (camera != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    camera.takePicture(null, null, PictureCallback);
                }
            }).start();
        }
    }
    private Bitmap compressAndResizeBitmap(Bitmap bitmap, int quality, int maxDimension) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float aspectRatio = (float) width / height;
        if (width > height && width > maxDimension) {
            width = maxDimension;
            height = (int) (width / aspectRatio);
        } else if (height > width && height > maxDimension) {
            height = maxDimension;
            width = (int) (height * aspectRatio);
        }
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.PNG, quality, byteArrayOutputStream);
        return resizedBitmap;
    }
    private Camera.PictureCallback PictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // Convert the byte array to a Bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            // Rotate the bitmap to the correct orientation
            bitmap = rotateBitmap(bitmap, getCameraDisplayOrientation());
            // Compress and resize the Bitmap to ensure it's below 300 KB
            bitmap = compressAndResizeBitmap(bitmap, 100, 1024);
            // Convert the Bitmap to a base64 encoded string
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String base64Image = Base64.encodeToString(byteArray, Base64.NO_WRAP);
            // Create a DataURL
            String dataUrl = "data:image/jpeg;base64," + base64Image;
            SharedPreferenceManager.setImageData(context, dataUrl);
            Log.e("Image", dataUrl);
            ImageData = dataUrl;
            IsCaptured = true;
            binding.buttonCapture.setVisibility(View.GONE);
            if (camera!= null) {
                camera.release();
                camera = null;
            }
        }
    };
    private int getCameraDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(getBackCameraId(), info);
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 180;
                break;
            case Surface.ROTATION_90:
                degrees = 270;
                break;
            case Surface.ROTATION_180:
                degrees = 0;
                break;
            case Surface.ROTATION_270:
                degrees = 90;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    private Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
    private int getBackCameraId() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return i;
            }
        }
        return -1;
    }

    private void retrieveLocation() {
        // Get the last known location
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                if (addresses.size() > 0) {
                                    Address address = addresses.get(0);
                                    String locationName = address.getAddressLine(0); // Get the first address line (e.g., "1600 Amphitheatre Parkway")
                                    SharedPreferenceManager.setLocationData(context, locationName);
                                    // Set the location name as the text of the TextView
                                    binding.locationData.setText(locationName);
                                    binding.locationData.setSelected(true);

                                    Log.e("LocationName", locationName);
                                    LocationData = locationName;
                                    LocationCoordinates = ""+location.getLatitude()+","+location.getLongitude()+"";
                                    Log.e("Location1", LocationCoordinates);

                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            // Request location updates
                            LocationRequest locationRequest = LocationRequest.create();
                            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                            locationRequest.setInterval(1000);
                            locationRequest.setFastestInterval(500);

                            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                                    .addLocationRequest(locationRequest);

                            SettingsClient settingsClient = LocationServices.getSettingsClient(context);
                            Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

                            task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                                @Override
                                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                                    // GPS is enabled, request location updates
                                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        // TODO: Consider calling
                                        //    ActivityCompat#requestPermissions
                                        // here to request the missing permissions, and then overriding
                                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                        //                                          int[] grantResults)
                                        // to handle the case where the user grants the permission. See the documentation
                                        // for ActivityCompat#requestPermissions for more details.

                                        return;
                                    }
                                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
                                }
                            });

                            task.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    if (e instanceof ResolvableApiException) {
                                        // GPS is disabled, show a dialog to the user asking them to enable GPS
                                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                        try {
                                            resolvableApiException.startResolutionForResult(PunchOutActivity.this, REQUEST_LOCATION_PERMISSION);
                                        } catch (IntentSender.SendIntentException ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                }
                            });
                        }
                    }
                });
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
        else{

            CameraPermissionGranted = true;
            // Initialize the camera
            camera = getFrontFacingCamera();
            if (camera!= null) {
                mPreview = new CameraPreview(this, camera);
                binding.cameraPreview.addView(mPreview);
            } else {
                // Handle the case where the camera is not available
            }
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
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                CameraPermissionGranted = true;
                camera = getFrontFacingCamera();
                if (camera != null) {
                    mPreview = new CameraPreview(this, camera);
                    binding.cameraPreview.addView(mPreview);
                } else {
                    // Handle the case where the camera is not available
                }
            } else {
                // Permission denied
                CameraPermissionGranted = false;
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                LocationPermissionGranted = true;
            } else {
                // Permission denied
                LocationPermissionGranted = false;
            }
        }
    }
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                // Do something with the location
                // For example, save it to SharedPreferences
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses.size() > 0) {
                        Address address = addresses.get(0);
                        String locationName = address.getAddressLine(0); // Get the first address line (e.g., "1600 Amphitheatre Parkway")
                        SharedPreferenceManager.setLocationData(context, locationName);
                        // Set the location name as the text of the TextView
                        binding.locationData.setText(locationName);
                        binding.locationData.setSelected(true);
                        Log.e("Location2", location.toString());
                        Log.e("LocationName2", locationName);

                        LocationData = locationName;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void uploadData(){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(ApiConstants.K_USER_ID, SharedPreferenceManager.getUserId(context));
            jsonObject.put(ApiConstants.K_ACTIVITY_TYPE, "OUT");
            jsonObject.put(ApiConstants.K_IMAGE_DATA, ImageData);
            jsonObject.put(ApiConstants.K_LOCATION_DATA, LocationData);
            jsonObject.put(ApiConstants.K_LOCATION_COORDINATE, LocationCoordinates);
            jsonObject.put(ApiConstants.K_PUNCH_DATE_TIME, AssetUtils.getSystemDateTimeInFormat());
            jsonObject.put(ApiConstants.K_DEVICE_ID, SharedPreferenceManager.getDeviceId(context));
            jsonObject.put(ApiConstants.K_TRANS_ID, SharedPreferenceManager.getTransId(context));
            punchIn(jsonObject, ApiConstants.M_USER_PUNCH_ACTIVITY,"Processing...");
        } catch (JSONException e) {

        }
    }
    private void punchIn(JSONObject jsonObject, String MethodName, String progressMessage){
        AssetUtils.showProgress(context, progressMessage);

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(ApiConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(ApiConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(ApiConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .build();
        Log.e("JSONBody", jsonObject.toString());
        AndroidNetworking.post(ApiConstants.URL + MethodName).addJSONObjectBody(jsonObject)
                .setTag("test")
                .setPriority(Priority.LOW)
                .setOkHttpClient(okHttpClient) // passing a custom okHttpClient
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject result) {
                        hideProgressDialog();
                        if (result != null) {
                            try {
                                Log.e("POSTINGRES", result.toString());
                                String status = result.getString(ApiConstants.K_STATUS).trim();
                                String message = result.getString(ApiConstants.K_MESSAGE).trim();
                                if (status.equalsIgnoreCase("true")) {
                                    hideProgressDialog();
                                    setDefault();
                                    finish();
                                } else {
                                    hideProgressDialog();
                                    DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            finish();
                                        }
                                    };
                                    AssetUtils.showAlertDialogSpec(context,"", message, onClickListener);
                                }
                            } catch (JSONException e) {
                                hideProgressDialog();
                                AssetUtils.showAlertDialog(context,"", getResources().getString(R.string.something_went_wrong_error));
                            }
                        } else {
                            hideProgressDialog();
                            AssetUtils.showAlertDialog(context,"", getResources().getString(R.string.communication_error));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hideProgressDialog();
                        Log.e("ERROR", anError.getErrorDetail());
                        if (anError.getErrorDetail().equalsIgnoreCase("responseFromServerError")) {
                            hideProgressDialog();
                            AssetUtils.showAlertDialog(context,"", getResources().getString(R.string.communication_error));
                        } else if (anError.getErrorDetail().equalsIgnoreCase("connectionError")) {
                            hideProgressDialog();
                            AssetUtils.showAlertDialog(context,"", getResources().getString(R.string.internet_error));
                        } else {
                            hideProgressDialog();
                            AssetUtils.showAlertDialog(context,"", getResources().getString(R.string.internet_error));
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
        IsCaptured = false;
        super.onDestroy();
    }

    private void setDefault(){
        IsCaptured = false;
        ImageData = "";
        LocationData = "";
        binding.locationData.setText("");
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (LocationPermissionGranted) {
            retrieveLocation();
        }
        if (CameraPermissionGranted) {
            camera = getFrontFacingCamera();
            if (camera!= null) {
                mPreview = new CameraPreview(this, camera);
                binding.cameraPreview.addView(mPreview);
            } else {
                // Handle the case where the camera is not available
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (camera!= null) {
            camera.release();
            camera = null;
        }
    }
}