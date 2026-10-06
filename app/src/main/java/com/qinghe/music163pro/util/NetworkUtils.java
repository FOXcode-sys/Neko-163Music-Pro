package com.qinghe.music163pro.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Simple connectivity check used before firing off network requests (e.g.
 * searching), so we can show a friendly "no network" message instead of
 * letting the request fail silently or with a raw error.
 */
public final class NetworkUtils {

    private NetworkUtils() {
    }

    public static boolean isConnected(Context context) {
        if (context == null) return true; // fail-open: don't block on a bad context
        try {
            ConnectivityManager cm = (ConnectivityManager)
                    context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return true;
            NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected();
        } catch (Throwable t) {
            return true; // fail-open on unexpected errors
        }
    }
}
