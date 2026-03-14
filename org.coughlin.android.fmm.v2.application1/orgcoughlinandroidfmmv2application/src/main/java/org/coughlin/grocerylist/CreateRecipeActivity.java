package org.coughlin.grocerylist;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CreateRecipeActivity extends AppCompatActivity {

    private static final String TAG = "CreateRecipeActivity";
    public static final String EXTRA_MEAL_TYPE = "extra_meal_type";
    public static final String EXTRA_DATE = "extra_date";
    public static final String EXTRA_MEAL_DESCRIPTION = "extra_meal_description";
    public static final String EXTRA_DAILY_MENU_ID = "extra_daily_menu_id";

    private AutoCompleteTextView autoCompleteIngredient;
    private EditText editQuantity;
    private EditText editProcedure;
    private LinearLayout layoutIngredientsList;
    private final List<IngredientEntry> ingredientEntries = new ArrayList<>();
    private ArrayAdapter<String> suggestionAdapter;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);

        String mealType = getIntent().getStringExtra(EXTRA_MEAL_TYPE);
        String targetDate = getIntent().getStringExtra(EXTRA_DATE);
        String existingDescription = getIntent().getStringExtra(EXTRA_MEAL_DESCRIPTION);
        int dailyMenuId = getIntent().getIntExtra(EXTRA_DAILY_MENU_ID, -1);
        boolean isEditMode = dailyMenuId != -1;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode
                    ? getString(R.string.edit_recipe)
                    : "New " + mealType);
        }

        EditText editMealDesc = findViewById(R.id.editTextMealDescription);
        editQuantity = findViewById(R.id.editTextQuantity);
        autoCompleteIngredient = findViewById(R.id.autoCompleteIngredient);
        ImageButton btnAddIngredient = findViewById(R.id.btnAddIngredient);
        layoutIngredientsList = findViewById(R.id.layoutIngredientsList);
        editProcedure = findViewById(R.id.editTextProcedure);
        Button btnSave = findViewById(R.id.btnSaveRecipe);
        Button btnGenerateRecipe = findViewById(R.id.btnGenerateRecipe);

        if (isEditMode) {
            btnGenerateRecipe.setVisibility(View.VISIBLE);
            btnGenerateRecipe.setOnClickListener(v -> generateRecipeWithAI(editMealDesc));
        }

        if (existingDescription != null && !existingDescription.isEmpty()) {
            editMealDesc.setText(existingDescription);
        }

        // Set up autocomplete with product search
        suggestionAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        autoCompleteIngredient.setAdapter(suggestionAdapter);

        autoCompleteIngredient.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.length() >= 1) {
                    searchProducts(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnAddIngredient.setOnClickListener(v -> addIngredient());

        btnSave.setOnClickListener(v -> {
            String description = editMealDesc.getText().toString().trim();
            String procedureText = editProcedure.getText().toString().trim();

            if (description.isEmpty()) {
                editMealDesc.setError(getString(R.string.meal_description_required));
                return;
            }

            saveRecipe(targetDate, mealType, description, procedureText);
        });

        // If editing, load existing recipe data
        if (isEditMode) {
            loadExistingRecipe(dailyMenuId);
        }
    }

    private void loadExistingRecipe(int dailyMenuId) {
        GroceryListDatabase.databaseWriteExecutor.execute(() -> {
            GroceryListDatabase db = GroceryListDatabase.getDatabase(getApplication());
            Recipe recipe = db.recipeDao().getRecipeForMenuSync(dailyMenuId);
            if (recipe == null) return;

            List<RecipeIngredient> ingredients =
                    db.recipeIngredientDao().getIngredientsForRecipeSync(recipe.getId());

            mainHandler.post(() -> {
                // Pre-fill procedure
                if (recipe.getProcedure() != null && !recipe.getProcedure().isEmpty()) {
                    editProcedure.setText(recipe.getProcedure());
                }

                // Pre-fill ingredients
                if (ingredients != null) {
                    for (RecipeIngredient ing : ingredients) {
                        IngredientEntry entry = new IngredientEntry(ing.getName(), ing.getQuantity());
                        ingredientEntries.add(entry);
                        addIngredientRow(entry);
                    }
                }
            });
        });
    }

    private void searchProducts(String query) {
        GroceryListDatabase.databaseWriteExecutor.execute(() -> {
            List<Product> products = GroceryListDatabase.getDatabase(getApplication())
                    .productDao().searchProductsByName("%" + query + "%");
            List<String> names = new ArrayList<>();
            for (Product p : products) {
                names.add(p.getName());
            }
            mainHandler.post(() -> {
                suggestionAdapter.clear();
                suggestionAdapter.addAll(names);
                suggestionAdapter.notifyDataSetChanged();
            });
        });
    }

    private void addIngredient() {
        String name = autoCompleteIngredient.getText().toString().trim();
        String quantity = editQuantity.getText().toString().trim();

        if (name.isEmpty()) {
            autoCompleteIngredient.setError(getString(R.string.ingredient_name_required));
            return;
        }

        IngredientEntry entry = new IngredientEntry(name, quantity);
        ingredientEntries.add(entry);
        addIngredientRow(entry);

        // Clear fields for next entry
        autoCompleteIngredient.setText("");
        editQuantity.setText("");
        autoCompleteIngredient.requestFocus();
    }

    private void addIngredientRow(IngredientEntry entry) {
        View row = LayoutInflater.from(this)
                .inflate(R.layout.item_ingredient_row, layoutIngredientsList, false);

        TextView textInfo = row.findViewById(R.id.textIngredientInfo);
        ImageButton btnRemove = row.findViewById(R.id.btnRemoveIngredient);

        String display = entry.quantity.isEmpty()
                ? "\u2022 " + entry.name
                : "\u2022 " + entry.quantity + " " + entry.name;
        textInfo.setText(display);

        btnRemove.setOnClickListener(v -> {
            ingredientEntries.remove(entry);
            layoutIngredientsList.removeView(row);
        });

        layoutIngredientsList.addView(row);
    }

    private void generateRecipeWithAI(EditText editMealDesc) {
        String mealName = editMealDesc.getText().toString().trim();
        if (mealName.isEmpty()) {
            editMealDesc.setError(getString(R.string.meal_description_required));
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.generating_recipe));
        progressDialog.setCancelable(false);
        progressDialog.show();

        GroceryListDatabase.databaseWriteExecutor.execute(() -> {
            try {
                String prompt = "Generate a recipe for \"" + mealName + "\". "
                        + "Return ONLY valid JSON (no markdown, no extra text) in this exact format: "
                        + "{\"ingredients\": [{\"name\": \"ingredient name\", \"quantity\": \"amount with unit\"}], "
                        + "\"procedure\": \"Step 1: First step\\nStep 2: Second step\"} "
                        + "Requirements: practical family recipe, 5-10 ingredients, "
                        + "4-6 preparation steps each prefixed with 'Step N:', "
                        + "use standardized ingredient names.";

                Log.d(TAG, "Sending prompt to Ollama: " + prompt);
                String response = MenuGenerationWorker.callOllama(prompt);
                Log.d(TAG, "Raw Ollama response: " + response);

                String jsonStr = response.trim();
                // Strip markdown code fences if present
                if (jsonStr.startsWith("```")) {
                    jsonStr = jsonStr.replaceAll("^```[a-zA-Z]*\\n?", "").replaceAll("```$", "").trim();
                }
                Log.d(TAG, "Cleaned JSON string: " + jsonStr);

                JSONObject json = new JSONObject(jsonStr);
                JSONArray ingredientsArray = json.getJSONArray("ingredients");
                String procedure = json.getString("procedure");
                Log.d(TAG, "Parsed " + ingredientsArray.length() + " ingredients, procedure length: " + procedure.length());

                List<IngredientEntry> generatedIngredients = new ArrayList<>();
                for (int i = 0; i < ingredientsArray.length(); i++) {
                    JSONObject ing = ingredientsArray.getJSONObject(i);
                    generatedIngredients.add(new IngredientEntry(
                            ing.getString("name"), ing.getString("quantity")));
                }

                mainHandler.post(() -> {
                    progressDialog.dismiss();
                    showRecipePreviewDialog(generatedIngredients, procedure);
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to generate recipe", e);
                mainHandler.post(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, getString(R.string.recipe_generation_failed),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showRecipePreviewDialog(List<IngredientEntry> generatedIngredients, String procedure) {
        StringBuilder preview = new StringBuilder();
        preview.append("Ingredients:\n");
        for (IngredientEntry entry : generatedIngredients) {
            preview.append("\u2022 ");
            if (!entry.quantity.isEmpty()) {
                preview.append(entry.quantity).append(" ");
            }
            preview.append(entry.name).append("\n");
        }
        preview.append("\nPreparation Steps:\n").append(procedure);

        ScrollView scrollView = new ScrollView(this);
        TextView textView = new TextView(this);
        textView.setText(preview.toString());
        textView.setPadding(48, 32, 48, 32);
        textView.setTextSize(15);
        scrollView.addView(textView);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.generated_recipe_preview))
                .setView(scrollView)
                .setPositiveButton(R.string.save_recipe, (dialog, which) -> {
                    // Clear existing ingredients from UI
                    ingredientEntries.clear();
                    layoutIngredientsList.removeAllViews();

                    // Populate with generated ingredients
                    for (IngredientEntry entry : generatedIngredients) {
                        ingredientEntries.add(entry);
                        addIngredientRow(entry);
                    }

                    // Set procedure
                    editProcedure.setText(procedure);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void saveRecipe(String dateStr, String mealType, String description,
                            String procedureText) {
        GroceryListDatabase.databaseWriteExecutor.execute(() -> {
            GroceryListDatabase db = GroceryListDatabase.getDatabase(getApplication());
            DailyMenuDao menuDao = db.dailyMenuDao();
            RecipeDao recipeDao = db.recipeDao();
            RecipeIngredientDao ingredientDao = db.recipeIngredientDao();
            ProductDao productDao = db.productDao();

            // Delete existing meal for this date+type (FK cascade handles recipe+ingredients)
            DailyMenu existing = menuDao.getMealByDateAndTypeSync(dateStr, mealType);
            if (existing != null) {
                menuDao.delete(existing);
            }

            // Insert new DailyMenu
            DailyMenu newMeal = new DailyMenu(dateStr, mealType, description);
            newMeal.setManuallySet(true);
            long newMenuId = menuDao.insertReturnId(newMeal);

            // Insert Recipe if ingredients or procedure provided
            if (!ingredientEntries.isEmpty() || !procedureText.isEmpty()) {
                Recipe newRecipe = new Recipe((int) newMenuId, "", procedureText);
                long newRecipeId = recipeDao.insert(newRecipe);

                if (!ingredientEntries.isEmpty()) {
                    List<RecipeIngredient> ingredients = new ArrayList<>();
                    for (IngredientEntry entry : ingredientEntries) {
                        ingredients.add(new RecipeIngredient(
                                (int) newRecipeId, entry.name, entry.quantity));
                        
                        // Check if item exists in database and add to grocery list
                        updateGroceryList(entry.name, productDao);
                    }
                    ingredientDao.insertAll(ingredients);
                }
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Meal saved: " + description,
                        Toast.LENGTH_SHORT).show();
                finish();
            });
        });
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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private static class IngredientEntry {
        final String name;
        final String quantity;

        IngredientEntry(String name, String quantity) {
            this.name = name;
            this.quantity = quantity;
        }
    }
}
