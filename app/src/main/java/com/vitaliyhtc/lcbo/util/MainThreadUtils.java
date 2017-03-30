package com.vitaliyhtc.lcbo.util;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by zolotar on 28/02/17.
 */

public class MainThreadUtils {

    private static final Handler mUIHandler = new Handler(Looper.getMainLooper());

    public static void post(Runnable runnable) {
        mUIHandler.post(runnable);
    }

    public static void postDelayed(Runnable runnable, long delay) {
        mUIHandler.postDelayed(runnable, delay);
    }
}