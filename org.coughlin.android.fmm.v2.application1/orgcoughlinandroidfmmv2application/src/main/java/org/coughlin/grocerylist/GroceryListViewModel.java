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
                selectedProductsLive.postValue(allProducts != null ? allProducts : new ArrayList<>()));
    }
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
        observeOnce(liveProduct, product -> {
            if (product != null) {
                GroceryListDatabase.databaseWriteExecutor.execute(() -> {
                    product.setSelected(true);
                    repository.update(product);
                    List<Product> updatedList = repository.getSelectedProducts();
                    selectedProductLiveData.postValue(updatedList);
                });
            } else {
                Log.w("GroceryListViewModel", "Product with ID " + productId + " not found.");
            }
        });
    }
    public static <T> void observeOnce(LiveData<T> liveData, Observer<T> observer) {
        liveData.observeForever(new Observer<>() {
            @Override
            public void onChanged(T t) {
                liveData.removeObserver(this);
                observer.onChanged(t);
            }
        });
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

