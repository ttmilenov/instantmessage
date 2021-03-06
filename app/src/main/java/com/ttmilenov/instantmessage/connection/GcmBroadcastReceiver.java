package com.ttmilenov.instantmessage.connection;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.ttmilenov.instantmessage.Common;
import com.ttmilenov.instantmessage.Common;
import com.ttmilenov.instantmessage.activity.ChatActivity;
import com.ttmilenov.instantmessage.storage.DataProvider;
import com.ttmilenov.instantmessage.R;
import com.ttmilenov.instantmessage.activity.MainActivity;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Teodor Milenov on 4/9/2016.
 */
public class GcmBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "GcmBroadcastReceiver";
    private Context ctx;

    @Override
    public void onReceive(Context context, Intent intent) {
        ctx = context;
        String senderProfileId = null;
        PowerManager mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire();
        try {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
            String messageType = gcm.getMessageType(intent);
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error", senderProfileId, false);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server", senderProfileId, false);
            } else {
                String msg = intent.getStringExtra(DataProvider.COL_MESSAGE);
                String senderEmail = intent.getStringExtra(DataProvider.COL_SENDER_EMAIL);
                String receiverEmail = intent.getStringExtra(DataProvider.COL_RECEIVER_EMAIL);
                ContentValues values = new ContentValues(2);
                values.put(DataProvider.COL_TYPE, DataProvider.MessageType.INCOMING.ordinal());
                values.put(DataProvider.COL_MESSAGE, msg);
                values.put(DataProvider.COL_SENDER_EMAIL, senderEmail);
                values.put(DataProvider.COL_RECEIVER_EMAIL, receiverEmail);
                context.getContentResolver().insert(DataProvider.CONTENT_URI_MESSAGES, values);

                if (Common.isNotify()) {

                    Cursor cursor = context.getContentResolver().query(
                            DataProvider.CONTENT_URI_PROFILE,
                            new String[]{DataProvider.COL_ID},
                            DataProvider.COL_EMAIL + "= ?",
                            new String[]{senderEmail},
                            null);
                    if (cursor.moveToFirst()) {
                        senderProfileId = cursor.getString(cursor.getColumnIndex(DataProvider.COL_ID));
                    }
                    sendNotification("New message", senderProfileId, true);
                }
            }
            setResultCode(Activity.RESULT_OK);
        } finally {
            mWakeLock.release();
        }
    }

    private void sendNotification(String text, String senderProfileId, boolean launchApp) {
        NotificationManager mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(ctx);
        notification.setContentTitle(ctx.getString(R.string.app_name));
        notification.setContentText(text);
        notification.setAutoCancel(true);
        notification.setSmallIcon(R.drawable.ic_launcher);
        if (!TextUtils.isEmpty(Common.getRingtone())) {
            notification.setSound(Uri.parse(Common.getRingtone()));
        }

        if (launchApp) {
            Intent intent = null;
            if (senderProfileId != null) {
                intent = new Intent(ctx, ChatActivity.class);
                intent.putExtra(Common.PROFILE_ID, senderProfileId);
            } else {
                intent = new Intent(ctx, MainActivity.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pi = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            notification.setContentIntent(pi);
        }

        mNotificationManager.notify(1, notification.build());
    }

}
