package org.coughlin.grocerylist;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates recipes for a single day's menu. Processes one day at a time
 * to avoid timeouts, then enqueues itself for the next day.
 */
public class RecipeGenerationWorker extends Worker {
    private static final String TAG = "RecipeGenWorker";

    static final String KEY_START_DATE = "start_date";
    static final String KEY_END_DATE = "end_date";
    static final String KEY_CURRENT_INDEX = "current_index";
    static final String WORK_NAME = "RecipeGenerationChain";

    public RecipeGenerationWorker(@NonNull Context context,
                                  @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String startDate = getInputData().getString(KEY_START_DATE);
        String endDate = getInputData().getString(KEY_END_DATE);
        int currentIndex = getInputData().getInt(KEY_CURRENT_INDEX, 0);

        if (startDate == null || endDate == null) {
            Log.e(TAG, "Missing start/end date input data");
            return Result.failure();
        }

        try {
            if (isStopped()) return Result.retry();

            GroceryListDatabase db = GroceryListDatabase.getDatabase(
                    getApplicationContext());
            DailyMenuDao menuDao = db.dailyMenuDao();
            RecipeDao recipeDao = db.recipeDao();
            RecipeIngredientDao ingredientDao = db.recipeIngredientDao();
            ProductDao productDao = db.productDao();

            List<String> dates = menuDao.getDistinctDatesInRange(startDate, endDate);

            if (currentIndex >= dates.size()) {
                Log.i(TAG, "All recipe days processed (" + dates.size() + " days)");
                return Result.success();
            }

            String dateToProcess = dates.get(currentIndex);
            Log.i(TAG, "Processing recipes for day " + (currentIndex + 1) +
                    "/" + dates.size() + ": " + dateToProcess);

            List<DailyMenu> meals = menuDao.getMealsForDateSync(dateToProcess);
            
            if (meals != null && !meals.isEmpty()) {
                // Check if recipes already exist for ALL meals of this day.
                // If some are missing, we should probably try to generate.
                boolean allRecipesExist = true;
                for (DailyMenu meal : meals) {
                    if (recipeDao.getRecipeForMenuSync(meal.getId()) == null) {
                        allRecipesExist = false;
                        break;
                    }
                }

                if (!allRecipesExist) {
                    String recipePrompt = buildRecipePrompt(dateToProcess, meals);
                    Log.d(TAG, "Recipe prompt for " + dateToProcess + ": " + recipePrompt);

                    if (isStopped()) return Result.retry();

                    String recipeResponse = LlmClient.callLlm(getApplicationContext(), recipePrompt);
                    Log.d(TAG, "Recipe response for " + dateToProcess + ": " + recipeResponse);

                    if (isStopped()) return Result.retry();

                    insertRecipesAndAddToGroceryList(recipeResponse, meals, recipeDao, ingredientDao, productDao);
                    Log.i(TAG, "Successfully created recipes for " + dateToProcess);
                } else {
                    Log.i(TAG, "Recipes already exist for all meals on " + dateToProcess + ", skipping.");
                }
            } else {
                Log.w(TAG, "No meals found for date: " + dateToProcess);
            }

            // Move to next day
            enqueueNextDay(startDate, endDate, currentIndex + 1, dates.size());

            return Result.success();

        } catch (LlmApiException e) {
            Log.e(TAG, "Failed to generate recipes for index " + currentIndex, e);
            if (e.isClientError()) {
                Log.e(TAG, "Client error (HTTP " + e.getStatusCode() + "), not retrying");
                return Result.failure();
            }
            return Result.retry();
        } catch (Exception e) {
            Log.e(TAG, "Failed to generate recipes for index " + currentIndex, e);
            return Result.retry();
        }
    }

    private void enqueueNextDay(String startDate, String endDate, int nextIndex, int totalDays) {
        if (nextIndex < totalDays) {
            Data nextData = new Data.Builder()
                    .putString(KEY_START_DATE, startDate)
                    .putString(KEY_END_DATE, endDate)
                    .putInt(KEY_CURRENT_INDEX, nextIndex)
                    .build();

            OneTimeWorkRequest nextRequest = new OneTimeWorkRequest.Builder(
                    RecipeGenerationWorker.class)
                    .setInputData(nextData)
                    .addTag(WORK_NAME)
                    .build();

            // Use plain enqueue (not enqueueUniqueWork) for chained follow-ups.
            // The unique work constraint on the initial trigger from MenuGenerationWorker
            // already prevents duplicate chains. Using enqueueUniqueWork here with
            // APPEND_OR_REPLACE causes the previous completed work to be replaced,
            // which cancels any in-flight workers under the same unique name.
            WorkManager.getInstance(getApplicationContext()).enqueue(nextRequest);
            Log.i(TAG, "Enqueued recipe generation for day " + (nextIndex + 1));
        } else {
            Log.i(TAG, "Recipe generation chain complete.");
        }
    }

    private String buildRecipePrompt(String date, List<DailyMenu> meals) {
        StringBuilder sb = new StringBuilder();
        sb.append("Generate recipes for the following meals on ").append(date).append(":\n");

        for (DailyMenu meal : meals) {
            sb.append("- ").append(meal.getMealType()).append(": ")
              .append(meal.getMealDescription()).append("\n");
        }

        sb.append("\nReturn ONLY valid JSON in this exact format, no other text:\n");
        sb.append("{\"recipes\":[\n");
        sb.append("  {\"mealType\":\"breakfast\",\"ingredients\":[{\"name\":\"ingredient name\",\"quantity\":\"amount and unit\"}],\"procedure\":\"Step 1: ... Step 2: ...\"},\n");
        sb.append("  {\"mealType\":\"lunch\",\"ingredients\":[...],\"procedure\":\"...\"},\n");
        sb.append("  {\"mealType\":\"dinner\",\"ingredients\":[...],\"procedure\":\"...\"}\n");
        sb.append("]}\n");
        sb.append(AppSettings.getRecipePrompt(getApplicationContext()));

        return sb.toString();
    }

    private void insertRecipesAndAddToGroceryList(String response, List<DailyMenu> meals,
                                                  RecipeDao recipeDao,
                                                  RecipeIngredientDao ingredientDao,
                                                  ProductDao productDao) throws Exception {
        String trimmed = response.trim();
        
        // Handle potential markdown wrapping from LLM
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        trimmed = trimmed.trim();

        JSONArray recipes;
        if (trimmed.startsWith("[")) {
            recipes = new JSONArray(trimmed);
        } else if (trimmed.startsWith("{")) {
            JSONObject wrapper = new JSONObject(trimmed);
            if (wrapper.has("recipes")) {
                recipes = wrapper.getJSONArray("recipes");
            } else {
                throw new Exception("Unexpected recipe JSON format (missing 'recipes' key)");
            }
        } else {
            throw new Exception("Response is not valid JSON");
        }

        // Use case-insensitive lookup for meal types
        Map<String, DailyMenu> mealLookup = new HashMap<>();
        for (DailyMenu meal : meals) {
            mealLookup.put(meal.getMealType().toLowerCase(), meal);
        }

        List<Recipe> recipesToInsert = new ArrayList<>();
        List<JSONArray> ingredientsPerRecipe = new ArrayList<>();

        for (int i = 0; i < recipes.length(); i++) {
            JSONObject recipeObj = recipes.getJSONObject(i);
            String mealType = recipeObj.optString("mealType", "").toLowerCase();
            DailyMenu meal = mealLookup.get(mealType);

            if (meal != null) {
                // Check if recipe already exists for this specific meal
                if (recipeDao.getRecipeForMenuSync(meal.getId()) != null) {
                    continue; 
                }

                String procedure = recipeObj.optString("procedure", "");
                JSONArray ingredients = recipeObj.optJSONArray("ingredients");
                if (ingredients == null) {
                    ingredients = new JSONArray();
                }

                recipesToInsert.add(new Recipe(meal.getId(), "", procedure));
                ingredientsPerRecipe.add(ingredients);
            }
        }

        if (!recipesToInsert.isEmpty()) {
            List<Long> recipeIds = recipeDao.insertAll(recipesToInsert);

            List<RecipeIngredient> allIngredients = new ArrayList<>();
            for (int i = 0; i < recipeIds.size(); i++) {
                int recipeId = recipeIds.get(i).intValue();
                JSONArray ingredientsArr = ingredientsPerRecipe.get(i);
                for (int j = 0; j < ingredientsArr.length(); j++) {
                    JSONObject ingObj = ingredientsArr.getJSONObject(j);
                    String ingredientName = ingObj.optString("name", "");
                    String quantity = ingObj.optString("quantity", "");

                    if (!ingredientName.isEmpty()) {
                        allIngredients.add(new RecipeIngredient(
                                recipeId,
                                ingredientName,
                                quantity));
                        updateGroceryList(ingredientName, productDao);
                    }
                }
            }
            if (!allIngredients.isEmpty()) {
                ingredientDao.insertAll(allIngredients);
            }
        }
    }

    private void updateGroceryList(String ingredientName, ProductDao productDao) {
        String normalized = ingredientName.trim();
        Product existingProduct = productDao.getProductByNameIgnoreCaseSync(normalized);

        if (existingProduct == null && normalized.toLowerCase().endsWith("s") && normalized.length() > 3) {
            String singular = normalized.substring(0, normalized.length() - 1);
            existingProduct = productDao.getProductByNameIgnoreCaseSync(singular);
        }

        if (existingProduct != null) {
            productDao.updateSelectProduct(existingProduct.getId());
            productDao.uncheckProduct(existingProduct.getId());
        } else {
            if (!normalized.isEmpty()) {
                String formattedName = normalized.substring(0, 1).toUpperCase() + normalized.substring(1);
                Product newProduct = new Product(formattedName, true, false);
                productDao.insert(newProduct);
            }
        }
    }
}
