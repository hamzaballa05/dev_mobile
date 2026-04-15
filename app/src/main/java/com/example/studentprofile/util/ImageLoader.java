package com.example.studentprofile.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ImageLoader {
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private ImageLoader() {
    }

    public static void loadWithHttp(String imageUrl, ImageView imageView) {
        if (TextUtils.isEmpty(imageUrl)) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            return;
        }

        EXECUTOR.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(imageUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setDoInput(true);
                connection.connect();
                try (InputStream inputStream = connection.getInputStream()) {
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    MAIN.post(() -> {
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                        } else {
                            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
                        }
                    });
                }
            } catch (Exception ignored) {
                MAIN.post(() -> imageView.setImageResource(android.R.drawable.ic_menu_gallery));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    public static void loadWithVolley(Context context, String imageUrl, ImageView imageView) {
        if (TextUtils.isEmpty(imageUrl)) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            return;
        }

        RequestQueue requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        ImageRequest request = new ImageRequest(
                imageUrl,
                imageView::setImageBitmap,
                0,
                0,
                ImageView.ScaleType.CENTER_CROP,
                Bitmap.Config.ARGB_8888,
                error -> imageView.setImageResource(android.R.drawable.ic_menu_gallery)
        );
        requestQueue.add(request);
    }
}
