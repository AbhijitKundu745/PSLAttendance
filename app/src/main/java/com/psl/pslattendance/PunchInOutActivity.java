package com.psl.pslattendance;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.location.LocationCallback;
import com.psl.pslattendance.databinding.ActivityPunchInOutBinding;
import com.psl.pslattendance.helper.AssetUtils;
import com.psl.pslattendance.helper.SharedPreferenceManager;
import com.psl.pslattendance.holder.CameraPreview;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class PunchInOutActivity extends AppCompatActivity {
    private ActivityPunchInOutBinding binding;
    private Context context = this;

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_LOCATION_PERMISSION = 200;
    private Camera camera;
    private CameraPreview mPreview;
    private boolean CameraPermissionGranted = false;
    private boolean LocationPermissionGranted = false;
    private boolean IsCaptured = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_punch_in_out);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_punch_in_out);
        camera = getCameraInstance();

        // Request camera permission
        requestCameraPermission();
        // Request location permission
        requestLocationPermission();
        if (LocationPermissionGranted) {
            retrieveLocation();
        } else {
            Toast.makeText(context, "Please allow location", Toast.LENGTH_SHORT).show();
        }
//        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.CAMERA},
//                    REQUEST_CAMERA_PERMISSION);
//        } else {
//            // Initialize the camera
//            CameraPermissionGranted = true;
//            camera = getFrontFacingCamera();
//        }
//        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    REQUEST_LOCATION_PERMISSION);
//        } else {
//            LocationPermissionGranted = true;
//        }

        mPreview = new CameraPreview(this, camera);
        binding.cameraPreview.addView(mPreview);

        // Set up the capture button
        binding.buttonCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CameraPermissionGranted) {
                    if(!IsCaptured){
                        takePicture();
                    }
                    else{
                        binding.buttonCapture.setVisibility(View.INVISIBLE);
                    }
                } else {
                    Toast.makeText(context, "Please allow the camera", Toast.LENGTH_SHORT).show();
                }
            }
        });
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        binding.btnSetLocation.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (LocationPermissionGranted) {
//                    retrieveLocation();
//                } else {
//                    Toast.makeText(context, "Please allow location", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

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
        handler.postDelayed(runnable, 5000);
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
                    IsCaptured = true;
                    camera.takePicture(null, null, PictureCallback);
                }
            }).start();
        }
    }
    private Camera.PictureCallback PictureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                // Convert the byte array to a Bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                // Convert the Bitmap to a base64 encoded string
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);
                // Create a DataURL
                String dataUrl = "data:image/png;base64," + base64Image;
                SharedPreferenceManager.setImageData(context, dataUrl);
                Log.e("Image", dataUrl);
                camera.release();
                camera = null;
                binding.buttonCapture.setVisibility(View.INVISIBLE);
            }
    };

    private void retrieveLocation() {
        // Get the last known location
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
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
                                    Log.e("Location1", location.toString());
                                    Log.e("LocationName", locationName);

                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            // Request location updates
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
                            fusedLocationClient.requestLocationUpdates(new LocationRequest(), locationCallback, Looper.getMainLooper());
                        }
                    }
                });
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
        else{

            CameraPermissionGranted = true;
            // Initialize the camera
            camera = getFrontFacingCamera();
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

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        camera.release();
        IsCaptured = false;
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}