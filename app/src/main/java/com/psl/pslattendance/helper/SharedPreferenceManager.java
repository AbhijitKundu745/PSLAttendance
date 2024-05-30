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
    private static final String USER_FIRSTNAME = "FIRST_NAME";
    private static final String IMAGE_DATA = "IMAGE_DATA";
    private static final String LOCATION_DATA = "LOCATION_DATA";
    private static final String USER_ID = "USER_ID";
    private static final String LOCATION_NAME = "LOCATION_NAME";
    private static final String CAMERA_PERMISSION = "CAMERA_PERMISSION";
    private static final String LOCATION_PERMISSION = "LOCATION_PERMISSION";
    private static final String TRANS_ID = "TRANS_ID";
    private static final String IS_CHECKED_IN = "IS_CHECKED_IN";
    private static final String IS_FULL_CHECKED = "IS_FULL_CHECKED";
    private static final String OUT_DATE_TIME = "OUT_DATE_TIME";

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
    public static String getUserFirstname(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getString(USER_FIRSTNAME, null);
    }

    public static void setUserFirstname(Context context, String newValue) {
        SharedPreferences prefs = getSharedPreferences(context);
        synchronized (prefs) {
            prefs.edit().putString(USER_FIRSTNAME, newValue).apply();
        }
    }
    public static String getUserId(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getString(USER_ID, null);
    }

    public static void setUserId(Context context, String newValue) {
        SharedPreferences prefs = getSharedPreferences(context);
        synchronized (prefs) {
            prefs.edit().putString(USER_ID, newValue).apply();
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
    public static String getLocationName(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getString(LOCATION_NAME, null);
    }

    public static void setLocationName(Context context, String newValue) {
        SharedPreferences prefs = getSharedPreferences(context);
        synchronized (prefs) {
            prefs.edit().putString(LOCATION_NAME, newValue).apply();
        }
    }
    public static Boolean getCameraPermission(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getBoolean(CAMERA_PERMISSION, false);
    }

    public static void setCameraPermission(Context context, Boolean newValue) {
        SharedPreferences prefs = getSharedPreferences(context);
        synchronized (prefs) {
            prefs.edit().putBoolean(CAMERA_PERMISSION, newValue).apply();
        }
    }
    public static Boolean getLocationPermission(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getBoolean(LOCATION_PERMISSION, false);
    }

    public static void setLocationPermission(Context context, Boolean newValue) {
        SharedPreferences prefs = getSharedPreferences(context);
        synchronized (prefs) {
            prefs.edit().putBoolean(LOCATION_PERMISSION, newValue).apply();
        }
    }
    public static String getTransId(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getString(TRANS_ID, "3B678469-FFF5-43A1-BB73-566648BCA5A0");
    }

    public static void setTransId(Context context, String newValue) {
        SharedPreferences prefs = getSharedPreferences(context);
        synchronized (prefs) {
            prefs.edit().putString(TRANS_ID, newValue).apply();
        }
    }
    public static boolean getIsCheckedIn(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getBoolean(IS_CHECKED_IN, false);
    }
    public static void setIsCheckedIn(Context context, boolean newValue) {
        SharedPreferences prefs = getSharedPreferences(context);
        synchronized (prefs) {
            prefs.edit().putBoolean(IS_CHECKED_IN, newValue).apply();
        }
    }
    public static boolean getIsFullChecked(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getBoolean(IS_FULL_CHECKED, false);
    }
    public static void setIsFullChecked(Context context, boolean newValue) {
        SharedPreferences prefs = getSharedPreferences(context);
        synchronized (prefs) {
            prefs.edit().putBoolean(IS_FULL_CHECKED, newValue).apply();
        }
    }
    public static String getOutDateTime(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getString(OUT_DATE_TIME, "");
    }

    public static void setOutDateTime(Context context, String newValue) {
        SharedPreferences prefs = getSharedPreferences(context);
        synchronized (prefs) {
            prefs.edit().putString(OUT_DATE_TIME, newValue).apply();
        }
    }
}
