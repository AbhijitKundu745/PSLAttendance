package com.psl.pslattendance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.AndroidViewModel;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.psl.pslattendance.databinding.ActivityLoginBinding;
import com.psl.pslattendance.helper.ApiConstants;
import com.psl.pslattendance.helper.ApiService;
import com.psl.pslattendance.helper.ConnectionDetector;
import com.psl.pslattendance.helper.RetrofitClient;
import com.psl.pslattendance.helper.SharedPreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
private ActivityLoginBinding binding;
private Context context = this;
    private ConnectionDetector cd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_login);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        cd = new ConnectionDetector(context);

        String deviceID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        deviceID = deviceID.toUpperCase();
        SharedPreferenceManager.setDeviceId(this, deviceID);

        if(SharedPreferenceManager.getIsLoginSaved(context)){
            binding.edtUserName.setText(SharedPreferenceManager.getLoginUser(context));
            binding.edtPassword.setText(SharedPreferenceManager.getLoginPassword(context));
            binding.chkRemember.setChecked(true);
        }
        else{
            binding.edtUserName.setText("");
            binding.edtPassword.setText("");
            binding.chkRemember.setChecked(false);
        }
        if (binding.chkRemember.isChecked()){
            binding.chkRemember.setChecked(true);
        } else{
            binding.chkRemember.setChecked(false);
        }
        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userDetails = binding.edtUserName.getText().toString();
                String password = binding.edtPassword.getText().toString();
                if(userDetails.equalsIgnoreCase("")||password.equalsIgnoreCase("")){
                    Toast.makeText(context, "Please insert Employee ID/Email ID/Contact Number", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(cd.isConnectingToInternet()){
                        try{
                            SharedPreferenceManager.setLoginUser(context,userDetails);
                            SharedPreferenceManager.setLoginPassword(context,password);
                            JSONObject jsonObject= new JSONObject();
                            jsonObject.put(ApiConstants.K_USER_DETAILS,userDetails);
                            jsonObject.put(ApiConstants.K_PASSWORD,password);
                            jsonObject.put(ApiConstants.K_DEVICE_ID,SharedPreferenceManager.getDeviceId(context));
                            userLogin(jsonObject, "Login");
                        } catch (Exception ex){
Log.e("EXC", ex.getMessage());
                        }
                    }

                }
            }
        });
    }
    public void onLoginClick(View View){
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
    private void userLogin(JSONObject LoginJsonObject, String ProgressMesage){
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<ResponseBody> call = apiService.login(LoginJsonObject);
        Log.e("Json", LoginJsonObject.toString());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        boolean status = jsonObject.getBoolean(ApiConstants.K_STATUS);
                        String message = jsonObject.getString(ApiConstants.K_MESSAGE);

                        if (status) {
                            // Login successful, handle accordingly
//                            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
//                            startActivity(intent);
                            Toast.makeText(context, "Login Success: " + message, Toast.LENGTH_SHORT).show();
                            if (binding.chkRemember.isChecked()) {
                                SharedPreferenceManager.setIsLoginSaved(context, true);
                            } else {
                                SharedPreferenceManager.setIsLoginSaved(context, false);
                            }
                        } else {
                            // Login failed, handle accordingly
                            Toast.makeText(context, "Login failed: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                        // Error parsing JSON or reading response body, handle accordingly
                        Toast.makeText(context, "Error parsing JSON or reading response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Server returned error, handle accordingly
                    Toast.makeText(context, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Retrofit request failed, handle accordingly
                Toast.makeText(context, "Failed to connect to server", Toast.LENGTH_SHORT).show();
            }
        });
    }
}