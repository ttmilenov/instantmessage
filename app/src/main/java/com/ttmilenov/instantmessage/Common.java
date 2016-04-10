package com.ttmilenov.instantmessage;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Patterns;

import com.ttmilenov.instantmessage.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Teodor Milenov on 4/9/2016.
 */
public class Common extends Application {

    public static final String PROFILE_ID = "profile_id";
    public static final String ACTION_REGISTER = "com.ttmilenov.instantmessage.REGISTER";
    public static final String EXTRA_STATUS = "status";
    public static final int STATUS_SUCCESS = 1;
    public static final int STATUS_FAILED = 0;
    public static final String APPLICATION_TITLE = "InstantMessage";

    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    public static final int PERMISSIONS_REQUEST_GET_ACCOUNTS = 2;
    public static final int PERMISSIONS_REQUEST_INTERNET = 3;
    public static final int PERMISSIONS_REQUEST_WAKE_LOCK = 4;
    public static final int PERMISSIONS_REQUEST_RECEIVE = 5;

    private static SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private List<String> getEmailList() {
        List<String> lst = new ArrayList<>();

        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (Patterns.EMAIL_ADDRESS.matcher(account.name).matches()) {
                lst.add(account.name);
            }
        }
        return lst;
    }

    public static String getPreferredEmail(String[] email_arr) {
        return prefs.getString("chat_email_id", email_arr.length == 0 ? "" : email_arr[0]);
    }

//    public static String getDisplayName() {
//        String email = getPreferredEmail();
//        return prefs.getString("display_name", email.substring(0, email.indexOf('@')));
//    }

    public static boolean isNotify() {
        return prefs.getBoolean("notifications_new_message", true);
    }

    public static String getRingtone() {
        return prefs.getString("notifications_new_message_ringtone",
                android.provider.Settings.System.DEFAULT_NOTIFICATION_URI.toString());
    }

    public static String getServerUrl() {
        return prefs.getString("server_url_pref", Constants.SERVER_URL);
    }

    public static String getSenderId() {
        return prefs.getString("sender_id_pref", Constants.SENDER_ID);
    }

    public static boolean requestPermission(Activity activity, String permission, int permissionId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && activity.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{ permission }, permissionId);
            return false;
        }
        return true;
    }

}
