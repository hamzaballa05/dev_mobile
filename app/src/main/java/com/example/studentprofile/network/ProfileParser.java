package com.example.studentprofile.network;

import android.text.TextUtils;

import com.example.studentprofile.model.Note;
import com.example.studentprofile.model.Profile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public final class ProfileParser {
    private ProfileParser() {
    }

    public static Profile parse(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        if (root.has("error")) {
            throw new JSONException(root.optString("error"));
        }

        JSONObject source = root.optJSONObject("profile");
        if (source == null) {
            source = root;
        }

        Profile profile = new Profile();
        profile.setId(firstNonEmpty(source, "id", "studentId"));

        String firstName = firstNonEmpty(source, "prenom", "firstName", "firstname");
        String lastName = firstNonEmpty(source, "nom", "lastName", "lastname");
        String fullName = firstNonEmpty(source, "fullName", "fullname", "name", "username");
        if (TextUtils.isEmpty(fullName)) {
            fullName = (firstName + " " + lastName).trim();
        }
        if (TextUtils.isEmpty(fullName)) {
            fullName = "Étudiant";
        }

        profile.setFullName(fullName);
        profile.setEmail(firstNonEmpty(source, "email", "mail", "login"));
        profile.setPhone(firstNonEmpty(source, "telephone", "tel", "phone", "mobile"));
        profile.setRemarks(firstNonEmpty(source, "remarques", "remarks", "description", "bio"));
        profile.setImageUrl(firstNonEmpty(source, "image", "imageUrl", "photo", "avatar", "picture"));

        JSONArray notesArray = source.optJSONArray("notes");
        if (notesArray == null) {
            notesArray = root.optJSONArray("notes");
        }

        ArrayList<Note> notes = new ArrayList<>();
        if (notesArray != null) {
            for (int i = 0; i < notesArray.length(); i++) {
                Object rawNote = notesArray.get(i);
                if (rawNote instanceof JSONObject) {
                    JSONObject noteObject = (JSONObject) rawNote;
                    String subject = firstNonEmpty(noteObject, "matiere", "subject", "module", "name", "libelle");
                    double score = firstDouble(noteObject, "score", "note", "mark", "value");
                    notes.add(new Note(subject, score));
                }
            }
        }

        if (notes.isEmpty()) {
            notes.add(new Note("Android", 12.0));
        }

        if (TextUtils.isEmpty(profile.getRemarks())) {
            profile.setRemarks("Aucune remarque fournie par le service.");
        }

        profile.setNotes(notes);
        return profile;
    }

    private static String firstNonEmpty(JSONObject object, String... keys) {
        for (String key : keys) {
            String value = object.optString(key, "").trim();
            if (!TextUtils.isEmpty(value) && !"null".equalsIgnoreCase(value)) {
                return value;
            }
        }
        return "";
    }

    private static double firstDouble(JSONObject object, String... keys) {
        for (String key : keys) {
            if (object.has(key)) {
                return object.optDouble(key, 0.0);
            }
        }
        return 0.0;
    }
}
