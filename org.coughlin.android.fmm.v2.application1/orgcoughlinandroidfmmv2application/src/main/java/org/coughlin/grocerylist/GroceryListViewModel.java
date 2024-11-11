package org.coughlin.grocerylist;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class GroceryListViewModel extends AndroidViewModel {
    private final GroceryListRepository repository;
    private final LiveData<List<Product>> allProducts;
    private final ProductDao productDao;

    public GroceryListViewModel(@NonNull Application application) {
        super(application);  // Passes the application to AndroidViewModel
        repository = new GroceryListRepository(application);
        allProducts = repository.getAllProducts();
        GroceryListDatabase dbHelper = GroceryListDatabase.getDatabase(application);

        productDao = dbHelper.productDao();
    }

    public LiveData<List<Product>> getAllProducts() {
        return allProducts;
    }
    public void addToList(String id) {
        // Perform the update in a background thread
        new Thread(() -> {
            // Assuming you have a method in your DAO to update the product selection status
            Product product = productDao.getProductById(Integer.parseInt(id));  // Fetch the product first
            if (product != null) {
                product.setSelected(true);  // Set the selection status
                productDao.update(product);  // Update the product in the database
            }
        }).start();
    }

    public void insert(Product product) {
        repository.insert(product);
    }

    public void update(Product product) {
        repository.update(product);
    }

    public void delete(Product product) {
        repository.delete(product);
    }

    public void deleteAllProducts() {
        repository.deleteAllProducts();
    }
}
