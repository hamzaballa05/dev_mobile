package com.example.studentprofile.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.studentprofile.util.NetworkUtils;
import com.example.studentprofile.worker.UploadPositionsWorker;

public class ConnectivityReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (NetworkUtils.isNetworkAvailable(context)) {
            Log.d("TRACKING", "Connectivité détectée, demande de synchronisation.");
            UploadPositionsWorker.enqueue(context);
        }
    }
}
