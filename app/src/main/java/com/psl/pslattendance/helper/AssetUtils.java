package com.psl.pslattendance.helper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.psl.pslattendance.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
    public static String getSystemDateTimeInFormat() {
        String systemDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String date = dateFormat.format(calendar.getTime());
        String time = timeFormat.format(calendar.getTime());
        systemDate = (date+" "+time);
        Log.e("date", date);
        Log.e("time", time);
        return systemDate;
    }
    public static String getDate(String datetime) {
        String datePattern = "yyyy-MM-dd";
        String dateFormatPattern = "dd-MMM-yyyy";
        String date = "";

        SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern, Locale.getDefault());
        try {
            Date dateObj = dateFormat.parse(datetime.substring(0, 10));
            date = new SimpleDateFormat(dateFormatPattern, Locale.getDefault()).format(dateObj);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
    public static String getTime(String datetime) {
        String timePattern = "yyyy-MM-dd'T'HH:mm:ss";
        String timeFormatPattern = "hh:mm a";
        String time = "";

        SimpleDateFormat timeFormat = new SimpleDateFormat(timePattern, Locale.getDefault());
        try {
            Date timeObj = timeFormat.parse(datetime);
            time = new SimpleDateFormat(timeFormatPattern, Locale.getDefault()).format(timeObj);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }
    public static String getDayOfWeek(String datetime) {
        String datetimePattern = "yyyy-MM-dd";
        String dayOfWeek = "";

        SimpleDateFormat datetimeFormat = new SimpleDateFormat(datetimePattern, Locale.getDefault());
        try {
            Date date = datetimeFormat.parse(datetime.substring(0, 10));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            switch (day){
                case 1:
                    dayOfWeek = "Sunday";
                    break;
                case 2:
                    dayOfWeek = "Monday";
                    break;
                case 3:
                    dayOfWeek = "Tuesday";
                    break;
                case 4:
                    dayOfWeek = "Wednesday";
                    break;
                case 5:
                    dayOfWeek = "Thursday";
                    break;
                case 6:
                    dayOfWeek = "Friday";
                    break;
                case 7:
                    dayOfWeek = "Saturday";
                    break;
                default:
                    dayOfWeek = "";
                    break;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dayOfWeek;
    }
    public static String getSpecFormatDateTime(String datetime) {
        String dateTimePattern = "yyyy-MM-dd'T'HH:mm:ss";
        String formattedDateTime  = "";

        SimpleDateFormat timeFormat = new SimpleDateFormat(dateTimePattern, Locale.getDefault());
        try {
            Date timeObj = timeFormat.parse(datetime);
            formattedDateTime = timeObj.toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formattedDateTime;
    }
    static ProgressDialog progressDialog;

    /**
     * method to show Progress Dialog
     */
    public static void showProgress(Context context, String progress_message) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(progress_message);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);

        //progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    /**
     * method to hide Progress Dialog
     */
    public static void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
    public static void showAlertDialog(Context context, String title, String message) {
        // Create a new instance of the AlertDialog.Builder class
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title); //Set the title
        builder.setMessage(message);//Set the message of the dialog

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Implement the Code when OK Button is Clicked
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public static void showAlertDialogSpec(Context context, String title, String message, DialogInterface.OnClickListener onClickListener) {
        // Create a new instance of the AlertDialog.Builder class
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title); //Set the title
        builder.setMessage(message); //Set the message of the dialog
        builder.setPositiveButton("OK", onClickListener);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
