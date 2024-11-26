package org.coughlin.grocerylist;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class GroceryListViewModel extends AndroidViewModel {
    private final GroceryListRepository repository;
    private final ProductDao productDao;
    private final MutableLiveData<List<Product>> selectedProductsLive = new MutableLiveData<>();

    public GroceryListViewModel(@NonNull Application application) {
        super(application);  // Passes the application to AndroidViewModel
        repository = new GroceryListRepository(application);
        GroceryListDatabase dbHelper = GroceryListDatabase.getDatabase(application);
        productDao = dbHelper.productDao();
    }
    public LiveData<List<String>> getSelectedProductNames() {
        return productDao.getSelectedProductsName();
    }
    public LiveData<List<Product>> getSelectedProductsLive() {
        return productDao.getSelectedProductsLive();
    }
    public void selectProductById(int productId) {
        repository.getProductById(productId); // Update product in repository
    }
    public void updateSelectedProduct(int productId) {
        productDao.updateSelectProduct(productId);
    }

    public void filterProducts(String query) {
        if (query == null || query.isEmpty()) {
            // Reset to full list if no query
            selectedProductsLive.setValue(productDao.getAllProductsLive());
        } else {
            // Filter products based on the query
            GroceryListDatabase.databaseWriteExecutor.execute(() -> {
                List<Product> filteredProducts = productDao.getProductByName(query);
                selectedProductsLive.postValue(filteredProducts);
            });
        }
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
    public void selectProduct(int id) {
        repository.checkProduct(id);
    }

    public void unselectProduct(int id) {
        repository.unCheckProduct(id);
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
}
