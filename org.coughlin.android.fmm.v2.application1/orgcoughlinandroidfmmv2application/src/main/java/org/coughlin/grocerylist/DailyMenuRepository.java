package org.coughlin.grocerylist;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DailyMenuRepository {
    private final DailyMenuDao dailyMenuDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public DailyMenuRepository(Application application) {
        GroceryListDatabase db = GroceryListDatabase.getDatabase(application);
        dailyMenuDao = db.dailyMenuDao();
    }

    public LiveData<List<DailyMenu>> getMealsForDate(String date) {
        return dailyMenuDao.getMealsForDate(date);
    }

    public void insert(DailyMenu dailyMenu) {
        executorService.execute(() -> dailyMenuDao.insert(dailyMenu));
    }

    public void insertAll(List<DailyMenu> dailyMenus) {
        executorService.execute(() -> dailyMenuDao.insertAll(dailyMenus));
    }

    public void deleteMealsForDateRange(String startDate, String endDate) {
        executorService.execute(() -> dailyMenuDao.deleteMealsForDateRange(startDate, endDate));
    }

    public void delete(DailyMenu dailyMenu) {
        executorService.execute(() -> dailyMenuDao.delete(dailyMenu));
    }
}
