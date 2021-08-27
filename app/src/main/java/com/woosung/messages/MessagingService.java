package com.woosung.messages;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.woosung.R;

import java.util.HashMap;
import java.util.Map;

public class MessagingService extends FirebaseMessagingService {
    private static final String TAG = "MessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle FCM Message
        Log.e(TAG, remoteMessage.getFrom());
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleNow();
        }


        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

            String sMessage = remoteMessage.getNotification().getBody();
            String sTitle = remoteMessage.getNotification().getTitle();
            if (TextUtils.isEmpty(sMessage)) {
                Log.e(TAG, "ERR: Message data is empty...");
            } else {
                Map<String, String> mapMessage = new HashMap<>();
                assert sMessage != null;
                mapMessage.put("title", sTitle);
                mapMessage.put("message", sMessage);


                sendNotification(mapMessage);

                // Broadcast Data Sending Test
                //Intent intent = new Intent("MESSAGE_LIST");
                //intent.putExtra("msg", sMessage);
                //LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }
        }
    }

    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }


    /**
     * 새로운 토큰이 생성되는 경우
     **/
    @Override
    public void onNewToken(String refreshedToken) {
        super.onNewToken(refreshedToken);
    }


    private void sendNotification(Map<String, String> data) {
        int noti_id = 1;
        String sMessage = "";
        String sTitle = "";
        Intent intent = new Intent(this, MessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("notification_id", 0);

        // Push로 받은 데이터를 그대로 다시 intent에 넣어준다.
        if (data != null && data.size() > 0) {
            for (String key : data.keySet()) {
                if(key.equals("title")) sTitle = data.get(key);
                else if(key.equals("message")) sMessage = data.get(key);

                intent.putExtra(key, sMessage);
            }
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_ws)
                        .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                        .setContentTitle(sTitle)
                        .setContentText(sMessage)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification 채널을 설정합니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(noti_id, notificationBuilder.build());
    }


}
