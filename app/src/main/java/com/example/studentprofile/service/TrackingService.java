package com.example.studentprofile.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;
import androidx.core.content.ContextCompat;

import com.example.studentprofile.MainActivity;
import com.example.studentprofile.R;
import com.example.studentprofile.db.PositionDbHelper;
import com.example.studentprofile.receiver.ConnectivityReceiver;
import com.example.studentprofile.util.Constants;
import com.example.studentprofile.util.NetworkUtils;
import com.example.studentprofile.worker.UploadPositionsWorker;

public class TrackingService extends Service implements LocationListener {
    private LocationManager locationManager;
    private PositionDbHelper dbHelper;
    private ConnectivityReceiver connectivityReceiver;

    public static void start(Context context) {
        Intent intent = new Intent(context, TrackingService.class);
        try {
            ContextCompat.startForegroundService(context, intent);
        } catch (Exception e) {
            Log.e("TRACKING", "Impossible de démarrer le service : " + e.getMessage());
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new PositionDbHelper(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        createNotificationChannel();
        registerConnectivityReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForegroundMode();
        requestLocationUpdatesSafe();
        UploadPositionsWorker.enqueue(this);
        return START_STICKY;
    }

    private void startForegroundMode() {
        Intent openAppIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constants.TRACKING_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentTitle("Tracking GPS actif")
                .setContentText("Les positions GPS sont enregistrées localement.")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        int foregroundType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ? ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                : 0;

        ServiceCompat.startForeground(this, Constants.TRACKING_NOTIFICATION_ID, builder.build(), foregroundType);
    }

    private void registerConnectivityReceiver() {
        connectivityReceiver = new ConnectivityReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        ContextCompat.registerReceiver(this, connectivityReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private void requestLocationUpdatesSafe() {
        if (!NetworkUtils.hasForegroundLocationPermission(this) || locationManager == null) {
            Log.d("TRACKING", "Permissions GPS absentes, suivi non démarré.");
            return;
        }

        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60_000L, 10f, this);
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60_000L, 10f, this);
            }
        } catch (SecurityException e) {
            Log.e("TRACKING", "Erreur permission GPS : " + e.getMessage());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    Constants.TRACKING_CHANNEL_ID,
                    "Tracking GPS",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            return;
        }
        dbHelper.insertPosition(location.getLatitude(), location.getLongitude(), System.currentTimeMillis());
        Log.d("TRACKING", "Position enregistrée : " + location.getLatitude() + ", " + location.getLongitude());
        if (NetworkUtils.isNetworkAvailable(this)) {
            UploadPositionsWorker.enqueue(this);
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("TRACKING", provider + " activé");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("TRACKING", provider + " désactivé");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Compatibilité API anciennes.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (locationManager != null) {
                locationManager.removeUpdates(this);
            }
            if (connectivityReceiver != null) {
                unregisterReceiver(connectivityReceiver);
            }
        } catch (Exception ignored) {
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
