package org.coughlin.grocerylist;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sends token counts for each LLM call to the AI Usage Monitor on the home PC
 * (http://&lt;pc&gt;:8099/api/events). Copied from /development/AI-Usage/ai-usage-monitor/clients/java.
 *
 * Where it sends: the "usage_tracker_url" setting if set, otherwise the same host as the
 * Ollama URL on port 8099. With neither, nothing is sent. Reporting runs on its own thread,
 * never throws, and only sends model names and token counts (no prompts, replies or keys).
 */
public final class UsageReporter {
    private static final String TAG = "UsageReporter";
    public static final String KEY_USAGE_URL = "usage_tracker_url";
    private static final String PROJECT = "family-meal-management";
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();

    private UsageReporter() {}

    static String endpoint(Context context) {
        try {
            String url = AppSettings.getPrefs(context).getString(KEY_USAGE_URL, "").trim();
            if (!url.isEmpty()) return url;
            String ollama = AppSettings.getOllamaUrl(context).trim();
            if (ollama.isEmpty()) return null;
            String host = new URL(ollama).getHost();
            return host == null || host.isEmpty() ? null : "http://" + host + ":8099/api/events";
        } catch (Exception e) {
            return null;
        }
    }

    /** inputTokens is the whole prompt; cachedTokens is the part of it served from cache. */
    public static void report(Context context, String provider, String model,
                              long inputTokens, long cachedTokens, long outputTokens, long reasoningTokens) {
        final String target = endpoint(context);
        if (target == null) return;
        EXEC.execute(() -> {
            HttpURLConnection conn = null;
            try {
                SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                iso.setTimeZone(TimeZone.getTimeZone("UTC"));
                JSONObject ev = new JSONObject();
                ev.put("event_uid", UUID.randomUUID().toString());
                ev.put("ts", iso.format(new Date()));
                ev.put("project", PROJECT);
                ev.put("feature", "meal-planning");
                ev.put("provider", provider);
                ev.put("model", model == null ? "unknown" : model);
                ev.put("input_tokens", inputTokens);
                ev.put("cached_input_tokens", cachedTokens);
                ev.put("output_tokens", outputTokens);
                ev.put("reasoning_tokens", reasoningTokens);
                byte[] body = new JSONArray().put(ev).toString().getBytes(StandardCharsets.UTF_8);

                conn = (HttpURLConnection) new URL(target).openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                try (OutputStream os = conn.getOutputStream()) { os.write(body); }
                int code = conn.getResponseCode();
                if (code >= 300) Log.w(TAG, "Usage tracker answered HTTP " + code);
            } catch (Exception e) {
                Log.d(TAG, "Usage not recorded (tracker unreachable): " + e.getMessage());
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    /** OpenAI-compatible responses: OpenAI, Groq, xAI Grok. */
    public static void fromOpenAi(Context context, String provider, String model, JSONObject response) {
        JSONObject u = response.optJSONObject("usage");
        if (u == null) return;
        JSONObject pd = u.optJSONObject("prompt_tokens_details");
        JSONObject cd = u.optJSONObject("completion_tokens_details");
        report(context, provider, response.optString("model", model),
                u.optLong("prompt_tokens"), pd == null ? 0 : pd.optLong("cached_tokens"),
                u.optLong("completion_tokens"), cd == null ? 0 : cd.optLong("reasoning_tokens"));
    }

    public static void fromAnthropic(Context context, String model, JSONObject response) {
        JSONObject u = response.optJSONObject("usage");
        if (u == null) return;
        long read = u.optLong("cache_read_input_tokens"), write = u.optLong("cache_creation_input_tokens");
        report(context, "anthropic", response.optString("model", model),
                u.optLong("input_tokens") + read + write, read, u.optLong("output_tokens"), 0);
    }

    public static void fromGemini(Context context, String model, JSONObject response) {
        JSONObject u = response.optJSONObject("usageMetadata");
        if (u == null) return;
        long thoughts = u.optLong("thoughtsTokenCount");
        report(context, "google", model, u.optLong("promptTokenCount"), u.optLong("cachedContentTokenCount"),
                u.optLong("candidatesTokenCount") + thoughts, thoughts);
    }

    public static void fromOllama(Context context, String model, JSONObject response) {
        report(context, "ollama", response.optString("model", model),
                response.optLong("prompt_eval_count"), 0, response.optLong("eval_count"), 0);
    }
}
