package org.coughlin.grocerylist;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Lists the text-generation models a provider offers for a given API key (or, for Ollama,
 * the models pulled on the server). Blocking; call from a background thread.
 */
public class ModelListClient {

    // Ids containing any of these are speech, image, embedding or safety models, not chat models.
    private static final String[] NON_CHAT_MARKERS = {
            "embed", "whisper", "tts", "audio", "realtime", "transcribe", "image", "imagine",
            "dall-e", "moderation", "guard", "orpheus", "babbage", "davinci", "aqa", "native-audio"
    };

    /**
     * @param credential the API key, or the Ollama server URL for {@link AppSettings#PROVIDER_OLLAMA}
     */
    public static List<String> listModels(String provider, String credential) throws Exception {
        List<String> models;
        switch (provider) {
            case AppSettings.PROVIDER_OLLAMA:
                models = listOllama(credential);
                break;
            case AppSettings.PROVIDER_GEMINI:
                models = listGemini(credential);
                break;
            case AppSettings.PROVIDER_CLAUDE:
                models = idsFromData(get("https://api.anthropic.com/v1/models?limit=1000",
                        headers("x-api-key", credential, "anthropic-version", "2023-06-01")));
                break;
            case AppSettings.PROVIDER_GROQ:
                models = idsFromData(get("https://api.groq.com/openai/v1/models",
                        headers("Authorization", "Bearer " + credential)));
                break;
            case AppSettings.PROVIDER_OPENAI:
                models = idsFromData(get("https://api.openai.com/v1/models",
                        headers("Authorization", "Bearer " + credential)));
                break;
            case AppSettings.PROVIDER_GROK:
                models = idsFromData(get("https://api.x.ai/v1/models",
                        headers("Authorization", "Bearer " + credential)));
                break;
            default:
                return Collections.emptyList();
        }

        List<String> chatModels = new ArrayList<>();
        for (String id : models) {
            if (isChatModel(id)) chatModels.add(id);
        }
        Collections.sort(chatModels);
        return chatModels;
    }

    private static boolean isChatModel(String id) {
        String lower = id.toLowerCase(Locale.ROOT);
        for (String marker : NON_CHAT_MARKERS) {
            if (lower.contains(marker)) return false;
        }
        return true;
    }

    private static List<String> listOllama(String serverUrl) throws Exception {
        // The saved URL points at /api/generate; the model list lives at /api/tags on the same host.
        URL tagsUrl = new URL(new URL(serverUrl), "/api/tags");
        JSONArray models = get(tagsUrl.toString(), new String[0]).getJSONArray("models");
        List<String> names = new ArrayList<>();
        for (int i = 0; i < models.length(); i++) {
            names.add(models.getJSONObject(i).getString("name"));
        }
        return names;
    }

    private static List<String> listGemini(String apiKey) throws Exception {
        JSONObject response = get(
                "https://generativelanguage.googleapis.com/v1beta/models?pageSize=1000",
                headers("x-goog-api-key", apiKey));
        JSONArray models = response.optJSONArray("models");
        List<String> names = new ArrayList<>();
        if (models == null) return names;
        for (int i = 0; i < models.length(); i++) {
            JSONObject model = models.getJSONObject(i);
            JSONArray methods = model.optJSONArray("supportedGenerationMethods");
            if (methods == null || !methods.toString().contains("\"generateContent\"")) continue;
            // Names come back as "models/gemini-..."; the generate endpoint wants the bare id.
            names.add(model.getString("name").replaceFirst("^models/", ""));
        }
        return names;
    }

    /** Reads {@code data[].id}, the shape used by the OpenAI-compatible and Anthropic APIs. */
    private static List<String> idsFromData(JSONObject response) throws Exception {
        JSONArray data = response.getJSONArray("data");
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            ids.add(data.getJSONObject(i).getString("id"));
        }
        return ids;
    }

    private static String[] headers(String... namesAndValues) {
        return namesAndValues;
    }

    /** @param headers alternating header names and values */
    private static JSONObject get(String endpoint, String[] headers) throws Exception {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(endpoint).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Connection", "close");
            for (int i = 0; i + 1 < headers.length; i += 2) {
                conn.setRequestProperty(headers[i], headers[i + 1]);
            }
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new LlmApiException(responseCode, "HTTP " + responseCode);
            }

            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            }
            return new JSONObject(body.toString());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
