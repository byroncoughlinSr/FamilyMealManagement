package org.coughlin.grocerylist;

import android.app.Application;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import java.util.List;


public class GroceryListRepository {
    private final ProductDao productDao;
    private final GroceryListDatabase db;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public GroceryListRepository(Application application) {
        db = GroceryListDatabase.getDatabase(application);
        productDao = db.productDao();
    }



    public void selectProductById(int productId) {
        executor.execute(() -> productDao.updateSelectProduct(productId));
    }


    // Expose data for ContentProvider
    public List<Product> getProducts(String query) {
        return productDao.getAllProductsLive(); // DAO query returning List<Product>
    }
    public List<Product> getFilteredProducts(String query) {
        return productDao.searchSelectedProducts(query);
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
