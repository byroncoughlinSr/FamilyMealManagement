package org.coughlin.grocerylist;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GroceryListViewModel extends AndroidViewModel {
    private final ItemTouchHelper itemTouchHelper;
    private final GroceryItemTouchHelperCallback itemTouchHelperCallback = new GroceryItemTouchHelperCallback();
    private final GroceryListRepository repository;
    private final ProductDao productDao;
    private final MutableLiveData<List<Product>> selectedProductsLive = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Product>> selectedProductLiveData = new MutableLiveData<>();

    public class GroceryItemTouchHelperCallback extends ItemTouchHelper.Callback {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            // Enable swipe right and left
            return makeMovementFlags(0, ItemTouchHelper.RIGHT);
        }


        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            if (direction == ItemTouchHelper.RIGHT) {
                int position = viewHolder.getAdapterPosition();
                String query = ""; // Adjust based on your filtering logic

                // Run the database query on a background thread
                GroceryListDatabase.databaseWriteExecutor.execute(() -> {
                    List<Product> filteredList = query.isEmpty()
                            ? productDao.getAllProductsSync()
                            : productDao.getProductByNameSync(query);

                    if (filteredList != null && position >= 0 && position < filteredList.size()) {
                        Product product = filteredList.get(position);

                        if (product != null) {
                            if(product.isChecked()) {
                                productDao.uncheckProduct(product.getId());
                            }
                            productDao.unSelectProduct(product.getId());

                            // Remove the product from the list
                            filteredList.remove(position);

                            // Post the updated list back to LiveData on the main thread
                            selectedProductsLive.postValue(filteredList);
                        }
                    } else {
                        Log.e("ItemTouchHelper", "List is null or position is invalid.");
                    }
                });
            }
        }

    }

        public GroceryListViewModel(@NonNull Application application) {
        super(application);
        itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        repository = new GroceryListRepository(application);
        GroceryListDatabase dbHelper = GroceryListDatabase.getDatabase(application);
        productDao = dbHelper.productDao();
        GroceryListDatabase.databaseWriteExecutor.execute(() -> {
            List<Product> allProducts = productDao.getAllProductsSync();
            selectedProductsLive.postValue(allProducts != null ? allProducts : new ArrayList<>());
        });
    }

    public LiveData<List<Product>> getSelectedProductsLive() {
        return productDao.getSelectedProductsLive();
    }
    public MutableLiveData<List<Product>> getSelectedProductLive() {
        return selectedProductLiveData;
    }
    public void filterProducts(String query) {
        GroceryListDatabase.databaseWriteExecutor.execute(() -> {
            List<Product> filteredProducts = (query == null || query.isEmpty())
                    ? productDao.getAllProductsSync() // Use synchronous fetch
                    : productDao.getProductByNameSync(query);

            selectedProductsLive.postValue(filteredProducts != null ? filteredProducts : new ArrayList<>());
        });
    }


    public void selectProductById(int productId) {
        repository.getProductById(productId, result -> {
            if (result != null) {
                addToList(result.getId());
                List<Product> productList = Collections.singletonList(result);
                selectedProductLiveData.postValue(productList);
            }
        });
    }
    public void addToList(int id) {
        new Thread(() -> {
            Product product = productDao.getProductById(id);
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
    public void update(Product product) {
        repository.update(product);
    }
    public ItemTouchHelper getItemTouchHelper() {
        return itemTouchHelper;
    }
}

