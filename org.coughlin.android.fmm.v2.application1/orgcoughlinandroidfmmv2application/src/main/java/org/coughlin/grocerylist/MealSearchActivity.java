package org.coughlin.grocerylist;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MealSearchActivity extends AppCompatActivity {

    public static final String EXTRA_MEAL_TYPE = "extra_meal_type";
    public static final String EXTRA_DATE = "extra_date";

    private MealSearchAdapter adapter;
    private final List<DailyMenu> searchResults = new ArrayList<>();
    private TextView textNoResults;
    private String targetDate;
    private String mealType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_search);

        mealType = getIntent().getStringExtra(EXTRA_MEAL_TYPE);
        targetDate = getIntent().getStringExtra(EXTRA_DATE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Search " + mealType);
        }

        textNoResults = findViewById(R.id.textNoResults);
        SearchView searchView = findViewById(R.id.searchViewMeals);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewResults);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MealSearchAdapter(searchResults, this::onMealSelected);
        recyclerView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() >= 2) {
                    performSearch(newText);
                } else if (newText.isEmpty()) {
                    searchResults.clear();
                    adapter.notifyDataSetChanged();
                    textNoResults.setVisibility(View.GONE);
                }
                return true;
            }
        });
    }

    private void performSearch(String query) {
        GroceryListDatabase.databaseWriteExecutor.execute(() -> {
            List<DailyMenu> results = GroceryListDatabase.getDatabase(getApplication())
                    .dailyMenuDao().searchMealsByDescription("%" + query + "%");
            runOnUiThread(() -> {
                searchResults.clear();
                searchResults.addAll(results);
                adapter.notifyDataSetChanged();
                textNoResults.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void onMealSelected(DailyMenu sourceMeal) {
        GroceryListDatabase.databaseWriteExecutor.execute(() -> {
            GroceryListDatabase db = GroceryListDatabase.getDatabase(getApplication());
            DailyMenuDao menuDao = db.dailyMenuDao();
            RecipeDao recipeDao = db.recipeDao();
            RecipeIngredientDao ingredientDao = db.recipeIngredientDao();

            // Delete existing meal for this date+type (FK cascade deletes recipe+ingredients)
            DailyMenu existing = menuDao.getMealByDateAndTypeSync(targetDate, mealType);
            if (existing != null) {
                menuDao.delete(existing);
            }

            // Insert new DailyMenu
            DailyMenu newMeal = new DailyMenu(targetDate, mealType, sourceMeal.getMealDescription());
            newMeal.setManuallySet(true);
            long newMenuId = menuDao.insertReturnId(newMeal);

            // Copy Recipe if source has one
            Recipe sourceRecipe = recipeDao.getRecipeForMenuSync(sourceMeal.getId());
            if (sourceRecipe != null) {
                Recipe newRecipe = new Recipe((int) newMenuId, sourceRecipe.getImageUrl(),
                        sourceRecipe.getProcedure());
                long newRecipeId = recipeDao.insert(newRecipe);

                // Copy RecipeIngredients
                List<RecipeIngredient> sourceIngredients =
                        ingredientDao.getIngredientsForRecipeSync(sourceRecipe.getId());
                if (sourceIngredients != null && !sourceIngredients.isEmpty()) {
                    List<RecipeIngredient> newIngredients = new ArrayList<>();
                    for (RecipeIngredient ing : sourceIngredients) {
                        newIngredients.add(new RecipeIngredient(
                                (int) newRecipeId, ing.getName(), ing.getQuantity()));
                    }
                    ingredientDao.insertAll(newIngredients);
                }
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Meal set: " + sourceMeal.getMealDescription(),
                        Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
