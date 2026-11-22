package org.coughlin.grocerylist;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Random;

/**
 * Fetches a stock food photo for a dish from the Pexels API and stores it
 * locally. Best-effort: any failure returns an empty path so menu generation
 * is never blocked by image fetching.
 */
public class ImageSearchClient {
    private static final String TAG = "ImageSearchClient";
    private static final String IMAGE_DIR = "dinner_images";
    private static final int RANDOM_POOL_SIZE = 15;
    private static final Random RANDOM = new Random();

    /**
     * Searches Pexels for {@code dishName}, downloads the first matching photo,
     * and saves it to internal storage keyed by {@code dailyMenuId}.
     *
     * @return the absolute local file path, or "" if no image could be fetched.
     */
    static String fetchAndStoreDinnerImage(Context context, String dishName, int dailyMenuId) {
        return fetchAndStoreDinnerImage(context, dishName, dailyMenuId, false);
    }

    /**
     * Like {@link #fetchAndStoreDinnerImage(Context, String, int)}, but when
     * {@code pickRandom} is true a random photo from the top results is used, so
     * asking again for the same dish gives a different picture.
     */
    static String fetchAndStoreDinnerImage(Context context, String dishName, int dailyMenuId,
                                           boolean pickRandom) {
        String apiKey = AppSettings.getPexelsApiKey(context);
        if (apiKey.isEmpty()) {
            Log.i(TAG, "No Pexels API key configured; skipping dinner image.");
            return "";
        }
        if (dishName == null || dishName.trim().isEmpty()) {
            return "";
        }

        try {
            String imageUrl = searchImageUrl(dishName, apiKey, pickRandom);
            if (imageUrl == null || imageUrl.isEmpty()) {
                Log.w(TAG, "No Pexels result for: " + dishName);
                return "";
            }
            return downloadToFile(context, imageUrl, dailyMenuId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to fetch dinner image for '" + dishName + "'", e);
            return "";
        }
    }

    private static String searchImageUrl(String dishName, String apiKey, boolean pickRandom)
            throws Exception {
        String endpoint = "https://api.pexels.com/v1/search?query="
                + URLEncoder.encode(dishName, "UTF-8")
                + "&per_page=" + (pickRandom ? RANDOM_POOL_SIZE : 1)
                + "&orientation=landscape";

        HttpURLConnection conn = null;
        try {
            URL url = new URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", apiKey);
            conn.setRequestProperty("Connection", "close");
            conn.setConnectTimeout(60000);
            conn.setReadTimeout(60000);

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Pexels API error " + responseCode);
                return "";
            }

            StringBuilder responseBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
            }

            JSONObject response = new JSONObject(responseBuilder.toString());
            JSONArray photos = response.optJSONArray("photos");
            if (photos == null || photos.length() == 0) {
                return "";
            }
            int index = pickRandom ? RANDOM.nextInt(photos.length()) : 0;
            JSONObject src = photos.getJSONObject(index).getJSONObject("src");
            // Prefer a medium-sized image; fall back to large or the original.
            if (src.has("large")) return src.getString("large");
            if (src.has("medium")) return src.getString("medium");
            return src.optString("original", "");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String downloadToFile(Context context, String imageUrl, int dailyMenuId)
            throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(imageUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Connection", "close");
            conn.setConnectTimeout(60000);
            conn.setReadTimeout(120000);

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "Image download error " + responseCode + " for " + imageUrl);
                return "";
            }

            File dir = new File(context.getFilesDir(), IMAGE_DIR);
            if (!dir.exists() && !dir.mkdirs()) {
                Log.e(TAG, "Could not create image directory: " + dir);
                return "";
            }
            File outFile = new File(dir, dailyMenuId + ".jpg");

            try (InputStream in = conn.getInputStream();
                 FileOutputStream out = new FileOutputStream(outFile)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
            }

            Log.i(TAG, "Saved dinner image to " + outFile.getAbsolutePath());
            return outFile.getAbsolutePath();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
