package org.coughlin.grocerylist;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HistoryViewModel extends AndroidViewModel {
    private final HistoryRepository repository;
    private ArrayList<String> historyListView = new ArrayList<>();

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        repository = new HistoryRepository(application);
        GroceryListDatabase.getDatabase(application);
    }
    public void insertNewHistory(int proId, String proName) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = LocalDate.now().format(formatter);
        ProductHistory history = new ProductHistory(formattedDate, proId);
        repository.insert(history);
    }
    public LiveData<List<ProductHistory>> getAllHistory() {
        return repository.getAllHistory();
    }
}
