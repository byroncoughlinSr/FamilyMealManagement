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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAllReturnIds(List<DailyMenu> dailyMenus);

    @Query("SELECT * FROM tblDailyMenu WHERE menuDate = :date ORDER BY " +
            "CASE mealType WHEN 'breakfast' THEN 1 WHEN 'lunch' THEN 2 WHEN 'dinner' THEN 3 END")
    LiveData<List<DailyMenu>> getMealsForDate(String date);

    @Query("SELECT * FROM tblDailyMenu WHERE menuDate = :date ORDER BY " +
            "CASE mealType WHEN 'breakfast' THEN 1 WHEN 'lunch' THEN 2 WHEN 'dinner' THEN 3 END")
    List<DailyMenu> getMealsForDateSync(String date);

    @Query("DELETE FROM tblDailyMenu WHERE menuDate BETWEEN :startDate AND :endDate")
    void deleteMealsForDateRange(String startDate, String endDate);

    @Query("SELECT DISTINCT menuDate FROM tblDailyMenu WHERE menuDate BETWEEN :startDate AND :endDate ORDER BY menuDate")
    List<String> getDistinctDatesInRange(String startDate, String endDate);

    @Delete
    void delete(DailyMenu dailyMenu);

    @Query("SELECT * FROM tblDailyMenu WHERE mealDescription LIKE :query " +
            "GROUP BY mealDescription ORDER BY menuDate DESC LIMIT 50")
    List<DailyMenu> searchMealsByDescription(String query);

    @Query("SELECT * FROM tblDailyMenu WHERE menuDate BETWEEN :startDate AND :endDate " +
            "AND manuallySet = 1 ORDER BY menuDate")
    List<DailyMenu> getManualMealsInRange(String startDate, String endDate);

    @Query("SELECT * FROM tblDailyMenu WHERE menuDate = :date AND mealType = :mealType LIMIT 1")
    DailyMenu getMealByDateAndTypeSync(String date, String mealType);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertReturnId(DailyMenu dailyMenu);

    @Query("DELETE FROM tblDailyMenu WHERE menuDate BETWEEN :startDate AND :endDate AND manuallySet = 0")
    void deleteNonManualMealsForDateRange(String startDate, String endDate);

    @Query("SELECT * FROM tblDailyMenu WHERE _id = :id LIMIT 1")
    DailyMenu getMenuByIdSync(int id);
}
