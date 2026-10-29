package org.coughlin.grocerylist;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class AppSettings {

    private static final String PREFS_NAME = "FmmSettings";

    public static final String KEY_OLLAMA_URL = "ollama_url";
    public static final String KEY_OLLAMA_MODEL = "ollama_model";
    public static final String KEY_MENU_PROMPT = "menu_generation_prompt";
    public static final String KEY_RECIPE_PROMPT = "recipe_generation_prompt";
    public static final String KEY_SCHEDULE_DAY = "schedule_day_of_week";
    public static final String KEY_SCHEDULE_HOUR = "schedule_hour";
    public static final String KEY_SCHEDULE_MINUTE = "schedule_minute";
    public static final String KEY_LLM_PROVIDER = "llm_provider";
    public static final String KEY_API_KEY = "api_key";
    public static final String KEY_GEMINI_MODEL = "gemini_model";
    public static final String KEY_CLAUDE_API_KEY = "claude_api_key";
    public static final String KEY_CLAUDE_MODEL = "claude_model";
    public static final String KEY_GROQ_API_KEY = "groq_api_key";
    public static final String KEY_GROQ_MODEL = "groq_model";
    public static final String KEY_OPENAI_API_KEY = "openai_api_key";
    public static final String KEY_OPENAI_MODEL = "openai_model";
    public static final String KEY_GROK_API_KEY = "grok_api_key";
    public static final String KEY_GROK_MODEL = "grok_model";
    public static final String KEY_PEXELS_API_KEY = "pexels_api_key";
    public static final String KEY_CALENDAR_AUTO_SYNC = "calendar_auto_sync";
    public static final String KEY_CALENDAR_ID = "calendar_id";
    public static final String KEY_CALENDAR_PERMISSION_ASKED = "calendar_permission_asked";

    public static final String PROVIDER_OLLAMA = "ollama";
    public static final String PROVIDER_GEMINI = "gemini";
    public static final String PROVIDER_CLAUDE = "claude";
    public static final String PROVIDER_GROQ = "groq";
    public static final String PROVIDER_OPENAI = "openai";
    public static final String PROVIDER_GROK = "grok";
    public static final String PROVIDER_ON_DEVICE = "on_device";

    public static final String DEFAULT_OLLAMA_URL = "";
    public static final String DEFAULT_OLLAMA_MODEL = "llama3.1:8b";

    public static final String DEFAULT_MENU_PROMPT =
            "Two days out of the week include chicken for dinner. " +
            "One day fish and on sunday for breakfast is veggie omelettes with ham. " +
            "Dinners should also include two side dishes. " +
            "Ensure there are seven days of meals. Should have eggs four times during the week";

    public static final String DEFAULT_RECIPE_PROMPT =
            "Requirements: practical family recipe, 5-10 ingredients, " +
            "4-6 preparation steps each prefixed with 'Step N:', " +
            "use standardized ingredient names.";

    public static final String DEFAULT_GEMINI_MODEL = "gemini-3.8-flash";
    public static final String DEFAULT_CLAUDE_MODEL = "claude-haiku-4-5";
    public static final String DEFAULT_GROQ_MODEL = "llama-3.3-70b-versatile";
    public static final String DEFAULT_OPENAI_MODEL = "gpt-5.6-luna";
    public static final String DEFAULT_GROK_MODEL = "grok-4.6";

    public static final int DEFAULT_SCHEDULE_DAY = Calendar.SATURDAY;
    public static final int DEFAULT_SCHEDULE_HOUR = 0;
    public static final int DEFAULT_SCHEDULE_MINUTE = 0;

    /** Grok model ids xAI has retired; requests naming them fail with HTTP 404. */
    private static final Set<String> RETIRED_GROK_MODELS = new HashSet<>(Arrays.asList(
            "grok-2", "grok-2-latest", "grok-2-1212",
            "grok-3", "grok-3-latest", "grok-3-mini", "grok-3-mini-latest",
            "grok-4", "grok-4-latest", "grok-4-0709", "grok-beta"));

    /** Claude model ids Anthropic has retired; requests naming them fail with HTTP 404. */
    private static final Set<String> RETIRED_CLAUDE_MODELS = new HashSet<>(Arrays.asList(
            "claude-sonnet-4-20250514", "claude-opus-4-20250514", "claude-opus-4-1-20250805",
            "claude-3-7-sonnet-20250219", "claude-3-5-sonnet-20240620",
            "claude-3-5-sonnet-20241022", "claude-3-sonnet-20240229",
            "claude-3-5-haiku-20241022", "claude-3-haiku-20240307",
            "claude-3-opus-20240229", "claude-2.0", "claude-2.1"));

    /**
     * Reads a saved model id, replacing it with {@code currentDefault} when the provider has
     * retired it. A model saved before its retirement would otherwise fail with a 404 forever,
     * since these ids are only rewritten when the user re-saves Settings.
     */
    private static String currentModel(Context context, String key,
                                       Set<String> retired, String currentDefault) {
        String model = getPrefs(context).getString(key, currentDefault);
        if (model.isEmpty() || retired.contains(model)) {
            getPrefs(context).edit().putString(key, currentDefault).apply();
            return currentDefault;
        }
        return model;
    }

    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static String getOllamaUrl(Context context) {
        return getPrefs(context).getString(KEY_OLLAMA_URL, DEFAULT_OLLAMA_URL);
    }

    public static String getOllamaModel(Context context) {
        return getPrefs(context).getString(KEY_OLLAMA_MODEL, DEFAULT_OLLAMA_MODEL);
    }

    public static String getMenuPrompt(Context context) {
        return getPrefs(context).getString(KEY_MENU_PROMPT, DEFAULT_MENU_PROMPT);
    }

    public static String getRecipePrompt(Context context) {
        return getPrefs(context).getString(KEY_RECIPE_PROMPT, DEFAULT_RECIPE_PROMPT);
    }

    public static int getScheduleDay(Context context) {
        return getPrefs(context).getInt(KEY_SCHEDULE_DAY, DEFAULT_SCHEDULE_DAY);
    }

    public static int getScheduleHour(Context context) {
        return getPrefs(context).getInt(KEY_SCHEDULE_HOUR, DEFAULT_SCHEDULE_HOUR);
    }

    public static int getScheduleMinute(Context context) {
        return getPrefs(context).getInt(KEY_SCHEDULE_MINUTE, DEFAULT_SCHEDULE_MINUTE);
    }

    public static String getLlmProvider(Context context) {
        return getPrefs(context).getString(KEY_LLM_PROVIDER, PROVIDER_OLLAMA);
    }

    public static void setLlmProvider(Context context, String provider) {
        getPrefs(context).edit().putString(KEY_LLM_PROVIDER, provider).apply();
    }

    public static boolean isProviderConfigured(Context context) {
        String p = getLlmProvider(context);
        switch (p) {
            case PROVIDER_ON_DEVICE: return OnDeviceLlmClient.isAvailable(context);
            case PROVIDER_OLLAMA:    return !getOllamaUrl(context).isEmpty();
            case PROVIDER_GEMINI:    return !getApiKey(context).isEmpty();
            case PROVIDER_CLAUDE:    return !getClaudeApiKey(context).isEmpty();
            case PROVIDER_GROQ:      return !getGroqApiKey(context).isEmpty();
            case PROVIDER_OPENAI:    return !getOpenAiApiKey(context).isEmpty();
            case PROVIDER_GROK:      return !getGrokApiKey(context).isEmpty();
            default: return false;
        }
    }

    public static String getApiKey(Context context) {
        return getPrefs(context).getString(KEY_API_KEY, "");
    }

    public static void setApiKey(Context context, String apiKey) {
        getPrefs(context).edit().putString(KEY_API_KEY, apiKey).apply();
    }

    public static String getGeminiModel(Context context) {
        return getPrefs(context).getString(KEY_GEMINI_MODEL, DEFAULT_GEMINI_MODEL);
    }

    public static void setGeminiModel(Context context, String model) {
        getPrefs(context).edit().putString(KEY_GEMINI_MODEL, model).apply();
    }

    public static String getClaudeApiKey(Context context) {
        return getPrefs(context).getString(KEY_CLAUDE_API_KEY, "");
    }

    public static void setClaudeApiKey(Context context, String apiKey) {
        getPrefs(context).edit().putString(KEY_CLAUDE_API_KEY, apiKey).apply();
    }

    public static String getClaudeModel(Context context) {
        return currentModel(context, KEY_CLAUDE_MODEL, RETIRED_CLAUDE_MODELS, DEFAULT_CLAUDE_MODEL);
    }

    public static void setClaudeModel(Context context, String model) {
        getPrefs(context).edit().putString(KEY_CLAUDE_MODEL, model).apply();
    }

    public static String getGroqApiKey(Context context) {
        return getPrefs(context).getString(KEY_GROQ_API_KEY, "");
    }

    public static void setGroqApiKey(Context context, String apiKey) {
        getPrefs(context).edit().putString(KEY_GROQ_API_KEY, apiKey).apply();
    }

    public static String getGroqModel(Context context) {
        return getPrefs(context).getString(KEY_GROQ_MODEL, DEFAULT_GROQ_MODEL);
    }

    public static void setGroqModel(Context context, String model) {
        getPrefs(context).edit().putString(KEY_GROQ_MODEL, model).apply();
    }

    public static String getOpenAiApiKey(Context context) {
        return getPrefs(context).getString(KEY_OPENAI_API_KEY, "");
    }

    public static void setOpenAiApiKey(Context context, String apiKey) {
        getPrefs(context).edit().putString(KEY_OPENAI_API_KEY, apiKey).apply();
    }

    public static String getOpenAiModel(Context context) {
        return getPrefs(context).getString(KEY_OPENAI_MODEL, DEFAULT_OPENAI_MODEL);
    }

    public static void setOpenAiModel(Context context, String model) {
        getPrefs(context).edit().putString(KEY_OPENAI_MODEL, model).apply();
    }

    public static String getGrokApiKey(Context context) {
        return getPrefs(context).getString(KEY_GROK_API_KEY, "");
    }

    public static void setGrokApiKey(Context context, String apiKey) {
        getPrefs(context).edit().putString(KEY_GROK_API_KEY, apiKey).apply();
    }

    public static String getGrokModel(Context context) {
        return currentModel(context, KEY_GROK_MODEL, RETIRED_GROK_MODELS, DEFAULT_GROK_MODEL);
    }

    public static void setGrokModel(Context context, String model) {
        getPrefs(context).edit().putString(KEY_GROK_MODEL, model).apply();
    }

    public static String getPexelsApiKey(Context context) {
        return getPrefs(context).getString(KEY_PEXELS_API_KEY, "");
    }

    public static void setPexelsApiKey(Context context, String apiKey) {
        getPrefs(context).edit().putString(KEY_PEXELS_API_KEY, apiKey).apply();
    }

    /** Whether newly generated menus are added to Google Calendar automatically. */
    public static boolean isCalendarAutoSync(Context context) {
        return getPrefs(context).getBoolean(KEY_CALENDAR_AUTO_SYNC, true);
    }

    public static void setCalendarAutoSync(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_CALENDAR_AUTO_SYNC, enabled).apply();
    }

    /** Calendar meals are synced to; -1 means use the primary Google calendar. */
    public static long getCalendarId(Context context) {
        return getPrefs(context).getLong(KEY_CALENDAR_ID, -1);
    }

    public static void setCalendarId(Context context, long calendarId) {
        getPrefs(context).edit().putLong(KEY_CALENDAR_ID, calendarId).apply();
    }

    public static boolean wasCalendarPermissionAsked(Context context) {
        return getPrefs(context).getBoolean(KEY_CALENDAR_PERMISSION_ASKED, false);
    }

    public static void setCalendarPermissionAsked(Context context) {
        getPrefs(context).edit().putBoolean(KEY_CALENDAR_PERMISSION_ASKED, true).apply();
    }
}
