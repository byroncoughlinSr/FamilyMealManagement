package org.coughlin.grocerylist;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public class ProductViewModel extends AndroidViewModel {
    private final ProductRepository repository;
    public ProductViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);
        GroceryListDatabase dbHelper = GroceryListDatabase.getDatabase(application);
        ProductDao productDao = dbHelper.productDao();
    }
    public void insertNewProduct(String product) {
        boolean isChecked = false;
        boolean isSelected = false;
        Product newProduct = new Product(product, isChecked, isSelected);
        repository.insert(newProduct);
    }

}
