package org.coughlin.grocerylist;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import java.util.ArrayList;
import java.util.List;

public class GroceryListViewModel extends AndroidViewModel {

    private final ProductRepository repository;
    private final ProductDao productDao;
    private final MutableLiveData<List<Product>> selectedProductsLive = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Product>> selectedProductLiveData = new MutableLiveData<>();

    public GroceryListViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);
        GroceryListDatabase dbHelper = GroceryListDatabase.getDatabase(application);
        productDao = dbHelper.productDao();

        repository.getAllProducts().observeForever(allProducts ->
                selectedProductsLive.postValue(allProducts != null ? allProducts : new ArrayList<>()));    }
    public LiveData<List<Product>> getSelectedProducts() {
        return productDao.getSelectedProducts();    }
    public MutableLiveData<List<Product>> getSelectedProductLive() {
        return selectedProductLiveData;    }
    public void filterProducts(String query) {
        GroceryListDatabase.databaseWriteExecutor.execute(() -> {
            List<Product> filteredProducts = (query == null || query.isEmpty())
                    ? repository.getAllProducts().getValue()
                    : repository.getProductByName(query).getValue();
            List<Product> finalFilteredProducts = (filteredProducts != null) ? filteredProducts : new ArrayList<>();
            selectedProductsLive.postValue(finalFilteredProducts);
        });    }
    public void selectProductById(int productId) {
        LiveData<Product> liveProduct = repository.getProductById(productId);

        // Observe the product LiveData once
        observeOnce(liveProduct, product -> {
            if (product != null) {
                GroceryListDatabase.databaseWriteExecutor.execute(() -> {
                    product.setSelected(true); // Example: Mark the product as selected
                    repository.update(product); // Update the product in the database

                    // Fetch updated list and update LiveData for the adapter
                    List<Product> updatedList = repository.getSelectedProducts();
                    selectedProductLiveData.postValue(updatedList);
                });
            } else {
                Log.w("GroceryListViewModel", "Product with ID " + productId + " not found.");
            }
        });
    }

    public static <T> void observeOnce(LiveData<T> liveData, Observer<T> observer) {
        liveData.observeForever(new Observer<T>() {
            @Override
            public void onChanged(T t) {
                liveData.removeObserver(this); // Automatically remove the observer after the first update
                observer.onChanged(t);        // Notify the provided observer with the data
            }
        });
    }
    public void addToList(int productId) {
        // Perform the operation on a background thread
        GroceryListDatabase.databaseWriteExecutor.execute(() -> {
            // Fetch the product from the database
            Product product = productDao.getProductByIdSync(productId);

            if (product != null) {
                // Check if the product state actually changed
                if (!product.isChecked()) {
                    // If product is not checked, update its state
                    product.setChecked(false);
                }
                productDao.update(product);  // Update the product in the database

                    // Post the updated list of products to LiveData on the main thread
                selectedProductsLive.postValue(repository.getAllProducts().getValue());

                    // Optionally, insert a new history record (make sure this runs on the main thread if necessary)
                //historyViewModel.insertNewHistory(product.getId(), product.getName());
            }
        });

    }
    private void updateSelectedProductLiveData(Product product) {
        // Only post updated LiveData if necessary
        List<Product> updatedList = getUpdatedProductList();

        // Ensure no unnecessary updates
        if (updatedList != null && !updatedList.equals(selectedProductLiveData.getValue())) {
            selectedProductLiveData.postValue(updatedList);
        }
    }

    private List<Product> getUpdatedProductList() {
        // Fetch the updated product list, which could be from the database or repository
        return repository.getSelectedProducts();
    }
    public void selectProduct(int id) {
        repository.checkProduct(id);
    }
    public void unselectProduct(int id) {
        repository.unCheckProduct(id);
    }
    public void update(Product product) {
        repository.update(product);
    }
}

