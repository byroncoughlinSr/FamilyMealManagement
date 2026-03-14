package org.coughlin.grocerylist;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

public class RecipeViewModel extends AndroidViewModel {
    private final RecipeRepository repository;
    private final MutableLiveData<Integer> dailyMenuId = new MutableLiveData<>();
    private final LiveData<Recipe> recipe;
    private final MutableLiveData<Integer> recipeId = new MutableLiveData<>();
    private final LiveData<List<RecipeIngredient>> ingredients;

    public RecipeViewModel(@NonNull Application application) {
        super(application);
        repository = new RecipeRepository(application);
        recipe = Transformations.switchMap(dailyMenuId,
                id -> repository.getRecipeForMenu(id));
        ingredients = Transformations.switchMap(recipeId,
                id -> repository.getIngredientsForRecipe(id));
    }

    public void setDailyMenuId(int id) {
        dailyMenuId.setValue(id);
    }

    public void setRecipeId(int id) {
        recipeId.setValue(id);
    }

    public LiveData<Recipe> getRecipe() {
        return recipe;
    }

    public LiveData<List<RecipeIngredient>> getIngredients() {
        return ingredients;
    }
}
