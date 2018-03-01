package com.future.bestmovies.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private static final String TAG = NetworkChangeReceiver.class.getSimpleName();
    private static final String CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        int status = NetworkUtils.getConnectivityStatusString(context);
        Log.e(TAG, "Network receiver");
        if (!CONNECTIVITY_CHANGE.equals(intent.getAction())) {
            if(status == NetworkUtils.NETWORK_STATUS_NOT_CONNECTED){
                Log.e(TAG, "Network not connected");
                //new ForceExitPause(context).execute();
            } else {
                Log.e(TAG, "Network connected");
                //new ResumeForceExitPause(context).execute();
            }

        }
    }
}
