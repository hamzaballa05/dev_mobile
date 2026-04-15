package com.example.studentprofile.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.studentprofile.model.Profile;
import com.example.studentprofile.util.Constants;
import com.example.studentprofile.util.NetworkUtils;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileRepository {
    public interface ProfileListener {
        void onSuccess(Profile profile, String sourceLabel);

        void onError(String message);
    }

    private final Context appContext;
    private final Handler mainHandler;
    private final ExecutorService executorService;
    private final RequestQueue requestQueue;

    public ProfileRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.executorService = Executors.newSingleThreadExecutor();
        this.requestQueue = Volley.newRequestQueue(appContext);
    }

    public void loadProfileWithHttp(String id, boolean includeNotes, ProfileListener listener) {
        executorService.execute(() -> {
            HttpURLConnection connection = null;
            try {
                if (!NetworkUtils.isNetworkAvailable(appContext)) {
                    throw new IllegalStateException("Aucune connexion Internet.");
                }

                String endpoint = NetworkUtils.buildProfileUrl(id, includeNotes);
                URL url = new URL(endpoint);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.connect();

                int code = connection.getResponseCode();
                if (code < 200 || code >= 300) {
                    throw new IllegalStateException("Réponse HTTP : " + code);
                }

                String response = NetworkUtils.readStream(new BufferedInputStream(connection.getInputStream()));
                Profile profile = ProfileParser.parse(response);
                mainHandler.post(() -> listener.onSuccess(profile, "Source : HttpURLConnection"));
            } catch (Exception e) {
                fallback(listener, "", e.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    public void loadProfileWithVolley(String id, boolean includeNotes, ProfileListener listener) {
        String endpoint = NetworkUtils.buildProfileUrl(id, includeNotes);
        StringRequest request = new StringRequest(
                Request.Method.GET,
                endpoint,
                response -> {
                    try {
                        Profile profile = ProfileParser.parse(response);
                        listener.onSuccess(profile, "Source : Volley");
                    } catch (Exception e) {
                        fallback(listener, "Source : JSON local (fallback Volley)", e.getMessage());
                    }
                },
                error -> fallback(listener, "Source : JSON local (fallback Volley)", error.getMessage())
        );
        requestQueue.add(request);
    }

    private void fallback(ProfileListener listener, String sourceLabel, String originalError) {
        executorService.execute(() -> {
            try {
                String response = NetworkUtils.readAsset(appContext, Constants.SAMPLE_PROFILE_ASSET);
                Profile profile = ProfileParser.parse(response);
                mainHandler.post(() -> listener.onSuccess(profile, sourceLabel));
            } catch (Exception assetError) {
                String message = "Échec de chargement du profil : " + originalError;
                mainHandler.post(() -> listener.onError(message));
            }
        });
    }
}
