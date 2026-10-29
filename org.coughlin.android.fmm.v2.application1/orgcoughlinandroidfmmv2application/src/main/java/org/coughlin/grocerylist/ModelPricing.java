package org.coughlin.grocerylist;

import android.content.Context;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Looks up per-token prices for provider models. None of the providers' model-list APIs report
 * prices, so this reads LiteLLM's community-maintained price table, cached for a day.
 * Blocking; call from a background thread.
 */
public class ModelPricing {

    private static final String TAG = "ModelPricing";
    private static final String PRICES_URL =
            "https://raw.githubusercontent.com/BerriAI/litellm/main/model_prices_and_context_window.json";
    private static final String CACHE_FILE = "model_prices.json";
    private static final long CACHE_MAX_AGE_MS = 24L * 60 * 60 * 1000;

    /**
     * Returns a short cost label per model id ("$0.15 in / $0.60 out per 1M tokens", "Free"),
     * omitting models the price table doesn't list. Never throws; returns what it can.
     */
    public static Map<String, String> describeCosts(Context context, String provider,
                                                    List<String> modelIds) {
        Map<String, String> labels = new HashMap<>();
        if (AppSettings.PROVIDER_OLLAMA.equals(provider)) {
            for (String id : modelIds) {
                labels.put(id, context.getString(R.string.settings_model_cost_local));
            }
            return labels;
        }

        File prices = pricesFile(context);
        if (prices == null) return labels;

        // The table keys most models as "<provider>/<id>"; OpenAI and Anthropic also use the bare id.
        String prefix = tablePrefix(provider);
        Map<String, String> keyToId = new HashMap<>();
        for (String id : modelIds) {
            keyToId.put(prefix + id, id);
            keyToId.putIfAbsent(id, id);
        }

        try {
            Map<String, double[]> costs = readCosts(prices, keyToId.keySet());
            for (String id : modelIds) {
                double[] cost = costs.get(prefix + id);
                if (cost == null) cost = costs.get(id);
                if (cost != null) labels.put(id, formatCost(context, cost[0], cost[1]));
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not read model prices", e);
        }
        return labels;
    }

    private static String tablePrefix(String provider) {
        switch (provider) {
            case AppSettings.PROVIDER_GEMINI: return "gemini/";
            case AppSettings.PROVIDER_GROQ:   return "groq/";
            case AppSettings.PROVIDER_GROK:   return "xai/";
            case AppSettings.PROVIDER_CLAUDE: return "anthropic/";
            case AppSettings.PROVIDER_OPENAI: return "openai/";
            default:                          return "";
        }
    }

    private static String formatCost(Context context, double inputPerToken, double outputPerToken) {
        if (inputPerToken == 0 && outputPerToken == 0) {
            return context.getString(R.string.settings_model_cost_free);
        }
        return context.getString(R.string.settings_model_cost,
                perMillion(inputPerToken), perMillion(outputPerToken));
    }

    private static String perMillion(double perToken) {
        double dollars = perToken * 1_000_000;
        return String.format(Locale.US, dollars < 1 ? "$%.3f" : "$%.2f", dollars)
                .replaceAll("(\\.\\d\\d)0$", "$1");
    }

    /** Returns the cached price table, downloading it when missing or a day old. */
    private static File pricesFile(Context context) {
        File cached = new File(context.getCacheDir(), CACHE_FILE);
        boolean fresh = cached.exists()
                && System.currentTimeMillis() - cached.lastModified() < CACHE_MAX_AGE_MS;
        if (fresh) return cached;

        File partial = new File(context.getCacheDir(), CACHE_FILE + ".tmp");
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(PRICES_URL).openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new Exception("HTTP " + conn.getResponseCode());
            }
            try (InputStream in = conn.getInputStream();
                 OutputStream out = new FileOutputStream(partial)) {
                byte[] buffer = new byte[16384];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
            if (!partial.renameTo(cached)) throw new Exception("Could not save price table");
            return cached;
        } catch (Exception e) {
            Log.w(TAG, "Could not download model prices", e);
            partial.delete();
            // A stale table beats no prices at all.
            return cached.exists() ? cached : null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /**
     * Streams the ~3 MB table, keeping {input, output} cost per token for the wanted keys only,
     * so the whole file is never held in memory.
     */
    private static Map<String, double[]> readCosts(File prices, Collection<String> wantedKeys)
            throws Exception {
        Set<String> wanted = new HashSet<>(wantedKeys);
        Map<String, double[]> costs = new HashMap<>();
        try (JsonReader reader = new JsonReader(
                new InputStreamReader(new FileInputStream(prices), "UTF-8"))) {
            reader.beginObject();
            while (reader.hasNext()) {
                String key = reader.nextName();
                if (!wanted.contains(key) || reader.peek() != JsonToken.BEGIN_OBJECT) {
                    reader.skipValue();
                    continue;
                }
                Double input = null;
                Double output = null;
                reader.beginObject();
                while (reader.hasNext()) {
                    String field = reader.nextName();
                    if ("input_cost_per_token".equals(field) && reader.peek() == JsonToken.NUMBER) {
                        input = reader.nextDouble();
                    } else if ("output_cost_per_token".equals(field)
                            && reader.peek() == JsonToken.NUMBER) {
                        output = reader.nextDouble();
                    } else {
                        reader.skipValue();
                    }
                }
                reader.endObject();
                if (input != null && output != null) {
                    costs.put(key, new double[] {input, output});
                }
            }
            reader.endObject();
        }
        return costs;
    }
}
