package org.coughlin.grocerylist;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HistoryViewModel extends AndroidViewModel {
    private final HistoryRepository repository;
    private final HistoryDao historyDao;
    private ArrayList<String> historyListView = new ArrayList<>();
    private final LiveData<List<ProductHistory>> allHistory;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        repository = new HistoryRepository(application);
        GroceryListDatabase dbHelper = GroceryListDatabase.getDatabase(application);
        historyDao = dbHelper.historyDao();
        allHistory = historyDao.getAllHistory();
    }
    public void insertNewHistory(int proId, String proName) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = LocalDate.now().format(formatter);
        ProductHistory history = new ProductHistory(formattedDate, proId);
        repository.insert(history);
    }
    public LiveData<Product> getProductNameById(int productId) {
        return repository.getProductNameById((productId));
    }
    public LiveData<List<ProductHistory>> getAllHistory() {
        return repository.getAllHistory();
    }
    public void deleteHistoryById(int productId) {
        repository.deleteHistoryById((productId));
    }

    public HistoryDao getHistoryDao() {
        return historyDao;
    }
}
