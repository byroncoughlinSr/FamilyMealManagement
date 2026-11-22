package org.coughlin.grocerylist;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecipeActivity extends AppCompatActivity {

    private static final String TAG = "RecipeActivity";

    public static final String EXTRA_DAILY_MENU_ID = "extra_daily_menu_id";
    public static final String EXTRA_MEAL_DESCRIPTION = "extra_meal_description";

    private int dailyMenuId;
    private String mealDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dailyMenuId = getIntent().getIntExtra(EXTRA_DAILY_MENU_ID, -1);
        mealDescription = getIntent().getStringExtra(EXTRA_MEAL_DESCRIPTION);

        TextView titleView = findViewById(R.id.textViewRecipeTitle);
        TextView ingredientsView = findViewById(R.id.textViewIngredients);
        TextView procedureView = findViewById(R.id.textViewProcedure);

        titleView.setText(mealDescription != null ? mealDescription : "");

        RecipeViewModel viewModel =
                new ViewModelProvider(this).get(RecipeViewModel.class);
        viewModel.setDailyMenuId(dailyMenuId);

        viewModel.getRecipe().observe(this, recipe -> {
            if (recipe == null) {
                ingredientsView.setText(R.string.no_recipe_available);
                procedureView.setText("");
                return;
            }

            procedureView.setText(recipe.getProcedure());

            // Load ingredients once we have the recipe ID
            viewModel.setRecipeId(recipe.getId());
        });

        viewModel.getIngredients().observe(this, ingredients -> {
            if (ingredients == null || ingredients.isEmpty()) {
                ingredientsView.setText(R.string.no_recipe_available);
                return;
            }
            ingredientsView.setText(formatIngredients(ingredients));
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recipe, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_edit_recipe) {
            launchEditRecipe();
            return true;
        } else if (item.getItemId() == R.id.action_regenerate_recipe) {
            confirmRegenerateRecipe();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void launchEditRecipe() {
        GroceryListDatabase.databaseWriteExecutor.execute(() -> {
            DailyMenu menu = GroceryListDatabase.getDatabase(getApplication())
                    .dailyMenuDao().getMenuByIdSync(dailyMenuId);
            if (menu != null) {
                runOnUiThread(() -> {
                    Intent intent = new Intent(this, CreateRecipeActivity.class);
                    intent.putExtra(CreateRecipeActivity.EXTRA_MEAL_TYPE, menu.getMealType());
                    intent.putExtra(CreateRecipeActivity.EXTRA_DATE, menu.getMenuDate());
                    intent.putExtra(CreateRecipeActivity.EXTRA_MEAL_DESCRIPTION,
                            menu.getMealDescription());
                    intent.putExtra(CreateRecipeActivity.EXTRA_DAILY_MENU_ID, dailyMenuId);
                    startActivity(intent);
                    finish();
                });
            }
        });
    }

    private void confirmRegenerateRecipe() {
        if (dailyMenuId < 0) return;
        String title = mealDescription != null ? mealDescription : "";
        new AlertDialog.Builder(this)
                .setTitle(R.string.regenerate_recipe_title)
                .setMessage(getString(R.string.regenerate_recipe_message, title))
                .setPositiveButton(R.string.regenerate_recipe_confirm,
                        (dialog, which) -> regenerateRecipe())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Asks the configured LLM for a fresh recipe based on the meal title, replaces the
     * stored recipe/ingredients, and syncs the grocery list: new ingredients are added,
     * and old ingredients no longer used by this or any other recipe are removed.
     */
    private void regenerateRecipe() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.generating_recipe));
        progressDialog.setCancelable(false);
        progressDialog.show();

        GroceryListDatabase.databaseWriteExecutor.execute(() -> {
            try {
                GroceryListDatabase db = GroceryListDatabase.getDatabase(getApplication());
                DailyMenu menu = db.dailyMenuDao().getMenuByIdSync(dailyMenuId);
                String title = menu != null ? menu.getMealDescription() : mealDescription;
                String mealType = menu != null ? menu.getMealType() : null;
                if (title == null || title.trim().isEmpty()) {
                    throw new IllegalStateException("Meal has no title to base a recipe on");
                }

                String response = LlmClient.callLlm(getApplicationContext(),
                        buildRecipePrompt(title, mealType));
                Log.d(TAG, "Regenerate recipe response: " + response);

                JSONObject json = parseJson(response);
                String procedure = json.optString("procedure", "").trim();
                JSONArray ingredientsArr = json.optJSONArray("ingredients");
                if (ingredientsArr == null || ingredientsArr.length() == 0) {
                    throw new IllegalStateException("No ingredients in generated recipe");
                }

                db.runInTransaction(() -> replaceRecipe(db, procedure, ingredientsArr));

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    progressDialog.dismiss();
                    Toast.makeText(this, R.string.regenerate_recipe_success,
                            Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to regenerate recipe", e);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    progressDialog.dismiss();
                    Toast.makeText(this, R.string.recipe_generation_failed,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void replaceRecipe(GroceryListDatabase db, String procedure, JSONArray ingredientsArr) {
        RecipeDao recipeDao = db.recipeDao();
        RecipeIngredientDao ingredientDao = db.recipeIngredientDao();
        ProductDao productDao = db.productDao();

        List<RecipeIngredient> oldIngredients = new ArrayList<>();
        Recipe recipe = recipeDao.getRecipeForMenuSync(dailyMenuId);
        int recipeId;
        if (recipe != null) {
            oldIngredients = ingredientDao.getIngredientsForRecipeSync(recipe.getId());
            ingredientDao.deleteIngredientsForRecipe(recipe.getId());
            recipe.setProcedure(procedure);
            recipeDao.update(recipe);
            recipeId = recipe.getId();
        } else {
            recipeId = (int) recipeDao.insert(new Recipe(dailyMenuId, "", procedure));
        }

        List<RecipeIngredient> newIngredients = new ArrayList<>();
        Set<Integer> newProductIds = new HashSet<>();
        for (int i = 0; i < ingredientsArr.length(); i++) {
            JSONObject ing = ingredientsArr.optJSONObject(i);
            if (ing == null) continue;
            String name = ing.optString("name", "").trim();
            if (name.isEmpty()) continue;
            newIngredients.add(new RecipeIngredient(recipeId, name, ing.optString("quantity", "")));
            GroceryListHelper.addIngredient(name, productDao);
            Product product = GroceryListHelper.findProductForIngredient(name, productDao);
            if (product != null) newProductIds.add(product.getId());
        }
        if (!newIngredients.isEmpty()) {
            ingredientDao.insertAll(newIngredients);
        }

        // Drop old ingredients from the grocery list only if nothing still needs them.
        for (RecipeIngredient old : oldIngredients) {
            Product product = GroceryListHelper.findProductForIngredient(old.getName(), productDao);
            if (product == null || newProductIds.contains(product.getId())) continue;
            if (ingredientDao.countIngredientsByName(old.getName()) == 0
                    && ingredientDao.countIngredientsByName(product.getName()) == 0) {
                productDao.unSelectProduct(product.getId());
            }
        }
    }

    private String buildRecipePrompt(String title, String mealType) {
        StringBuilder sb = new StringBuilder();
        sb.append("Generate a new recipe for this ");
        if (mealType != null && !mealType.isEmpty()) {
            sb.append(mealType).append(" ");
        }
        sb.append("meal: ").append(title.trim()).append(".\n");
        sb.append("Return ONLY valid JSON (no markdown, no extra text) in this exact format: ");
        sb.append("{\"ingredients\":[{\"name\":\"ingredient name\",\"quantity\":\"amount and unit\"}],");
        sb.append("\"procedure\":\"Step 1: ...\\nStep 2: ...\"}\n");
        sb.append(AppSettings.getRecipePrompt(getApplicationContext()));
        return sb.toString();
    }

    private static JSONObject parseJson(String response) throws Exception {
        String trimmed = response == null ? "" : response.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new IllegalStateException("Response is not valid JSON");
        }
        return new JSONObject(trimmed.substring(start, end + 1));
    }

    private String formatIngredients(List<RecipeIngredient> ingredients) {
        StringBuilder sb = new StringBuilder();
        for (RecipeIngredient ingredient : ingredients) {
            sb.append("\u2022 ")
              .append(ingredient.getQuantity())
              .append(" ")
              .append(ingredient.getName())
              .append("\n");
        }
        return sb.toString().trim();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
