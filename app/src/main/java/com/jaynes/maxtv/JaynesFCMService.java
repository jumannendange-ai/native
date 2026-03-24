package com.jaynes.maxtv;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * JaynesFCMService — Firebase Cloud Messaging handler
 * OneSignal inatumia FCM chini ya hood
 */
public class JaynesFCMService extends FirebaseMessagingService {

    private static final String TAG         = "JaynesFCM";
    private static final String CHANNEL_ID  = "jaynes_main";
    private static final String CHANNEL_NAME = "JAYNES MAX TV";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "FCM token mpya: " + token);
        // OneSignal inashughulikia token registration automatically
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d(TAG, "Ujumbe umefika kutoka: " + message.getFrom());

        // OneSignal inashughulikia notifications nyingi automatically
        // Hii inashughulikia data-only messages
        if (message.getNotification() == null && !message.getData().isEmpty()) {
            String title = message.getData().getOrDefault("title", "JAYNES MAX TV");
            String body  = message.getData().getOrDefault("message", "Habari mpya zimefika");
            showNotification(title, body);
        }
    }

    private void showNotification(String title, String body) {
        createChannel();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi);

        NotificationManager nm =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Arifa za JAYNES MAX TV");
            NotificationManager nm =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }
}
