package org.coughlin.grocerylist;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Recipe recipe);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<Recipe> recipes);

    @Query("SELECT * FROM tblRecipe WHERE dailyMenuId = :dailyMenuId LIMIT 1")
    LiveData<Recipe> getRecipeForMenu(int dailyMenuId);

    @Query("SELECT * FROM tblRecipe WHERE dailyMenuId = :dailyMenuId LIMIT 1")
    Recipe getRecipeForMenuSync(int dailyMenuId);

    @Query("DELETE FROM tblRecipe WHERE dailyMenuId IN " +
            "(SELECT _id FROM tblDailyMenu WHERE menuDate BETWEEN :startDate AND :endDate)")
    void deleteRecipesForDateRange(String startDate, String endDate);
}
