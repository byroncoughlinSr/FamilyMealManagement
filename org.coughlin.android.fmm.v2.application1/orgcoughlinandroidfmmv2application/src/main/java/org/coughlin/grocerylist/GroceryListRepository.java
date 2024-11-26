package org.coughlin.grocerylist;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;


public class GroceryListRepository {
    private final ProductDao productDao;
    private final GroceryListDatabase db;

    public GroceryListRepository(Application application) {
        db = GroceryListDatabase.getDatabase(application);
        productDao = db.productDao();
    }
    // Expose data for ContentProvider
    public List<Product> getProducts(String query) {
        return productDao.getAllProductsLive(); // DAO query returning List<Product>
    }

    public Product getProductById(int id) {
        return productDao.getProductById(id); // DAO query to fetch product by ID
    }
    public void checkProduct(int id) {
        GroceryListDatabase.databaseWriteExecutor.execute(() -> productDao.checkProduct(id));
    }

    public void unCheckProduct(int id) {
        GroceryListDatabase.databaseWriteExecutor.execute(() -> productDao.uncheckProduct(id));
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
}
