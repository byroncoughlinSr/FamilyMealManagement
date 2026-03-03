package org.coughlin.grocerylist;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
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
import java.util.List;

public class MenuGenerationWorker extends Worker {
    private static final String TAG = "MenuGenerationWorker";
    private static final String OLLAMA_URL = "http://192.168.7.249:11434/api/generate";
    private static final String OLLAMA_MODEL = "llama3.1:8b";
    private static final DateTimeFormatter DB_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public MenuGenerationWorker(@NonNull Context context,
                                @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            LocalDate nextMonday = LocalDate.now()
                    .with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            LocalDate nextSunday = nextMonday.plusDays(6);

            String prompt = "Generate a weekly meal plan for Monday " +
                    nextMonday.format(DB_FORMATTER) + " through Sunday " +
                    nextSunday.format(DB_FORMATTER) +
                    ". For each day provide breakfast, lunch, and dinner. " +
                    "Return ONLY valid JSON in this exact format, no other text: " +
                    "[{\"date\":\"yyyy-MM-dd\",\"breakfast\":\"meal\"," +
                    "\"lunch\":\"meal\",\"dinner\":\"meal\"}]";

            String response = callOllama(prompt);

            JSONArray weekMenu = new JSONArray(response);

            GroceryListDatabase db = GroceryListDatabase.getDatabase(
                    getApplicationContext());
            DailyMenuDao dao = db.dailyMenuDao();

            dao.deleteMealsForDateRange(
                    nextMonday.format(DB_FORMATTER),
                    nextSunday.format(DB_FORMATTER));

            List<DailyMenu> mealsToInsert = new ArrayList<>();
            for (int i = 0; i < weekMenu.length(); i++) {
                JSONObject dayObj = weekMenu.getJSONObject(i);
                String date = dayObj.getString("date");
                mealsToInsert.add(new DailyMenu(date, "breakfast",
                        dayObj.getString("breakfast")));
                mealsToInsert.add(new DailyMenu(date, "lunch",
                        dayObj.getString("lunch")));
                mealsToInsert.add(new DailyMenu(date, "dinner",
                        dayObj.getString("dinner")));
            }
            dao.insertAll(mealsToInsert);

            Log.i(TAG, "Successfully generated menu for week of " +
                    nextMonday.format(DB_FORMATTER));
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Failed to generate menu", e);
            return Result.retry();
        }
    }

    private String callOllama(String prompt) throws Exception {
        URL url = new URL(OLLAMA_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(120000);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", OLLAMA_MODEL);
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);
        requestBody.put("format", "json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.toString().getBytes("UTF-8"));
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
    }
}
