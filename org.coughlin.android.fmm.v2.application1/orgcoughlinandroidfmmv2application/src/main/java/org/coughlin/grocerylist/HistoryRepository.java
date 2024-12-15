package org.coughlin.grocerylist;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryRepository {
    private final HistoryDao historyDao;
    private final ProductDao productDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public HistoryRepository(Application application) {
        GroceryListDatabase db = GroceryListDatabase.getDatabase(application);
        historyDao = db.historyDao();
        productDao = db.productDao();
    }

    public void insert(ProductHistory history) {
        executorService.execute(() -> historyDao.insert(history));
    }
    public LiveData<List<ProductHistory>> getAllHistory() {
        return historyDao.getAllHistory();
    }
    public LiveData<Product> getProductNameById(int productId) {
        return productDao.getProductById(productId);
    }
}



