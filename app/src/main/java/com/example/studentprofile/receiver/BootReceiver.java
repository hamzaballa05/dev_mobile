package com.example.studentprofile.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.studentprofile.service.TrackingService;
import com.example.studentprofile.util.NetworkUtils;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("TRACKING", "BOOT_COMPLETED reçu.");
            if (NetworkUtils.hasForegroundLocationPermission(context)) {
                TrackingService.start(context);
            } else {
                Log.d("TRACKING", "Permissions de localisation absentes après redémarrage.");
            }
        }
    }
}
