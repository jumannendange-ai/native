package com.jaynes.maxtv;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;

public class JaynesApp extends Application {

    public static final String ONESIGNAL_APP_ID = "10360777-3ada-4145-b83f-00eb0312a53f";
    public static final String API_BASE         = "https://api-jaynestvmax2.onrender.com";
    public static final String WEB_BASE         = "https://dde.ct.ws";
    public static final String CHANNEL_ID       = "jaynes_channel";

    @Override
    public void onCreate() {
        super.onCreate();

        // Notification Channel (Android 8+)
        createNotificationChannel();

        // OneSignal Init
        OneSignal.getDebug().setLogLevel(LogLevel.NONE);
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);
        OneSignal.getNotifications().requestPermission(true, Continue.none());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "JAYNES MAX TV",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Taarifa za JAYNES MAX TV");
            channel.enableVibration(true);
            channel.setShowBadge(true);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }
}
