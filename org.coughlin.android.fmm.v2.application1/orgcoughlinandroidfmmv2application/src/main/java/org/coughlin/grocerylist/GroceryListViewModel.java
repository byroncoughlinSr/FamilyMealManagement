package org.coughlin.grocerylist;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.List;
import java.util.stream.Collectors;

public class GroceryListViewModel extends AndroidViewModel {
    private final GroceryListRepository repository;
    private final LiveData<List<Product>> selectedProducts;
    private final LiveData<List<String>> selectedProductNames;
    private final ProductDao productDao;

    public GroceryListViewModel(@NonNull Application application) {
        super(application);  // Passes the application to AndroidViewModel
        repository = new GroceryListRepository(application);
        selectedProducts = repository.getSelectedProducts();
        selectedProductNames = repository.getSelectedProductNames();
        GroceryListDatabase dbHelper = GroceryListDatabase.getDatabase(application);
        productDao = dbHelper.productDao();
    }

    public LiveData<List<Product>> getSelectedProducts() {
        return selectedProducts;
    }
    public LiveData<List<String>> getProductNames() {
        return selectedProductNames;
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
