package com.jaynes.maxtv;

import android.app.Application;
import android.util.Log;

import com.onesignal.OneSignal;

/**
 * JaynesApp — Application class
 * Inainish OneSignal push notifications
 */
public class JaynesApp extends Application {

    private static final String TAG              = "JaynesApp";
    // Badilisha na OneSignal App ID yako halisi
    private static final String ONESIGNAL_APP_ID = "YOUR_ONESIGNAL_APP_ID";

    @Override
    public void onCreate() {
        super.onCreate();

        // OneSignal init
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.WARN, OneSignal.LOG_LEVEL.NONE);
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);

        // Omba ruhusa ya notifications (Android 13+)
        OneSignal.promptForPushNotifications();

        Log.d(TAG, "JAYNES MAX TV app initialized");
    }
}
