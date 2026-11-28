package org.coughlin.grocerylist;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Keeps the notes shared into the app (Google Keep → Share → Family Meals) as a small
 * JSON list in SharedPreferences, so no database migration is needed.
 * Newest first, at most {@link #MAX_NOTES}; a note with the same title replaces the old copy.
 */
public final class ReferenceNoteStore {

    public static final int MAX_NOTES = 10;
    public static final int MAX_TEXT_CHARS = 4000;

    private static final String KEY_NOTES = "reference_notes";

    private ReferenceNoteStore() { }

    public static synchronized List<ReferenceNote> list(Context context) {
        List<ReferenceNote> notes = new ArrayList<>();
        String json = prefs(context).getString(KEY_NOTES, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                notes.add(new ReferenceNote(o.optString("id"), o.optString("title"),
                        o.optString("text"), o.optLong("savedAt")));
            }
        } catch (JSONException ignored) {
            // Corrupt data: treat as empty rather than crash.
        }
        return notes;
    }

    /** Saves a note and returns it, or null if the text is blank. */
    public static synchronized ReferenceNote add(Context context, String title, String text) {
        if (text == null || text.trim().isEmpty()) return null;
        String trimmed = text.trim();
        if (trimmed.length() > MAX_TEXT_CHARS) trimmed = trimmed.substring(0, MAX_TEXT_CHARS);
        String cleanTitle = title == null ? "" : title.trim();

        ReferenceNote note = new ReferenceNote(UUID.randomUUID().toString(), cleanTitle,
                trimmed, System.currentTimeMillis());

        List<ReferenceNote> notes = list(context);
        List<ReferenceNote> kept = new ArrayList<>();
        kept.add(note);
        for (ReferenceNote n : notes) {
            boolean sameTitle = !cleanTitle.isEmpty() && cleanTitle.equalsIgnoreCase(n.title);
            boolean sameText = n.text.equals(trimmed);
            if (sameTitle || sameText) continue;
            if (kept.size() >= MAX_NOTES) break;
            kept.add(n);
        }
        save(context, kept);
        return note;
    }

    public static synchronized void remove(Context context, String id) {
        List<ReferenceNote> kept = new ArrayList<>();
        for (ReferenceNote n : list(context)) {
            if (!n.id.equals(id)) kept.add(n);
        }
        save(context, kept);
    }

    private static void save(Context context, List<ReferenceNote> notes) {
        JSONArray arr = new JSONArray();
        try {
            for (ReferenceNote n : notes) {
                JSONObject o = new JSONObject();
                o.put("id", n.id);
                o.put("title", n.title);
                o.put("text", n.text);
                o.put("savedAt", n.savedAt);
                arr.put(o);
            }
        } catch (JSONException ignored) {
            return;
        }
        prefs(context).edit().putString(KEY_NOTES, arr.toString()).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return AppSettings.getPrefs(context);
    }
}
