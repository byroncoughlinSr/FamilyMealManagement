package org.coughlin.grocerylist;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import java.util.List;

public class ProductViewModel extends AndroidViewModel {
    private final ProductRepository repository;
    public ProductViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);
        GroceryListDatabase.getDatabase(application);
    }
    public void insertNewProduct(String product) {
        boolean isChecked = false;
        boolean isSelected = false;
        Product newProduct = new Product(product, isChecked, isSelected);
        repository.insert(newProduct);
    }
    public List<Product> searchProductsByName(String query) {
        return repository.searchProductsByName(query);
    }
    public void deleteProduct(Product product) {
        repository.delete(product);
    }
}
