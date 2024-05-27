package com.psl.pslattendance;

import static com.psl.pslattendance.helper.AssetUtils.hideProgressDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.devicelock.DeviceId;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.common.api.Api;
import com.google.android.material.snackbar.Snackbar;
import com.psl.pslattendance.databinding.ActivityRegisterBinding;
import com.psl.pslattendance.helper.ApiConstants;
import com.psl.pslattendance.helper.AssetUtils;
import com.psl.pslattendance.helper.ConnectionDetector;
import com.psl.pslattendance.helper.SearchableAdapter;
import com.psl.pslattendance.helper.SharedPreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ContentHandler;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private Context context = this;
    private ConnectionDetector cd;
    String FirstName = null;
    String LastName = null;
    String EmailID = null;
    String Contact = null;
    String EmpID = null;
    String Password = null;
    String Location = null;
    SearchableAdapter searchableAdapter;
    Dialog dialog;
    ArrayList<String> LocationList = new ArrayList<>();
    private String SELECTED_ITEM = "";
    private String default_source_item = "Select Location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_register);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register);
        cd = new ConnectionDetector(context);
        //setDefault();
        String androidID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        androidID = androidID.toUpperCase();
        SharedPreferenceManager.setDeviceId(context, androidID);
        Log.e("DEVICEID", androidID);

        getLocationDetails();

        binding.btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDefault();
            }
        });
        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirstName = binding.editTextFirstName.getText().toString().trim();
                LastName = binding.editTextLastName.getText().toString().trim();
                EmailID = binding.editTextEmail.getText().toString().trim();
                Contact = binding.editTextMobile.getText().toString().trim();
                EmpID = binding.editTextEmpID.getText().toString().trim();
                Password = binding.editTextPassword.getText().toString().trim();
                Location = binding.searchableTextView.getText().toString().trim();
                if (!TextUtils.isEmpty(FirstName)) {
                    if (!TextUtils.isEmpty(LastName)) {
                        if (!TextUtils.isEmpty(EmailID)) {
                            if (!TextUtils.isEmpty(Contact)) {
                                if (!TextUtils.isEmpty(EmpID)) {
                                    if (!TextUtils.isEmpty(Password)) {
                                        if (!Location.equalsIgnoreCase(default_source_item)) {
                                            try {
                                                JSONObject jsonObject = new JSONObject();
                                                jsonObject.put(ApiConstants.K_FIRST_NAME, FirstName);
                                                jsonObject.put(ApiConstants.K_LAST_NAME, LastName);
                                                jsonObject.put(ApiConstants.K_EMAIL, EmailID);
                                                jsonObject.put(ApiConstants.K_CONTACT, Contact);
                                                jsonObject.put(ApiConstants.K_EMP_ID, EmpID);
                                                jsonObject.put(ApiConstants.K_PASSWORD, Password);
                                                jsonObject.put(ApiConstants.K_LOCATION_NAME, Location);
                                                jsonObject.put(ApiConstants.K_DEVICE_ID, SharedPreferenceManager.getDeviceId(context));
                                                userRegister(jsonObject, ApiConstants.M_USER_REG, "Processing...");
                                            } catch (JSONException e) {

                                            }
                                        }  else {
                                            AssetUtils.showAlertDialog(context, "Location Required", "Please enter above details first");
                                        }
                                    } else {
                                        AssetUtils.showAlertDialog(context, "Password Required", "Please enter your Password");
                                    }
                                } else {
                                    AssetUtils.showAlertDialog(context, "Employee ID Required", "Please enter your Employee ID");
                                }
                            } else {
                                AssetUtils.showAlertDialog(context, "Mobile No Required", "Please enter your Mobile Number");
                            }
                        } else {
                            AssetUtils.showAlertDialog(context, "Email ID Required", "Please enter your Email ID");
                        }
                    } else {
                        AssetUtils.showAlertDialog(context, "Last Name Required", "Please enter your Last Name");
                    }
                } else {
                    AssetUtils.showAlertDialog(context, "First Name Required", "Please enter your First Name");
                }
            }
        });
        binding.searchableTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initialize dialog
                dialog = new Dialog(RegisterActivity.this);

                // set custom dialog
                dialog.setContentView(R.layout.dialog_searchable_spinner);

                // set custom height and width
                dialog.getWindow().setLayout(650, 800);

                // set transparent background
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                // show dialog
                dialog.show();

                // Initialize and assign variable
                ListView listView = dialog.findViewById(R.id.list_view);

                // Initialize array adapter
                searchableAdapter = new SearchableAdapter(RegisterActivity.this, LocationList);

                // set adapter
                listView.setAdapter(searchableAdapter);


                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            // when item selected from list
                            // set selected item on textView
                            // Dismiss dialog
                            dialog.dismiss();
                            SELECTED_ITEM = (String) searchableAdapter.getItem(position);
                            binding.searchableTextView.setText(SELECTED_ITEM);

                            if (SELECTED_ITEM.equalsIgnoreCase(default_source_item) || SELECTED_ITEM.equalsIgnoreCase("")) {
                                SELECTED_ITEM = "";

                            } else {
                                SharedPreferenceManager.setLocationName(context, SELECTED_ITEM);
                            }

                    }
                });
            }
        });
    }

    public void onLoginClick(View View) {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void userRegister(JSONObject registerJson, String METHODNAME, String progreesMess) {
        AssetUtils.showProgress(context, progreesMess);
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(ApiConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(ApiConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(ApiConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .build();
        Log.e("RegJSONBody", registerJson.toString());
        AndroidNetworking.post(ApiConstants.URL + METHODNAME).addJSONObjectBody(registerJson)
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
                                Log.e("REGRESULT", result.toString());
                                String status = result.getString(ApiConstants.K_STATUS).trim();
                                String message = result.getString(ApiConstants.K_MESSAGE).trim();
                                if (status.equalsIgnoreCase("true")) {
                                    SharedPreferenceManager.setUserFirstname(context, FirstName);
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                                    Intent intent1 = new Intent(RegisterActivity.this, LoginActivity.class);
                                    startActivity(intent1);
                                } else {
                                    AssetUtils.showAlertDialog(context, "", message);
                                    hideProgressDialog();
                                }
                            } catch (JSONException e) {
                                hideProgressDialog();
                                AssetUtils.showAlertDialog(context, "", getResources().getString(R.string.something_went_wrong_error));
                            }
                        } else {
                            hideProgressDialog();
                            AssetUtils.showAlertDialog(context, "", getResources().getString(R.string.communication_error));
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        hideProgressDialog();
                        Log.e("ERROR", anError.getErrorDetail());
                        if (anError.getErrorDetail().equalsIgnoreCase("responseFromServerError")) {
                            hideProgressDialog();
                            AssetUtils.showAlertDialog(context, "", getResources().getString(R.string.communication_error));
                        } else if (anError.getErrorDetail().equalsIgnoreCase("connectionError")) {
                            hideProgressDialog();
                            AssetUtils.showAlertDialog(context, "", getResources().getString(R.string.internet_error));
                        } else {
                            hideProgressDialog();
                            AssetUtils.showAlertDialog(context, "", getResources().getString(R.string.internet_error));
                        }
                    }
                });
    }

    @Override
    protected void onPause() {
        hideProgressDialog();
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideProgressDialog();
        finish();
    }

    private void setDefault() {
        binding.editTextFirstName.setText("");
        binding.editTextLastName.setText("");
        binding.editTextEmpID.setText("");
        binding.editTextEmail.setText("");
        binding.editTextMobile.setText("");
        binding.editTextPassword.setText("");
        binding.searchableTextView.setText(default_source_item);
    }

    private void getLocationDetails() {
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(ApiConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(ApiConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(ApiConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .build();
        AndroidNetworking.get(ApiConstants.URL + ApiConstants.M_GET_LOCATION)
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
                                            for (int i = 0; i < dataArray.length(); i++) {
                                                JSONObject dataObject = dataArray.getJSONObject(i);
                                                String LocationName = dataObject.getString(ApiConstants.K_LOCATION_NAME);
                                                LocationList.add(LocationName);
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
}
