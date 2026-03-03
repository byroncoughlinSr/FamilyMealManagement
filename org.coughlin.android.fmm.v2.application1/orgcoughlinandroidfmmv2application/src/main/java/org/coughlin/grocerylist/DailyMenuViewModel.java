package org.coughlin.grocerylist;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DailyMenuViewModel extends AndroidViewModel {
    private final DailyMenuRepository repository;
    private final MutableLiveData<String> currentDate = new MutableLiveData<>();
    private final LiveData<List<DailyMenu>> mealsForCurrentDate;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public DailyMenuViewModel(@NonNull Application application) {
        super(application);
        repository = new DailyMenuRepository(application);
        currentDate.setValue(LocalDate.now().format(DATE_FORMATTER));
        mealsForCurrentDate = Transformations.switchMap(currentDate,
                date -> repository.getMealsForDate(date));
    }

    public LiveData<List<DailyMenu>> getMealsForCurrentDate() {
        return mealsForCurrentDate;
    }

    public LiveData<String> getCurrentDate() {
        return currentDate;
    }

    public void setDate(String date) {
        currentDate.setValue(date);
    }

    public void insertMeal(DailyMenu meal) {
        repository.insert(meal);
    }
}
