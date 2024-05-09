package com.psl.pslattendance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;

import com.psl.pslattendance.databinding.ActivityRegisterBinding;
import com.psl.pslattendance.helper.ConnectionDetector;

import java.net.ContentHandler;

public class RegisterActivity extends AppCompatActivity {
private ActivityRegisterBinding binding;
private Context context = this;
private ConnectionDetector cd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_register);

        binding = DataBindingUtil.setContentView(this,R.layout.activity_register);

        cd = new ConnectionDetector(context);
    }
    public void onLoginClick(View View){
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}
