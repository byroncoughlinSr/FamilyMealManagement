package org.coughlin.grocerylist;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RecipeIngredientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RecipeIngredient> ingredients);

    @Query("SELECT * FROM tblRecipeIngredient WHERE recipeId = :recipeId")
    LiveData<List<RecipeIngredient>> getIngredientsForRecipe(int recipeId);

    @Query("SELECT * FROM tblRecipeIngredient WHERE recipeId = :recipeId")
    List<RecipeIngredient> getIngredientsForRecipeSync(int recipeId);

    @Query("DELETE FROM tblRecipeIngredient WHERE recipeId = :recipeId")
    void deleteIngredientsForRecipe(int recipeId);

    /** Number of ingredient rows (in any recipe) whose name matches, case-insensitive. */
    @Query("SELECT COUNT(*) FROM tblRecipeIngredient WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name))")
    int countIngredientsByName(String name);

    @Query("DELETE FROM tblRecipeIngredient WHERE recipeId IN " +
            "(SELECT _id FROM tblRecipe WHERE dailyMenuId IN " +
            "(SELECT _id FROM tblDailyMenu WHERE menuDate BETWEEN :startDate AND :endDate))")
    void deleteIngredientsForDateRange(String startDate, String endDate);
}
