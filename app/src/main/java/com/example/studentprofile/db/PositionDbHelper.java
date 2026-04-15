package com.example.studentprofile.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.studentprofile.model.PositionPoint;

import java.util.ArrayList;
import java.util.List;

public class PositionDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "tracking.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_POSITIONS = "positions";
    private static final String COL_ID = "id";
    private static final String COL_LAT = "latitude";
    private static final String COL_LON = "longitude";
    private static final String COL_TIMESTAMP = "ts";
    private static final String COL_SYNCED = "synced";

    public PositionDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_POSITIONS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_LAT + " REAL NOT NULL, "
                + COL_LON + " REAL NOT NULL, "
                + COL_TIMESTAMP + " INTEGER NOT NULL, "
                + COL_SYNCED + " INTEGER NOT NULL DEFAULT 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSITIONS);
        onCreate(db);
    }

    public long insertPosition(double latitude, double longitude, long timestamp) {
        ContentValues values = new ContentValues();
        values.put(COL_LAT, latitude);
        values.put(COL_LON, longitude);
        values.put(COL_TIMESTAMP, timestamp);
        values.put(COL_SYNCED, 0);
        return getWritableDatabase().insert(TABLE_POSITIONS, null, values);
    }

    public ArrayList<PositionPoint> getUnsyncedPositions() {
        ArrayList<PositionPoint> points = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(
                TABLE_POSITIONS,
                null,
                COL_SYNCED + " = 0",
                null,
                null,
                null,
                COL_TIMESTAMP + " ASC"
        );

        try {
            int idxId = cursor.getColumnIndexOrThrow(COL_ID);
            int idxLat = cursor.getColumnIndexOrThrow(COL_LAT);
            int idxLon = cursor.getColumnIndexOrThrow(COL_LON);
            int idxTs = cursor.getColumnIndexOrThrow(COL_TIMESTAMP);
            while (cursor.moveToNext()) {
                points.add(new PositionPoint(
                        cursor.getLong(idxId),
                        cursor.getDouble(idxLat),
                        cursor.getDouble(idxLon),
                        cursor.getLong(idxTs)
                ));
            }
        } finally {
            cursor.close();
        }
        return points;
    }

    public void markAsSynced(List<PositionPoint> points) {
        if (points == null || points.isEmpty()) {
            return;
        }

        StringBuilder placeholders = new StringBuilder();
        String[] args = new String[points.size()];
        for (int i = 0; i < points.size(); i++) {
            if (i > 0) {
                placeholders.append(',');
            }
            placeholders.append('?');
            args[i] = String.valueOf(points.get(i).getId());
        }

        ContentValues values = new ContentValues();
        values.put(COL_SYNCED, 1);
        getWritableDatabase().update(
                TABLE_POSITIONS,
                values,
                COL_ID + " IN (" + placeholders + ")",
                args
        );
    }
}
