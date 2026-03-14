package org.coughlin.grocerylist;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MenuGenerationWorker extends Worker {
    private static final String TAG = "MenuGenerationWorker";
    static final String OLLAMA_URL = "http://192.168.4.249:11434/api/generate";
    static final String OLLAMA_MODEL = "llama3.1:8b";
    static final DateTimeFormatter DB_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public MenuGenerationWorker(@NonNull Context context,
                                @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            if (isStopped()) return Result.retry();

            GroceryListDatabase db = GroceryListDatabase.getDatabase(
                    getApplicationContext());
            DailyMenuDao dao = db.dailyMenuDao();

            LocalDate startDate;
            LocalDate endDate;
            String prompt;

            // --- Toggle between WEEKLY and MONTHLY meal plans ---
            boolean isMonthly = false; // Set to true for monthly, false for weekly

            if (isMonthly) {
                LocalDate today = LocalDate.now();
                startDate = today.getDayOfMonth() > 1 ? today : today.withDayOfMonth(1);
                endDate = today.with(TemporalAdjusters.lastDayOfMonth());
            } else {
                startDate = LocalDate.now()
                        .with(TemporalAdjusters.next(DayOfWeek.MONDAY));
                endDate = startDate.plusDays(6);
            }

            String startDateStr = startDate.format(DB_FORMATTER);
            String endDateStr = endDate.format(DB_FORMATTER);

            // Check for manually-set meals in the date range
            List<DailyMenu> manualMeals = dao.getManualMealsInRange(startDateStr, endDateStr);
            Map<String, Set<String>> manualDateMeals = new HashMap<>();
            for (DailyMenu m : manualMeals) {
                manualDateMeals.computeIfAbsent(m.getMenuDate(), k -> new HashSet<>())
                        .add(m.getMealType());
            }

            // Build list of dates that need generation
            List<LocalDate> datesToGenerate = new ArrayList<>();
            LocalDate cursor = startDate;
            while (!cursor.isAfter(endDate)) {
                String cursorStr = cursor.format(DB_FORMATTER);
                Set<String> manualTypes = manualDateMeals.getOrDefault(cursorStr, Collections.emptySet());
                if (manualTypes.size() < 3) {
                    datesToGenerate.add(cursor);
                }
                cursor = cursor.plusDays(1);
            }

            if (datesToGenerate.isEmpty()) {
                Log.i(TAG, "All dates have manual meals, nothing to generate");
                return Result.success();
            }

            // Build prompt with date-specific instructions
            StringBuilder dateList = new StringBuilder();
            for (LocalDate d : datesToGenerate) {
                String dStr = d.format(DB_FORMATTER);
                Set<String> manualTypes = manualDateMeals.getOrDefault(dStr, Collections.emptySet());
                dateList.append(dStr);
                if (!manualTypes.isEmpty()) {
                    dateList.append(" (already have: ");
                    dateList.append(String.join(", ", manualTypes));
                    dateList.append(", generate remaining only)");
                }
                dateList.append("\n");
            }

            String preferences;
            if (isMonthly) {
                preferences = "Two days out of the week include chicken for dinner. " +
                        "One day fish and on every Sunday for breakfast is veggie omelettes with ham. " +
                        "Dinners should also include two side dishes. " +
                        "Ensure there are 7 days of meals per week. " +
                        "Should have eggs for  four times during the week. " +
                        "Include pizza for dinner twice a month on fridays. " +
                        "Include pasta twice a month for dinner, one pasta with red sauce " +
                        "and the other with white sauce.";
            } else {
                preferences = "Two days out of the week include chicken for dinner. " +
                        "One day fish and on sunday for breakfast is veggie omelettes with ham. " +
                        "Dinners should also include two side dishes. " +
                        "Ensure there are seven days of meals. Should have eggs four " +
                        "times during the week";
            }

            prompt = "Generate a meal plan for the following dates:\n" + dateList +
                    "For each date, provide the meals that are NOT already listed above. " +
                    "Return ONLY valid JSON in this exact format, no other text: " +
                    "{\"menu\":[{\"date\":\"yyyy-MM-dd\",\"breakfast\":\"meal\"," +
                    "\"lunch\":\"meal\",\"dinner\":\"meal\"}]}" +
                    preferences;

            Log.d(TAG, "Prompt: " + prompt);
            String response = callOllama(prompt);
            if (isStopped()) return Result.retry();
            Log.d(TAG, "Response: " + response);

            // Delete only non-manual meals (FK CASCADE handles recipe+ingredients)
            dao.deleteNonManualMealsForDateRange(startDateStr, endDateStr);

            List<DailyMenu> mealsToInsert = new ArrayList<>();
            String trimmed = response.trim();
            JSONArray menu;

            if (trimmed.startsWith("[")) {
                menu = new JSONArray(trimmed);
            } else if (trimmed.startsWith("{")) {
                JSONObject wrapper = new JSONObject(trimmed);
                if (wrapper.has("menu")) {
                    menu = wrapper.getJSONArray("menu");
                } else {
                    throw new Exception("Unexpected JSON format: " + trimmed);
                }
            } else {
                throw new Exception("Unexpected JSON response: " + trimmed);
            }

            for (int i = 0; i < menu.length(); i++) {
                if (isStopped()) return Result.retry();
                JSONObject dayObj = menu.getJSONObject(i);
                String date = dayObj.getString("date");
                Set<String> manualTypes = manualDateMeals.getOrDefault(date, Collections.emptySet());

                if (!manualTypes.contains("breakfast") && dayObj.has("breakfast")) {
                    mealsToInsert.add(new DailyMenu(date, "breakfast",
                            dayObj.getString("breakfast")));
                }
                if (!manualTypes.contains("lunch") && dayObj.has("lunch")) {
                    mealsToInsert.add(new DailyMenu(date, "lunch",
                            dayObj.getString("lunch")));
                }
                if (!manualTypes.contains("dinner") && dayObj.has("dinner")) {
                    mealsToInsert.add(new DailyMenu(date, "dinner",
                            dayObj.getString("dinner")));
                }
            }

            dao.insertAllReturnIds(mealsToInsert);

            Log.i(TAG, "Successfully generated menu from " +
                    startDateStr + " to " + endDateStr);

            // Automatically trigger recipe generation for this new menu
            triggerRecipeGeneration(startDateStr, endDateStr);

            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Failed to generate menu", e);
            return Result.retry();
        }
    }

    private void triggerRecipeGeneration(String startDate, String endDate) {
        Data inputData = new Data.Builder()
                .putString(RecipeGenerationWorker.KEY_START_DATE, startDate)
                .putString(RecipeGenerationWorker.KEY_END_DATE, endDate)
                .putInt(RecipeGenerationWorker.KEY_CURRENT_INDEX, 0)
                .build();

        OneTimeWorkRequest recipeRequest = new OneTimeWorkRequest.Builder(
                RecipeGenerationWorker.class)
                .setInputData(inputData)
                .addTag("RecipeGenerationChain")
                .build();

        // Use REPLACE to ensure any existing/stale recipe chains are stopped
        WorkManager.getInstance(getApplicationContext()).enqueueUniqueWork(
                "RecipeGenerationChain",
                ExistingWorkPolicy.REPLACE,
                recipeRequest
        );
        Log.i(TAG, "Triggered unique recipe generation chain for " + startDate + " to " + endDate);
    }

    /**
     * Shared Ollama API call method, used by both MenuGenerationWorker and RecipeGenerationWorker.
     */
    static String callOllama(String prompt) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(OLLAMA_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Connection", "close");
            conn.setDoOutput(true);
            conn.setConnectTimeout(60000); // 60 seconds connect timeout
            conn.setReadTimeout(600000);   // 10 minutes read timeout (recipes can take a while)

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", OLLAMA_MODEL);
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
                Log.e("MenuGenerationWorker", "Ollama API error " + responseCode + ": " + errorBody);
                throw new Exception("Ollama API error: " + responseCode + " - " + errorBody);
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
}
