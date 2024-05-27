package com.psl.pslattendance;

import static com.psl.pslattendance.helper.AssetUtils.hideProgressDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.psl.pslattendance.databinding.ActivityLoginBinding;
import com.psl.pslattendance.helper.ApiConstants;
import com.psl.pslattendance.helper.AssetUtils;
import com.psl.pslattendance.helper.ConnectionDetector;
import com.psl.pslattendance.helper.SharedPreferenceManager;

import com.androidnetworking.AndroidNetworking;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private Context context = this;
    private ConnectionDetector cd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_login);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        setTitle("USER LOGIN");
        cd = new ConnectionDetector(context);

        String androidID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        androidID = androidID.toUpperCase();
        SharedPreferenceManager.setDeviceId(context, androidID);
        Log.e("DEVICEID", androidID);

        if (SharedPreferenceManager.getIsLoginSaved(context)) {
            binding.chkRemember.setChecked(true);
            binding.edtUserName.setText(SharedPreferenceManager.getLoginUser(context));
            binding.edtPassword.setText(SharedPreferenceManager.getLoginPassword(context));
        } else {
            binding.chkRemember.setChecked(false);
            binding.edtUserName.setText("");
            binding.edtPassword.setText("");
        }


        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    String user = binding.edtUserName.getText().toString().trim();
                    String password = binding.edtPassword.getText().toString().trim();
                    if (user.equalsIgnoreCase("") || password.equalsIgnoreCase("")) {
                        AssetUtils.showAlertDialog(context, "Invalid Credential", getResources().getString(R.string.login_data_validation));
                    } else {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put(ApiConstants.K_USER_DETAILS, user);
                            jsonObject.put(ApiConstants.K_PASSWORD, password);
                            jsonObject.put(ApiConstants.K_DEVICE_ID, SharedPreferenceManager.getDeviceId(context));
                            userLogin(jsonObject, ApiConstants.M_USER_LOGIN,"Processing...");
                        } catch (JSONException e) {

                        }
                    }
            }
        });
    }

    public void onRegisterClick(View View){
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void userLogin(JSONObject loginRequestObject, String METHOD_NAME, String progress_message) {
        AssetUtils.showProgress(context, progress_message);

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(ApiConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(ApiConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(ApiConstants.API_TIMEOUT, TimeUnit.SECONDS)
                .build();
        Log.e("JSONBody", loginRequestObject.toString());
        AndroidNetworking.post(ApiConstants.URL + METHOD_NAME).addJSONObjectBody(loginRequestObject)
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
                                Log.e("LOGINRESULT", result.toString());
                                String status = result.getString(ApiConstants.K_STATUS).trim();
                                String message = result.getString(ApiConstants.K_MESSAGE).trim();
                                if (status.equalsIgnoreCase("true")) {
                                    if (binding.chkRemember.isChecked()) {
                                        // Save the username and password in SharedPreferences
                                        SharedPreferenceManager.setIsLoginSaved(context, true);
                                        SharedPreferenceManager.setLoginUser(context, loginRequestObject.getString(ApiConstants.K_USER_DETAILS));
                                        SharedPreferenceManager.setLoginPassword(context, loginRequestObject.getString(ApiConstants.K_PASSWORD));
                                    } else {
                                        // Clear saved credentials
                                        SharedPreferenceManager.setIsLoginSaved(context, false);
                                        SharedPreferenceManager.setLoginUser(context, "");
                                        SharedPreferenceManager.setLoginPassword(context, "");
                                    }
                                    JSONObject dataObject = null;
                                    if(result.has(ApiConstants.K_DATA)){
                                        dataObject = result.getJSONObject(ApiConstants.K_DATA);
                                        if (dataObject != null) {
                                            if(dataObject.has(ApiConstants.K_FIRST_NAME)){
                                                String FirstName = dataObject.getString(ApiConstants.K_FIRST_NAME);
                                                SharedPreferenceManager.setUserFirstname(context, FirstName);
                                            }
                                            if(dataObject.has(ApiConstants.K_USER_ID)){
                                                String UserID = dataObject.getString(ApiConstants.K_USER_ID);
                                                Log.e("USerID", UserID);
                                                SharedPreferenceManager.setUserId(context, UserID);
                                            }
                                            Intent intent1 = new Intent(LoginActivity.this, DashboardActivity.class);
                                            startActivity(intent1);
                                        }
                                    }

                                } else {
                                    AssetUtils.showAlertDialog(context,"", message);
                                    hideProgressDialog();
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
}