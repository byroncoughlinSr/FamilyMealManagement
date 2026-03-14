package org.coughlin.grocerylist;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecipeRepository {
    private final RecipeDao recipeDao;
    private final RecipeIngredientDao ingredientDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public RecipeRepository(Application application) {
        GroceryListDatabase db = GroceryListDatabase.getDatabase(application);
        recipeDao = db.recipeDao();
        ingredientDao = db.recipeIngredientDao();
    }

    public LiveData<Recipe> getRecipeForMenu(int dailyMenuId) {
        return recipeDao.getRecipeForMenu(dailyMenuId);
    }

    public LiveData<List<RecipeIngredient>> getIngredientsForRecipe(int recipeId) {
        return ingredientDao.getIngredientsForRecipe(recipeId);
    }

    public void insert(Recipe recipe) {
        executorService.execute(() -> recipeDao.insert(recipe));
    }

    public void insertAll(List<Recipe> recipes) {
        executorService.execute(() -> recipeDao.insertAll(recipes));
    }

    public void insertAllIngredients(List<RecipeIngredient> ingredients) {
        executorService.execute(() -> ingredientDao.insertAll(ingredients));
    }

    public void deleteRecipesForDateRange(String startDate, String endDate) {
        executorService.execute(() -> recipeDao.deleteRecipesForDateRange(startDate, endDate));
    }
}
