package org.coughlin.grocerylist;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;

public class GroceryListRepository {
    private final ProductDao productDao;
    private final LiveData<List<Product>> allProducts;

    public GroceryListRepository(Application application) {
        GroceryListDatabase db = GroceryListDatabase.getDatabase(application);
        productDao = db.productDao();
        allProducts = productDao.getAllProductsLive();
    }

    public LiveData<List<Product>> getAllProducts() {
        return allProducts;
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

    public void deleteAllProducts() {
        GroceryListDatabase.databaseWriteExecutor.execute(productDao::deleteAll);
    }
}
