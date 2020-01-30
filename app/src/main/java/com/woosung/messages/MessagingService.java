package com.woosung.messages;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.woosung.R;
import com.woosung.main.MainActivity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MessagingService extends FirebaseMessagingService {
    private static final String TAG = "MessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle FCM Message
        Log.e(TAG, remoteMessage.getFrom());
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Message data payload: " + remoteMessage.getData());
            handleNow();
        }


        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

            String getMessage = remoteMessage.getNotification().getBody();
            if (TextUtils.isEmpty(getMessage)) {
                Log.e(TAG, "ERR: Message data is empty...");
            } else {
                Map<String, String> mapMessage = new HashMap<>();
                assert getMessage != null;
                mapMessage.put("key", getMessage);


                sendNotification(mapMessage);

                // Broadcast Data Sending Test
                Intent intent = new Intent("alert_data");
                intent.putExtra("msg", getMessage);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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
//        Log.e(TAG, "Refreshed token: " + refreshedToken);
//        sendRegistrationToServer(refreshedToken);
    }


    /*
    private void sendRegistrationToServer(String token) {
        Log.e(TAG, "here ! sendRegistrationToServer! token is " + token);

        SharedPreferences pref = getSharedPreferences("Variable", Activity.MODE_PRIVATE);
        String empl = pref.getString("EMPLCODE", "");
        if(!empl.equals("")){
            OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("empl", empl)
                    .add("token", token)
                    .build();

            Request request = new Request.Builder()
                    .url(getString(R.string.url_token))
                    .post(formBody)
                    .build();

            client.newCall(request).enqueue(updateTokenCallback);
        }
    }

    private Callback updateTokenCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
//            Toast.makeText(getApplicationContext(),"ERROR on updating token : "+e.getMessage(),Toast.LENGTH_LONG ).show();
            Log.e(TAG, "ERROR Message : " + e.getMessage());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            final String responseData = response.body().string();
            if(responseData.equals("OK")) {
                Log.d(TAG, "SetToken :" + responseData);
            }else{
//                Toast.makeText(getApplicationContext(),"ERROR on updating token : "+responseData,Toast.LENGTH_LONG ).show();
                Log.e(TAG, "ERROR Message : " + responseData);
            }
        }
    };

    */


    private void sendNotification(Map<String, String> data) {
        int noti_id = 1;
        String getMessage = "";
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("notification_id", 0);

        // Push로 받은 데이터를 그대로 다시 intent에 넣어준다.
        if (data != null && data.size() > 0) {
            for (String key : data.keySet()) {
                getMessage = data.get(key);
                intent.putExtra(key, getMessage);
            }
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("FCM Message Test !")
                        .setContentText(getMessage)
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
