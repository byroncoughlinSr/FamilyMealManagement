package org.coughlin.grocerylist;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.List;

public class ProductRepository {
    private final ProductDao productDao;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    public ProductRepository(Application application) {
        GroceryListDatabase db = GroceryListDatabase.getDatabase(application);
        productDao = db.productDao();
    }
    public void insert(Product product) {
        executorService.execute(() -> productDao.insert(product));
    }
    public void delete(Product product) {
        executorService.execute(() -> productDao.delete(product));
    }
    public void update(Product product) {
        executorService.execute(() -> productDao.update(product));
    }
    public LiveData<List<Product>> getProductByName(String productName) {
        return productDao.getProductByName(productName);
    }
    public LiveData<Product> getProductById(int productId) {
        return productDao.getProductById(productId);
    };
    public Product getProductByIdSync(int productId) {
        return productDao.getProductByIdSync(productId);
    }
    public List<Product> getFilteredProducts(String query) {
        return productDao.searchSelectedProducts(query);
    }
    public List<Product> getAllProductsSync() {
        return productDao.getAllProductsSync();
    }
    public LiveData<List<Product>> getAllProducts() {
        return productDao.getAllProducts();
    }
    public List<Product> getSelectedProducts() {
        return productDao.getSelectedProductsSync();
    }
    public void uncheckProduct(int productId) {
        executorService.execute(() -> productDao.uncheckProduct(productId));
    }
    public void checkProduct(int id) {
       executorService.execute(() -> productDao.checkProduct(id));
    }
    public void unCheckProduct(int id) {
        executorService.execute(() -> productDao.uncheckProduct(id));
    }
}
