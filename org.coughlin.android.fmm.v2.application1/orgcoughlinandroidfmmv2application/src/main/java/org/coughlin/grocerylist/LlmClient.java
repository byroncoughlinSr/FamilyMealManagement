package org.coughlin.grocerylist;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LlmClient {
    private static final String TAG = "LlmClient";

    public static String callLlm(Context context, String prompt) throws Exception {
        String provider = AppSettings.getLlmProvider(context);
        Log.i(TAG, "Using LLM provider: " + provider);
        if (AppSettings.PROVIDER_ON_DEVICE.equals(provider)) {
            return OnDeviceLlmClient.generate(context, prompt);
        } else if (AppSettings.PROVIDER_GEMINI.equals(provider)) {
            return callGemini(context, prompt);
        } else if (AppSettings.PROVIDER_CLAUDE.equals(provider)) {
            return callClaude(context, prompt);
        } else if (AppSettings.PROVIDER_GROQ.equals(provider)) {
            return callGroq(context, prompt);
        } else if (AppSettings.PROVIDER_OPENAI.equals(provider)) {
            return callOpenAi(context, prompt);
        } else if (AppSettings.PROVIDER_GROK.equals(provider)) {
            return callGrok(context, prompt);
        }
        return callOllama(context, prompt);
    }

    static String callOllama(Context context, String prompt) throws Exception {
        String ollamaUrl = AppSettings.getOllamaUrl(context);
        if (ollamaUrl.isEmpty()) {
            throw new Exception("Ollama server URL is not configured. Set it in Settings.");
        }
        HttpURLConnection conn = null;
        try {
            URL url = new URL(ollamaUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Connection", "close");
            conn.setDoOutput(true);
            conn.setConnectTimeout(60000);
            conn.setReadTimeout(600000);

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", AppSettings.getOllamaModel(context));
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);
            requestBody.put("format", "json");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.toString().getBytes("UTF-8"));
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                StringBuilder errorBody = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorBody.append(line);
                    }
                } catch (Exception ignored) {}
                Log.e(TAG, "Ollama API error " + responseCode + ": " + errorBody);
                throw new LlmApiException(responseCode, "Ollama API error: " + responseCode + " - " + errorBody);
            }

            StringBuilder responseBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
            }

            JSONObject ollamaResponse = new JSONObject(responseBuilder.toString());
            return ollamaResponse.getString("response");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    static String callGemini(Context context, String prompt) throws Exception {
        String apiKey = AppSettings.getApiKey(context);
        if (apiKey.isEmpty()) {
            throw new Exception("Gemini API key is not configured. Set it in Settings.");
        }

        String model = AppSettings.getGeminiModel(context);
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        HttpURLConnection conn = null;
        try {
            URL url = new URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Connection", "close");
            conn.setDoOutput(true);
            conn.setConnectTimeout(60000);
            conn.setReadTimeout(600000);

            JSONObject textPart = new JSONObject();
            textPart.put("text", prompt);

            JSONArray parts = new JSONArray();
            parts.put(textPart);

            JSONObject content = new JSONObject();
            content.put("parts", parts);

            JSONArray contents = new JSONArray();
            contents.put(content);

            JSONObject generationConfig = new JSONObject();
            generationConfig.put("responseMimeType", "application/json");

            JSONObject requestBody = new JSONObject();
            requestBody.put("contents", contents);
            requestBody.put("generationConfig", generationConfig);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.toString().getBytes("UTF-8"));
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                StringBuilder errorBody = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorBody.append(line);
                    }
                } catch (Exception ignored) {}
                Log.e(TAG, "Gemini API error " + responseCode + ": " + errorBody);
                throw new LlmApiException(responseCode, "Gemini API error: " + responseCode + " - " + errorBody);
            }

            StringBuilder responseBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
            }

            JSONObject geminiResponse = new JSONObject(responseBuilder.toString());
            JSONArray candidates = geminiResponse.getJSONArray("candidates");
            JSONObject firstCandidate = candidates.getJSONObject(0);
            JSONObject candidateContent = firstCandidate.getJSONObject("content");
            JSONArray candidateParts = candidateContent.getJSONArray("parts");
            return candidateParts.getJSONObject(0).getString("text");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    static String callClaude(Context context, String prompt) throws Exception {
        String apiKey = AppSettings.getClaudeApiKey(context);
        if (apiKey.isEmpty()) {
            throw new Exception("Claude API key is not configured. Set it in Settings.");
        }

        String model = AppSettings.getClaudeModel(context);
        String endpoint = "https://api.anthropic.com/v1/messages";

        HttpURLConnection conn = null;
        try {
            URL url = new URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-api-key", apiKey);
            conn.setRequestProperty("anthropic-version", "2023-06-01");
            conn.setRequestProperty("Connection", "close");
            conn.setDoOutput(true);
            conn.setConnectTimeout(60000);
            conn.setReadTimeout(600000);

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);

            JSONArray messages = new JSONArray();
            messages.put(userMessage);

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 4096);
            requestBody.put("messages", messages);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.toString().getBytes("UTF-8"));
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                StringBuilder errorBody = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorBody.append(line);
                    }
                } catch (Exception ignored) {}
                Log.e(TAG, "Claude API error " + responseCode + ": " + errorBody);
                throw new LlmApiException(responseCode, "Claude API error: " + responseCode + " - " + errorBody);
            }

            StringBuilder responseBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
            }

            JSONObject claudeResponse = new JSONObject(responseBuilder.toString());
            JSONArray content = claudeResponse.getJSONArray("content");
            return content.getJSONObject(0).getString("text");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    static String callGroq(Context context, String prompt) throws Exception {
        String apiKey = AppSettings.getGroqApiKey(context);
        if (apiKey.isEmpty()) {
            throw new Exception("Groq API key is not configured. Set it in Settings.");
        }

        String model = AppSettings.getGroqModel(context);
        String endpoint = "https://api.groq.com/openai/v1/chat/completions";

        HttpURLConnection conn = null;
        try {
            URL url = new URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Connection", "close");
            conn.setDoOutput(true);
            conn.setConnectTimeout(60000);
            conn.setReadTimeout(600000);

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);

            JSONArray messages = new JSONArray();
            messages.put(userMessage);

            JSONObject responseFormat = new JSONObject();
            responseFormat.put("type", "json_object");

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("response_format", responseFormat);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.toString().getBytes("UTF-8"));
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                StringBuilder errorBody = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorBody.append(line);
                    }
                } catch (Exception ignored) {}
                Log.e(TAG, "Groq API error " + responseCode + ": " + errorBody);
                throw new LlmApiException(responseCode, "Groq API error: " + responseCode + " - " + errorBody);
            }

            StringBuilder responseBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
            }

            JSONObject groqResponse = new JSONObject(responseBuilder.toString());
            JSONArray choices = groqResponse.getJSONArray("choices");
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            return message.getString("content");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    static String callOpenAi(Context context, String prompt) throws Exception {
        String apiKey = AppSettings.getOpenAiApiKey(context);
        if (apiKey.isEmpty()) {
            throw new Exception("OpenAI API key is not configured. Set it in Settings.");
        }
        return callOpenAiCompatible(
                "https://api.openai.com/v1/chat/completions",
                apiKey,
                AppSettings.getOpenAiModel(context),
                prompt,
                "OpenAI");
    }

    static String callGrok(Context context, String prompt) throws Exception {
        String apiKey = AppSettings.getGrokApiKey(context);
        if (apiKey.isEmpty()) {
            throw new Exception("Grok API key is not configured. Set it in Settings.");
        }
        return callOpenAiCompatible(
                "https://api.x.ai/v1/chat/completions",
                apiKey,
                AppSettings.getGrokModel(context),
                prompt,
                "Grok");
    }

    private static String callOpenAiCompatible(String endpoint, String apiKey, String model,
                                               String prompt, String providerName) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Connection", "close");
            conn.setDoOutput(true);
            conn.setConnectTimeout(60000);
            conn.setReadTimeout(600000);

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);

            JSONArray messages = new JSONArray();
            messages.put(userMessage);

            JSONObject responseFormat = new JSONObject();
            responseFormat.put("type", "json_object");

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("response_format", responseFormat);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.toString().getBytes("UTF-8"));
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                StringBuilder errorBody = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorBody.append(line);
                    }
                } catch (Exception ignored) {}
                Log.e(TAG, providerName + " API error " + responseCode + ": " + errorBody);
                throw new LlmApiException(responseCode,
                        providerName + " API error: " + responseCode + " - " + errorBody);
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
            JSONArray choices = response.getJSONArray("choices");
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            return message.getString("content");
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
