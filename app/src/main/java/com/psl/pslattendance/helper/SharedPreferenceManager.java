package com.psl.pslattendance.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.renderscript.Sampler;

public class SharedPreferenceManager {
    private static final String SHARED_PREF_NAME = "psl_attendance_prefs";
    private static final String DEVICE_ID = "DEVICE_ID";
    private static final String IS_LOGIN_SAVED = "IS_LOGIN_SAVED";
    private static final String LOGIN_USER = "LOGIN_USER_DETAILS";
    private static final String LOGIN_PASSWORD = "LOGIN_PASSWORD";
    private static final String IMAGE_DATA = "IMAGE_DATA";
    private static final String LOCATION_DATA = "LOCATION_DATA";

    private SharedPreferenceManager() {} // prevent instantiation
    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }
    public static String getDeviceId(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getString(DEVICE_ID, null);
    }

    public static void setDeviceId(Context context, String newValue) {
        SharedPreferences prefs = getSharedPreferences(context);
        synchronized (prefs) {
            prefs.edit().putString(DEVICE_ID, newValue).apply();
        }
    }
    public static boolean getIsLoginSaved(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getBoolean(IS_LOGIN_SAVED, false);
    }
    public static void setIsLoginSaved(Context context, boolean newValue) {
        SharedPreferences prefs = getSharedPreferences(context);
        synchronized (prefs) {
            prefs.edit().putBoolean(IS_LOGIN_SAVED, newValue).apply();
        }
    }
    public static String getLoginUser(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getString(LOGIN_USER, null);
    }

    public static void setLoginUser(Context context, String newValue) {
        SharedPreferences prefs = getSharedPreferences(context);
        synchronized (prefs) {
            prefs.edit().putString(LOGIN_USER, newValue).apply();
        }
    }
    public static String getLoginPassword(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getString(LOGIN_PASSWORD, null);
    }

    public static void setLoginPassword(Context context, String newValue) {
        SharedPreferences prefs = getSharedPreferences(context);
        synchronized (prefs) {
            prefs.edit().putString(LOGIN_PASSWORD, newValue).apply();
        }
    }
    public static String getImageData(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getString(IMAGE_DATA, null);
    }

    public static void setImageData(Context context, String newValue) {
        SharedPreferences prefs = getSharedPreferences(context);
        synchronized (prefs) {
            prefs.edit().putString(IMAGE_DATA, newValue).apply();
        }
    }
    public static String getLocationData(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getString(LOCATION_DATA, null);
    }

    public static void setLocationData(Context context, String newValue) {
        SharedPreferences prefs = getSharedPreferences(context);
        synchronized (prefs) {
            prefs.edit().putString(LOCATION_DATA, newValue).apply();
        }
    }

}
