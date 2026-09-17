package org.coughlin.grocerylist;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {

    private static final String[] PROVIDER_ORDER = {
            AppSettings.PROVIDER_OLLAMA,
            AppSettings.PROVIDER_GEMINI,
            AppSettings.PROVIDER_CLAUDE,
            AppSettings.PROVIDER_GROQ,
            AppSettings.PROVIDER_OPENAI,
            AppSettings.PROVIDER_GROK,
            AppSettings.PROVIDER_ON_DEVICE
    };

    private EditText ollamaUrlEditText;
    private EditText ollamaModelEditText;
    private EditText promptEditText;
    private EditText recipePromptEditText;
    private Spinner daySpinner;
    private TimePicker timePicker;
    private Button testButton;
    private Spinner llmProviderSpinner;
    private EditText apiKeyEditText;
    private EditText geminiModelEditText;
    private EditText claudeApiKeyEditText;
    private EditText claudeModelEditText;
    private EditText groqApiKeyEditText;
    private EditText groqModelEditText;
    private EditText openAiApiKeyEditText;
    private EditText openAiModelEditText;
    private EditText grokApiKeyEditText;
    private Spinner grokModelSpinner;
    private EditText pexelsApiKeyEditText;
    private LinearLayout ollamaFields;
    private LinearLayout geminiFields;
    private LinearLayout claudeFields;
    private LinearLayout groqFields;
    private LinearLayout openaiFields;
    private LinearLayout grokFields;
    private LinearLayout onDeviceFields;

    // Prompt field that receives the next speech-to-text result.
    private EditText speechTarget;
    private ActivityResultLauncher<Intent> speechLauncher;

    // Must match the order of R.array.grok_models.
    private static final String[] GROK_MODELS = {
            "grok-4.6", "grok-4.5", "grok-4.3"
    };

    private static final int[] DAY_VALUES = {
            Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY,
            Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.action_settings);
        }

        ollamaUrlEditText = findViewById(R.id.editOllamaUrl);
        ollamaModelEditText = findViewById(R.id.editOllamaModel);
        promptEditText = findViewById(R.id.editMenuPrompt);
        recipePromptEditText = findViewById(R.id.editRecipePrompt);
        daySpinner = findViewById(R.id.spinnerScheduleDay);
        timePicker = findViewById(R.id.timePickerSchedule);
        Button saveButton = findViewById(R.id.btnSaveSettings);
        testButton = findViewById(R.id.btnTestConnection);
        llmProviderSpinner = findViewById(R.id.spinnerLlmProvider);
        apiKeyEditText = findViewById(R.id.editTextApiKey);
        geminiModelEditText = findViewById(R.id.editTextGeminiModel);
        claudeApiKeyEditText = findViewById(R.id.editTextClaudeApiKey);
        claudeModelEditText = findViewById(R.id.editTextClaudeModel);
        groqApiKeyEditText = findViewById(R.id.editTextGroqApiKey);
        groqModelEditText = findViewById(R.id.editTextGroqModel);
        openAiApiKeyEditText = findViewById(R.id.editTextOpenAiApiKey);
        openAiModelEditText = findViewById(R.id.editTextOpenAiModel);
        grokApiKeyEditText = findViewById(R.id.editTextGrokApiKey);
        grokModelSpinner = findViewById(R.id.spinnerGrokModel);
        pexelsApiKeyEditText = findViewById(R.id.editTextPexelsApiKey);

        ArrayAdapter<CharSequence> grokModelAdapter = ArrayAdapter.createFromResource(
                this, R.array.grok_models, R.layout.spinner_item);
        grokModelAdapter.setDropDownViewResource(R.layout.spinner_item);
        grokModelSpinner.setAdapter(grokModelAdapter);
        ollamaFields = findViewById(R.id.ollamaFields);
        geminiFields = findViewById(R.id.geminiFields);
        claudeFields = findViewById(R.id.claudeFields);
        groqFields = findViewById(R.id.groqFields);
        openaiFields = findViewById(R.id.openaiFields);
        grokFields = findViewById(R.id.grokFields);
        onDeviceFields = findViewById(R.id.onDeviceFields);

        ArrayAdapter<CharSequence> providerAdapter = ArrayAdapter.createFromResource(
                this, R.array.llm_providers, R.layout.spinner_item);
        providerAdapter.setDropDownViewResource(R.layout.spinner_item);
        llmProviderSpinner.setAdapter(providerAdapter);

        llmProviderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String provider = position < PROVIDER_ORDER.length
                        ? PROVIDER_ORDER[position] : AppSettings.PROVIDER_OLLAMA;
                ollamaFields.setVisibility(AppSettings.PROVIDER_OLLAMA.equals(provider) ? View.VISIBLE : View.GONE);
                geminiFields.setVisibility(AppSettings.PROVIDER_GEMINI.equals(provider) ? View.VISIBLE : View.GONE);
                claudeFields.setVisibility(AppSettings.PROVIDER_CLAUDE.equals(provider) ? View.VISIBLE : View.GONE);
                groqFields.setVisibility(AppSettings.PROVIDER_GROQ.equals(provider) ? View.VISIBLE : View.GONE);
                openaiFields.setVisibility(AppSettings.PROVIDER_OPENAI.equals(provider) ? View.VISIBLE : View.GONE);
                grokFields.setVisibility(AppSettings.PROVIDER_GROK.equals(provider) ? View.VISIBLE : View.GONE);
                onDeviceFields.setVisibility(AppSettings.PROVIDER_ON_DEVICE.equals(provider) ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.days_of_week, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        daySpinner.setAdapter(adapter);

        loadSettings();

        saveButton.setOnClickListener(v -> saveSettings());
        testButton.setOnClickListener(v -> testConnection());

        enableInnerScrolling(promptEditText);
        enableInnerScrolling(recipePromptEditText);

        speechLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> onSpeechResult(result.getResultCode(), result.getData()));

        ImageButton menuPromptMic = findViewById(R.id.btnMenuPromptMic);
        ImageButton recipePromptMic = findViewById(R.id.btnRecipePromptMic);
        menuPromptMic.setOnClickListener(v -> startSpeechToText(promptEditText));
        recipePromptMic.setOnClickListener(v -> startSpeechToText(recipePromptEditText));
    }

    /**
     * Lets a multi-line EditText scroll its own content when it is nested inside the
     * page ScrollView. Without this the outer ScrollView steals the drag gesture.
     */
    private void enableInnerScrolling(EditText editText) {
        editText.setOnTouchListener((v, event) -> {
            if (v.canScrollVertically(1) || v.canScrollVertically(-1)) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                if (event.getActionMasked() == MotionEvent.ACTION_UP
                        || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                }
            }
            return false;
        });
    }

    private void startSpeechToText(EditText target) {
        speechTarget = target;
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.settings_speech_prompt));
        try {
            speechLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            speechTarget = null;
            Toast.makeText(this, R.string.settings_speech_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    private void onSpeechResult(int resultCode, Intent data) {
        EditText target = speechTarget;
        speechTarget = null;
        if (resultCode != Activity.RESULT_OK || data == null || target == null) return;

        ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        if (matches == null || matches.isEmpty()) return;
        String spoken = matches.get(0).trim();
        if (spoken.isEmpty()) return;

        // Insert at the cursor (or replace the selection); fall back to appending.
        int start = Math.max(0, target.getSelectionStart());
        int end = Math.max(0, target.getSelectionEnd());
        if (start > end) { int t = start; start = end; end = t; }
        CharSequence existing = target.getText();
        boolean needsSpace = start > 0 && !Character.isWhitespace(existing.charAt(start - 1));
        String insert = (needsSpace ? " " : "") + spoken;
        target.getText().replace(start, end, insert);
        target.setSelection(start + insert.length());
        target.requestFocus();
    }

    private void loadSettings() {
        String provider = AppSettings.getLlmProvider(this);
        llmProviderSpinner.setSelection(providerIndex(provider));
        apiKeyEditText.setText(AppSettings.getApiKey(this));
        geminiModelEditText.setText(AppSettings.getGeminiModel(this));
        claudeApiKeyEditText.setText(AppSettings.getClaudeApiKey(this));
        claudeModelEditText.setText(AppSettings.getClaudeModel(this));
        groqApiKeyEditText.setText(AppSettings.getGroqApiKey(this));
        groqModelEditText.setText(AppSettings.getGroqModel(this));
        openAiApiKeyEditText.setText(AppSettings.getOpenAiApiKey(this));
        openAiModelEditText.setText(AppSettings.getOpenAiModel(this));
        grokApiKeyEditText.setText(AppSettings.getGrokApiKey(this));
        grokModelSpinner.setSelection(grokModelIndex(AppSettings.getGrokModel(this)));
        pexelsApiKeyEditText.setText(AppSettings.getPexelsApiKey(this));

        ollamaUrlEditText.setText(AppSettings.getOllamaUrl(this));
        ollamaModelEditText.setText(AppSettings.getOllamaModel(this));
        promptEditText.setText(AppSettings.getMenuPrompt(this));
        recipePromptEditText.setText(AppSettings.getRecipePrompt(this));

        int savedDay = AppSettings.getScheduleDay(this);
        daySpinner.setSelection(dayIndexFromCalendarDay(savedDay));

        timePicker.setIs24HourView(false);
        timePicker.setHour(AppSettings.getScheduleHour(this));
        timePicker.setMinute(AppSettings.getScheduleMinute(this));
    }

    private void saveSettings() {
        int providerPos = llmProviderSpinner.getSelectedItemPosition();
        String selectedProvider = providerPos < PROVIDER_ORDER.length
                ? PROVIDER_ORDER[providerPos] : AppSettings.PROVIDER_OLLAMA;

        String ollamaUrl = ollamaUrlEditText.getText().toString().trim();
        if (AppSettings.PROVIDER_OLLAMA.equals(selectedProvider) && ollamaUrl.isEmpty()) {
            ollamaUrlEditText.setError(getString(R.string.settings_prompt_required));
            return;
        }

        String ollamaModel = ollamaModelEditText.getText().toString().trim();
        if (AppSettings.PROVIDER_OLLAMA.equals(selectedProvider) && ollamaModel.isEmpty()) {
            ollamaModelEditText.setError(getString(R.string.settings_prompt_required));
            return;
        }

        String apiKey = apiKeyEditText.getText().toString().trim();
        String geminiModel = geminiModelEditText.getText().toString().trim();
        String claudeApiKey = claudeApiKeyEditText.getText().toString().trim();
        String claudeModel = claudeModelEditText.getText().toString().trim();
        String groqApiKey = groqApiKeyEditText.getText().toString().trim();
        String groqModel = groqModelEditText.getText().toString().trim();
        String openAiApiKey = openAiApiKeyEditText.getText().toString().trim();
        String openAiModel = openAiModelEditText.getText().toString().trim();
        String grokApiKey = grokApiKeyEditText.getText().toString().trim();
        String grokModel = GROK_MODELS[grokModelSpinner.getSelectedItemPosition()];
        String pexelsApiKey = pexelsApiKeyEditText.getText().toString().trim();

        String prompt = promptEditText.getText().toString().trim();
        if (prompt.isEmpty()) {
            promptEditText.setError(getString(R.string.settings_prompt_required));
            return;
        }

        String recipePrompt = recipePromptEditText.getText().toString().trim();
        if (recipePrompt.isEmpty()) {
            recipePromptEditText.setError(getString(R.string.settings_prompt_required));
            return;
        }

        int calendarDay = DAY_VALUES[daySpinner.getSelectedItemPosition()];
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        AppSettings.getPrefs(this).edit()
                .putString(AppSettings.KEY_LLM_PROVIDER, selectedProvider)
                .putString(AppSettings.KEY_API_KEY, apiKey)
                .putString(AppSettings.KEY_GEMINI_MODEL, geminiModel)
                .putString(AppSettings.KEY_CLAUDE_API_KEY, claudeApiKey)
                .putString(AppSettings.KEY_CLAUDE_MODEL, claudeModel)
                .putString(AppSettings.KEY_GROQ_API_KEY, groqApiKey)
                .putString(AppSettings.KEY_GROQ_MODEL, groqModel)
                .putString(AppSettings.KEY_OPENAI_API_KEY, openAiApiKey)
                .putString(AppSettings.KEY_OPENAI_MODEL, openAiModel)
                .putString(AppSettings.KEY_GROK_API_KEY, grokApiKey)
                .putString(AppSettings.KEY_GROK_MODEL, grokModel)
                .putString(AppSettings.KEY_PEXELS_API_KEY, pexelsApiKey)
                .putString(AppSettings.KEY_OLLAMA_URL, ollamaUrl)
                .putString(AppSettings.KEY_OLLAMA_MODEL, ollamaModel)
                .putString(AppSettings.KEY_MENU_PROMPT, prompt)
                .putString(AppSettings.KEY_RECIPE_PROMPT, recipePrompt)
                .putInt(AppSettings.KEY_SCHEDULE_DAY, calendarDay)
                .putInt(AppSettings.KEY_SCHEDULE_HOUR, hour)
                .putInt(AppSettings.KEY_SCHEDULE_MINUTE, minute)
                .apply();

        MenuGenerationScheduler.scheduleWeeklyMenuGeneration(this);

        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
    }

    private void testConnection() {
        String rawUrl = ollamaUrlEditText.getText().toString().trim();
        if (rawUrl.isEmpty()) {
            Toast.makeText(this, "Enter a server URL first", Toast.LENGTH_SHORT).show();
            return;
        }

        testButton.setEnabled(false);
        testButton.setText(R.string.settings_testing);

        String baseUrl;
        try {
            URL parsed = new URL(rawUrl);
            baseUrl = parsed.getProtocol() + "://" + parsed.getHost() + ":" + parsed.getPort();
        } catch (Exception e) {
            baseUrl = rawUrl;
        }

        final String urlToTest = baseUrl;
        Handler mainHandler = new Handler(Looper.getMainLooper());

        Executors.newSingleThreadExecutor().execute(() -> {
            String result;
            try {
                URL url = new URL(urlToTest);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                int code = conn.getResponseCode();
                conn.disconnect();
                result = "Connected! HTTP " + code;
            } catch (Exception e) {
                result = "Failed: " + e.getMessage();
            }

            final String finalResult = result;
            mainHandler.post(() -> {
                testButton.setEnabled(true);
                testButton.setText(R.string.settings_test_connection);
                Toast.makeText(this, finalResult, Toast.LENGTH_LONG).show();
            });
        });
    }

    private int providerIndex(String provider) {
        for (int i = 0; i < PROVIDER_ORDER.length; i++) {
            if (PROVIDER_ORDER[i].equals(provider)) return i;
        }
        return 0;
    }

    private int grokModelIndex(String model) {
        for (int i = 0; i < GROK_MODELS.length; i++) {
            if (GROK_MODELS[i].equals(model)) return i;
        }
        return 0; // default to first valid model (e.g. legacy/retired saved value)
    }

    private int dayIndexFromCalendarDay(int calendarDay) {
        for (int i = 0; i < DAY_VALUES.length; i++) {
            if (DAY_VALUES[i] == calendarDay) return i;
        }
        return 6; // default Saturday
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
