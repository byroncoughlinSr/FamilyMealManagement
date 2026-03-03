package org.coughlin.grocerylist;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DailyMenuDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DailyMenu dailyMenu);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DailyMenu> dailyMenus);

    @Query("SELECT * FROM tblDailyMenu WHERE menuDate = :date ORDER BY " +
            "CASE mealType WHEN 'breakfast' THEN 1 WHEN 'lunch' THEN 2 WHEN 'dinner' THEN 3 END")
    LiveData<List<DailyMenu>> getMealsForDate(String date);

    @Query("SELECT * FROM tblDailyMenu WHERE menuDate = :date ORDER BY " +
            "CASE mealType WHEN 'breakfast' THEN 1 WHEN 'lunch' THEN 2 WHEN 'dinner' THEN 3 END")
    List<DailyMenu> getMealsForDateSync(String date);

    @Query("DELETE FROM tblDailyMenu WHERE menuDate BETWEEN :startDate AND :endDate")
    void deleteMealsForDateRange(String startDate, String endDate);

    @Delete
    void delete(DailyMenu dailyMenu);
}
