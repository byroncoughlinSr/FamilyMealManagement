package org.coughlin.grocerylist;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.List;

public class GroceryListRepository {
    private final ProductDao productDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    public GroceryListRepository(Application application) {
        GroceryListDatabase db = GroceryListDatabase.getDatabase(application);
        productDao = db.productDao();
    }
    public List<Product> getFilteredProducts(String query) {
        return productDao.searchSelectedProducts(query);
    }
    public void getProductById(int productId, Callback<Product> callback) {
        executorService.execute(() -> {
            Product product = productDao.getProductById(productId);
            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(product));
        });
    }
    public void uncheckProduct(int productId) {
        executorService.execute(() -> productDao.uncheckProduct(productId));
    }
    public void delete(Product product) {
        executorService.execute(() -> productDao.delete(product));
    }
    public interface Callback<T> {
        void onResult(T result);
    }
    public void checkProduct(int id) {
        GroceryListDatabase.databaseWriteExecutor.execute(() -> productDao.checkProduct(id));
    }
    public void unCheckProduct(int id) {
        GroceryListDatabase.databaseWriteExecutor.execute(() -> productDao.uncheckProduct(id));
    }
    public void update(Product product) {
        GroceryListDatabase.databaseWriteExecutor.execute(() -> productDao.update(product));
    }
}
