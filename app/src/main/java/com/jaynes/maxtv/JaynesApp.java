package com.jaynes.maxtv;

import android.app.Application;
import android.util.Log;

import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;

public class JaynesApp extends Application {

    private static final String TAG = "JaynesApp";
    private static final String ONESIGNAL_APP_ID = "YOUR_ONESIGNAL_APP_ID";

    @Override
    public void onCreate() {
        super.onCreate();

        // OneSignal v5 API
        OneSignal.getDebug().setLogLevel(LogLevel.WARN);
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);

        Log.d(TAG, "JAYNES MAX TV app initialized");
    }
}
