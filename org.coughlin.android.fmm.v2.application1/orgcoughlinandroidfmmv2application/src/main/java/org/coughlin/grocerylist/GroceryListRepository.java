package org.coughlin.grocerylist;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;


public class GroceryListRepository {
    private final ProductDao productDao;
    private final LiveData<List<Product>> allProducts;
    private final LiveData<List<Product>> selectedProducts;
    private final LiveData<List<String>> selectedProductName;

    public GroceryListRepository(Application application) {
        GroceryListDatabase db = GroceryListDatabase.getDatabase(application);
        productDao = db.productDao();
        allProducts = productDao.getAllProductsLive();
        selectedProducts = productDao.getSelectedProductsLive();
        selectedProductName = productDao.getSelectedProductsName();
    }
    public LiveData<List<Product>> getAllProducts() {
        return allProducts;
    }
    public LiveData<List<Product>> getSelectedProducts() {
        return selectedProducts;
    }
    public LiveData<List<String>> getSelectedProductNames() {
        return selectedProductName;
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
