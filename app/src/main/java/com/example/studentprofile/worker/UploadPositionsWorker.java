package com.example.studentprofile.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.studentprofile.db.PositionDbHelper;
import com.example.studentprofile.model.PositionPoint;
import com.example.studentprofile.util.Constants;
import com.example.studentprofile.util.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class UploadPositionsWorker extends Worker {
    public UploadPositionsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static void enqueue(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(UploadPositionsWorker.class)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniqueWork("upload_positions", ExistingWorkPolicy.KEEP, request);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (!NetworkUtils.isNetworkAvailable(getApplicationContext())) {
            return Result.retry();
        }

        PositionDbHelper dbHelper = new PositionDbHelper(getApplicationContext());
        ArrayList<PositionPoint> points = dbHelper.getUnsyncedPositions();
        if (points.isEmpty()) {
            return Result.success();
        }

        HttpURLConnection connection = null;
        try {
            JSONArray array = new JSONArray();
            for (PositionPoint point : points) {
                JSONObject item = new JSONObject();
                item.put("id", point.getId());
                item.put("latitude", point.getLatitude());
                item.put("longitude", point.getLongitude());
                item.put("timestamp", point.getTimestamp());
                array.put(item);
            }

            JSONObject payload = new JSONObject();
            payload.put("positions", array);

            URL url = new URL(Constants.POSITIONS_UPLOAD_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(payload.toString().getBytes(StandardCharsets.UTF_8));
            }

            int code = connection.getResponseCode();
            if (code >= 200 && code < 300) {
                dbHelper.markAsSynced(points);
                Log.d("TRACKING", "Synchronisation réussie de " + points.size() + " positions.");
                return Result.success();
            }
            Log.e("TRACKING", "Erreur upload HTTP : " + code);
            return Result.retry();
        } catch (Exception e) {
            Log.e("TRACKING", "Erreur upload : " + e.getMessage());
            return Result.retry();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
