package org.coughlin.grocerylist;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
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
    private EditText promptEditText;
    private EditText recipePromptEditText;
    private Spinner daySpinner;
    private TimePicker timePicker;
    private Button testButton;
    private Spinner llmProviderSpinner;
    private EditText apiKeyEditText;
    private EditText claudeApiKeyEditText;
    private EditText groqApiKeyEditText;
    private EditText openAiApiKeyEditText;
    private EditText grokApiKeyEditText;
    private ModelPicker ollamaModelPicker;
    private ModelPicker geminiModelPicker;
    private ModelPicker claudeModelPicker;
    private ModelPicker groqModelPicker;
    private ModelPicker openAiModelPicker;
    private ModelPicker grokModelPicker;
    private ModelPicker[] modelPickers;
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

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService modelListExecutor = Executors.newSingleThreadExecutor();

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
        promptEditText = findViewById(R.id.editMenuPrompt);
        recipePromptEditText = findViewById(R.id.editRecipePrompt);
        daySpinner = findViewById(R.id.spinnerScheduleDay);
        timePicker = findViewById(R.id.timePickerSchedule);
        Button saveButton = findViewById(R.id.btnSaveSettings);
        testButton = findViewById(R.id.btnTestConnection);
        llmProviderSpinner = findViewById(R.id.spinnerLlmProvider);
        apiKeyEditText = findViewById(R.id.editTextApiKey);
        claudeApiKeyEditText = findViewById(R.id.editTextClaudeApiKey);
        groqApiKeyEditText = findViewById(R.id.editTextGroqApiKey);
        openAiApiKeyEditText = findViewById(R.id.editTextOpenAiApiKey);
        grokApiKeyEditText = findViewById(R.id.editTextGrokApiKey);
        pexelsApiKeyEditText = findViewById(R.id.editTextPexelsApiKey);

        ollamaModelPicker = new ModelPicker(AppSettings.PROVIDER_OLLAMA, ollamaUrlEditText,
                R.id.spinnerOllamaModel, R.id.spinnerOllamaModelStatus,
                R.string.settings_models_need_url,
                AppSettings.getOllamaModel(this), AppSettings.DEFAULT_OLLAMA_MODEL);
        geminiModelPicker = new ModelPicker(AppSettings.PROVIDER_GEMINI, apiKeyEditText,
                R.id.spinnerGeminiModel, R.id.spinnerGeminiModelStatus,
                R.string.settings_models_need_key,
                AppSettings.getGeminiModel(this), AppSettings.DEFAULT_GEMINI_MODEL);
        claudeModelPicker = new ModelPicker(AppSettings.PROVIDER_CLAUDE, claudeApiKeyEditText,
                R.id.spinnerClaudeModel, R.id.spinnerClaudeModelStatus,
                R.string.settings_models_need_key,
                AppSettings.getClaudeModel(this), AppSettings.DEFAULT_CLAUDE_MODEL);
        groqModelPicker = new ModelPicker(AppSettings.PROVIDER_GROQ, groqApiKeyEditText,
                R.id.spinnerGroqModel, R.id.spinnerGroqModelStatus,
                R.string.settings_models_need_key,
                AppSettings.getGroqModel(this), AppSettings.DEFAULT_GROQ_MODEL);
        openAiModelPicker = new ModelPicker(AppSettings.PROVIDER_OPENAI, openAiApiKeyEditText,
                R.id.spinnerOpenAiModel, R.id.spinnerOpenAiModelStatus,
                R.string.settings_models_need_key,
                AppSettings.getOpenAiModel(this), AppSettings.DEFAULT_OPENAI_MODEL);
        grokModelPicker = new ModelPicker(AppSettings.PROVIDER_GROK, grokApiKeyEditText,
                R.id.spinnerGrokModel, R.id.spinnerGrokModelStatus,
                R.string.settings_models_need_key,
                AppSettings.getGrokModel(this), AppSettings.DEFAULT_GROK_MODEL);
        modelPickers = new ModelPicker[] {
                ollamaModelPicker, geminiModelPicker, claudeModelPicker,
                groqModelPicker, openAiModelPicker, grokModelPicker
        };

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
                for (ModelPicker picker : modelPickers) {
                    if (picker.provider.equals(provider)) picker.load();
                }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler.removeCallbacksAndMessages(null);
        modelListExecutor.shutdownNow();
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
        claudeApiKeyEditText.setText(AppSettings.getClaudeApiKey(this));
        groqApiKeyEditText.setText(AppSettings.getGroqApiKey(this));
        openAiApiKeyEditText.setText(AppSettings.getOpenAiApiKey(this));
        grokApiKeyEditText.setText(AppSettings.getGrokApiKey(this));
        pexelsApiKeyEditText.setText(AppSettings.getPexelsApiKey(this));

        ollamaUrlEditText.setText(AppSettings.getOllamaUrl(this));
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

        String ollamaModel = ollamaModelPicker.getSelectedModel();
        String apiKey = apiKeyEditText.getText().toString().trim();
        String geminiModel = geminiModelPicker.getSelectedModel();
        String claudeApiKey = claudeApiKeyEditText.getText().toString().trim();
        String claudeModel = claudeModelPicker.getSelectedModel();
        String groqApiKey = groqApiKeyEditText.getText().toString().trim();
        String groqModel = groqModelPicker.getSelectedModel();
        String openAiApiKey = openAiApiKeyEditText.getText().toString().trim();
        String openAiModel = openAiModelPicker.getSelectedModel();
        String grokApiKey = grokApiKeyEditText.getText().toString().trim();
        String grokModel = grokModelPicker.getSelectedModel();
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

    private int dayIndexFromCalendarDay(int calendarDay) {
        for (int i = 0; i < DAY_VALUES.length; i++) {
            if (DAY_VALUES[i] == calendarDay) return i;
        }
        return 6; // default Saturday
    }

    /**
     * A provider's model dropdown. Its choices are fetched from the provider using the key
     * (or Ollama server URL) typed in {@code credentialField}, and refetched when that changes.
     * Until a fetch succeeds it offers only the saved model, so saving never loses it.
     * Each fetched model is shown with its price per million tokens when one is known.
     */
    private class ModelPicker {
        final String provider;
        private final EditText credentialField;
        private final Spinner spinner;
        private final TextView status;
        private final int missingCredentialMessage;
        private final String defaultModel;
        private final List<String> models = new ArrayList<>();
        // What the dropdown shows for each entry of models: the id plus its cost.
        private final List<String> labels = new ArrayList<>();
        private final ArrayAdapter<String> adapter;
        private final Runnable loadTask = this::load;
        // Credential the current list was fetched with, so revisiting a provider doesn't refetch.
        private String loadedFor;
        // Bumped per fetch; a response from an older fetch (stale key) is dropped.
        private int requestId;

        ModelPicker(String provider, EditText credentialField, int spinnerId, int statusId,
                    int missingCredentialMessage, String savedModel, String defaultModel) {
            this.provider = provider;
            this.credentialField = credentialField;
            this.spinner = findViewById(spinnerId);
            this.status = findViewById(statusId);
            this.missingCredentialMessage = missingCredentialMessage;
            this.defaultModel = defaultModel;

            models.add(savedModel.isEmpty() ? defaultModel : savedModel);
            labels.add(models.get(0));
            adapter = new ArrayAdapter<>(SettingsActivity.this, R.layout.spinner_item, labels);
            adapter.setDropDownViewResource(R.layout.spinner_item);
            spinner.setAdapter(adapter);

            credentialField.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    // Wait for typing/pasting to settle before hitting the network.
                    mainHandler.removeCallbacks(loadTask);
                    mainHandler.postDelayed(loadTask, 800);
                }
            });
        }

        String getSelectedModel() {
            int position = spinner.getSelectedItemPosition();
            return position >= 0 && position < models.size() ? models.get(position) : defaultModel;
        }

        /** Fetches the model list if this provider is the selected one and its key changed. */
        void load() {
            mainHandler.removeCallbacks(loadTask);
            int providerPos = llmProviderSpinner.getSelectedItemPosition();
            if (providerPos < 0 || providerPos >= PROVIDER_ORDER.length
                    || !PROVIDER_ORDER[providerPos].equals(provider)) {
                return;
            }

            String credential = credentialField.getText().toString().trim();
            if (credential.isEmpty()) {
                requestId++;
                loadedFor = null;
                showStatus(getString(missingCredentialMessage));
                return;
            }
            if (credential.equals(loadedFor)) return;

            final int request = ++requestId;
            showStatus(getString(R.string.settings_models_loading));
            modelListExecutor.execute(() -> {
                List<String> fetched = null;
                Map<String, String> fetchedCosts = null;
                String error = null;
                try {
                    fetched = ModelListClient.listModels(provider, credential);
                    fetchedCosts = ModelPricing.describeCosts(
                            getApplicationContext(), provider, fetched);
                } catch (Exception e) {
                    error = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                }
                final List<String> result = fetched;
                final Map<String, String> costs = fetchedCosts;
                final String failure = error;
                mainHandler.post(() -> {
                    if (request != requestId || isFinishing()) return;
                    if (result == null) {
                        // Keep the current choices so the saved model is still selectable.
                        showStatus(getString(R.string.settings_models_failed, failure));
                        return;
                    }
                    loadedFor = credential;
                    if (result.isEmpty()) {
                        showStatus(getString(R.string.settings_models_empty));
                        return;
                    }
                    status.setVisibility(View.GONE);
                    setModels(result, costs);
                });
            });
        }

        private void setModels(List<String> newModels, Map<String, String> costs) {
            String current = getSelectedModel();
            models.clear();
            models.addAll(newModels);
            labels.clear();
            String unknownCost = getString(R.string.settings_model_cost_unknown);
            for (String id : newModels) {
                String cost = costs.get(id);
                labels.add(id + "\n" + (cost != null ? cost : unknownCost));
            }
            adapter.notifyDataSetChanged();
            // Keep the user's model if this key can use it, else fall back to the app default.
            int index = models.indexOf(current);
            if (index < 0) index = models.indexOf(defaultModel);
            spinner.setSelection(Math.max(index, 0));
        }

        private void showStatus(String message) {
            status.setText(message);
            status.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
