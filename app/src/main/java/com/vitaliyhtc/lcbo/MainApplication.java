package com.vitaliyhtc.lcbo;

import android.app.Application;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;

public class MainApplication extends Application {
    private static final String LOG_TAG = "LCBO MainApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        // https://github.com/square/leakcanary
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        // Normal app init code...



        verifyApiKey();
    }

    private void verifyApiKey(){
        if (Config.LCBO_API_ACCESS_KEY.isEmpty()) {
            Log.e(LOG_TAG, "Please obtain your API ACCESS_KEY first from lcboapi.com");
        }
    }
}
