package com.psl.pslattendance.helper;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST(ApiConstants.M_LOGIN)
    Call<ResponseBody> login(@Body JSONObject jsonObject);
}
