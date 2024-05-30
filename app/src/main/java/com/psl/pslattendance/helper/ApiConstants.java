package com.psl.pslattendance.helper;

public class ApiConstants {
    //APIURL
    public static final String URL = "https://pslattendanceapi.azurewebsites.net/";
    //public static final String URL = "http://192.168.0.113/Atten12/";
    //Method Name
    public static final String M_USER_LOGIN = "PSL/Login";
    public static final String M_USER_REG = "PSL/Register";
    public static final String M_USER_PUNCH_ACTIVITY = "PSL/InsertAttendanceActivity";
    public static final String M_GET_LOCATION = "PSL/GetAllLocation";
    public static final String M_GET_ACTIVITY = "PSL/GetActivity";

//Networking
public static final int API_TIMEOUT = 60;
    //Variable Name
    public static final String K_USER_DETAILS = "UserDetails";
    public static final String K_PASSWORD = "Password";
    public static final String K_DEVICE_ID = "DeviceID";
    public static final String K_FIRST_NAME = "FirstName";
    public static final String K_LAST_NAME = "LastName";
    public static final String K_EMAIL = "EmailID";
    public static final String K_CONTACT = "ContactNumber";
    public static final String K_EMP_ID = "EmployeeID";
    public static final String K_USER_ID = "UserID";
    public static final String K_ACTIVITY_TYPE = "Type";
    public static final String K_IMAGE_DATA = "ImageData";
    public static final String K_LOCATION_DATA = "LocationData";
    public static final String K_LOCATION_COORDINATE = "LocationCoordinates";
    public static final String K_PUNCH_DATE_TIME = "PunchDateTime";
    public static final String K_LOCATION_NAME = "LocationName";
    public static final String K_TRANS_ID = "PreviousTransactionID";
    public static final String K_STATUS = "status";
    public static final String K_MESSAGE = "message";
    public static final String K_DATA = "data";
}
