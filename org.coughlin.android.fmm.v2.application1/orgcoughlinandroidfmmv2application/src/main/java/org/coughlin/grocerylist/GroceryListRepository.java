package org.coughlin.grocerylist;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;


public class GroceryListRepository {
    private final ProductDao productDao;

    public GroceryListRepository(Application application) {
        GroceryListDatabase db = GroceryListDatabase.getDatabase(application);
        productDao = db.productDao();
    }
    public void insert(Product product) {
        GroceryListDatabase.databaseWriteExecutor.execute(() -> productDao.insert(product));
    }
    public void update(Product product) {
        GroceryListDatabase.databaseWriteExecutor.execute(() -> productDao.update(product));
    }
    public void delete(Product product) {
        GroceryListDatabase.databaseWriteExecutor.execute(() -> productDao.delete(product));
    }
    public void deleteProduct(String productName) {
        // You need to make sure you can find the product by its name,
        // either by using an async task or calling it on a background thread.
        new Thread(() -> {
            Product product = productDao.getProductByName(productName);
            if (product != null) {
                productDao.delete(product);
            }
        }).start();
    }
}
