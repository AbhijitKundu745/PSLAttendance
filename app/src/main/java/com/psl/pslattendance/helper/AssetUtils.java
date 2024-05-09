package com.psl.pslattendance.helper;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AssetUtils {
    public static String getSystemDateTimeInFormatt() {
        String systemDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        String date = dateFormat.format(calendar.getTime());
        String time = timeFormat.format(calendar.getTime());
        systemDate = (date+"\n"+time);
        Log.e("date", date);
        Log.e("time", time);
        return systemDate;
    }
}
