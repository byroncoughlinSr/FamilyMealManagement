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
    public static final String KEY_ERROR_MESSAGE = "error_message";
    static final String KEY_START_DATE = "start_date";
    static final String KEY_END_DATE = "end_date";
    private static final String TAG = "MenuGenerationWorker";
    static final DateTimeFormatter DB_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** Monday of the week containing today. */
    static LocalDate currentWeekStart() {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /** Monday of next week — the range the weekly scheduler plans ahead. */
    static LocalDate nextWeekStart() {
        return LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }

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

            String requestedStart = getInputData().getString(KEY_START_DATE);
            String requestedEnd = getInputData().getString(KEY_END_DATE);

            if (requestedStart != null && requestedEnd != null) {
                // Caller picked the range (e.g. regenerating the current week).
                startDate = LocalDate.parse(requestedStart, DB_FORMATTER);
                endDate = LocalDate.parse(requestedEnd, DB_FORMATTER);
            } else {
                // No range supplied — the weekly scheduler runs this way. Plan ahead.
                // --- Toggle between WEEKLY and MONTHLY meal plans ---
                boolean isMonthly = false; // Set to true for monthly, false for weekly

                if (isMonthly) {
                    LocalDate today = LocalDate.now();
                    startDate = today.getDayOfMonth() > 1 ? today : today.withDayOfMonth(1);
                    endDate = today.with(TemporalAdjusters.lastDayOfMonth());
                } else {
                    startDate = nextWeekStart();
                    endDate = startDate.plusDays(6);
                }
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
                MealCalendarHelper.autoSyncRange(getApplicationContext(), startDate, endDate);
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

            String preferences = AppSettings.getMenuPrompt(getApplicationContext());

            prompt = "Generate a meal plan for the following dates:\n" + dateList +
                    "For each date, provide the meals that are NOT already listed above. " +
                    "Return ONLY valid JSON in this exact format, no other text: " +
                    "{\"menu\":[{\"date\":\"yyyy-MM-dd\",\"breakfast\":\"meal\"," +
                    "\"lunch\":\"meal\",\"dinner\":\"meal\"}]}" +
                    preferences;

            Log.d(TAG, "Prompt: " + prompt);
            String response = LlmClient.callLlm(getApplicationContext(), prompt);
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

            List<Long> insertedIds = dao.insertAllReturnIds(mealsToInsert);

            // Fetch and store a photo for each dinner item. Best-effort: image
            // failures must not abort menu generation. insertAllReturnIds returns
            // IDs in list order, so index i maps meal -> id.
            for (int i = 0; i < mealsToInsert.size(); i++) {
                if (isStopped()) return Result.retry();
                DailyMenu meal = mealsToInsert.get(i);
                if ("dinner".equals(meal.getMealType())) {
                    int menuId = insertedIds.get(i).intValue();
                    String imagePath = ImageSearchClient.fetchAndStoreDinnerImage(
                            getApplicationContext(), meal.getMealDescription(), menuId);
                    if (!imagePath.isEmpty()) {
                        dao.updateImageUrl(menuId, imagePath);
                    }
                }
            }

            Log.i(TAG, "Successfully generated menu from " +
                    startDateStr + " to " + endDateStr);

            // Put the new menu on the family's Google Calendar without any user action.
            int synced = MealCalendarHelper.autoSyncRange(
                    getApplicationContext(), startDate, endDate);
            if (synced >= 0) {
                Log.i(TAG, "Added " + synced + " meals to Google Calendar");
            }

            // Automatically trigger recipe generation for this new menu
            triggerRecipeGeneration(startDateStr, endDateStr);

            return Result.success();

        } catch (LlmApiException e) {
            Log.e(TAG, "Failed to generate menu", e);
            return Result.failure(errorData("HTTP " + e.getStatusCode() + ": " + e.getMessage()));
        } catch (Exception e) {
            Log.e(TAG, "Failed to generate menu", e);
            return Result.failure(errorData(e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    private static Data errorData(String message) {
        return new Data.Builder().putString(KEY_ERROR_MESSAGE, message).build();
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

}
