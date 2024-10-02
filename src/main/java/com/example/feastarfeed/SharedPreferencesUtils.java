package com.example.feastarfeed;

import android.content.Context;
import android.content.SharedPreferences;
public class SharedPreferencesUtils {
    private static final String PREF_NAME = "UserData";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_USERNAME = "username";
    public static final String VIDEO_TAGS = "video_tags";


    private static final String PREF_PARAM_LOW = "parameterTimeLOW";
    private static final String PREF_PARAM_HIGH = "parameterTimeHIGH";
    private static final String PREF_PARAMTimeScore = "parameterTimeScore";
    private static final String PREF_PARAM_RE = "parameterVideoListRE";
    private static final String PREF_PARAM_DAD = "parameterAdDAD";
    private static final String PREF_PARAM_LIKE = "parameterLIKE";
    private static final String PREF_PARAM_TAG = "parameterTAG";
    private SharedPreferencesUtils() {
        // 私有構造函數,防止實例化
    }
    public static void saveVideotag(Context context, String video_tag) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(VIDEO_TAGS, video_tag);
        editor.apply();
    }
    public static String getVideotag(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(VIDEO_TAGS, "");
    }
    public static void clearVideotag(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(VIDEO_TAGS);
        editor.apply();
    }

    public static void saveUserData(Context context, String email, String password, String username) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_USERNAME, username); // 儲存 username
        editor.apply();
    }

    public static void clearUserData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_PASSWORD);
        editor.apply();
    }

    public static String getEmail(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_EMAIL, "");
    }

    public static String getPassword(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_PASSWORD, "");
    }
    public static String getUsername(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USERNAME, "");
    }
    public static void savechangeparameter(Context context, long value1, long value2, long value3, long value4, long value5, long value6, long value7) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PREF_PARAM_LOW, value1);
        editor.putLong(PREF_PARAM_HIGH, value2);
        editor.putLong(PREF_PARAMTimeScore, value3);
        editor.putLong(PREF_PARAM_RE, value4);
        editor.putLong(PREF_PARAM_DAD, value5);
        editor.putLong(PREF_PARAM_LIKE, value6);
        editor.putLong(PREF_PARAM_TAG, value7);
        editor.apply();
    }
    public static long getPARALow(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(PREF_PARAM_LOW, 4);
    }
    public static long getPARAHigh(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(PREF_PARAM_HIGH, 8);
    }
    public static long getPARATimeScore(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(PREF_PARAMTimeScore, 5);
    }
    public static long getPARA_RE(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(PREF_PARAM_RE,1);
    }
    public static long getPARA_DAD(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(PREF_PARAM_DAD, 1);
    }
    public static long getPARA_Like(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(PREF_PARAM_LIKE, 10);
    }
    public static long getPARA_TAG(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(PREF_PARAM_TAG, 15);
    }

}
