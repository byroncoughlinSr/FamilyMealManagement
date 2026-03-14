package org.coughlin.grocerylist;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

public class RecipeActivity extends AppCompatActivity {

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
