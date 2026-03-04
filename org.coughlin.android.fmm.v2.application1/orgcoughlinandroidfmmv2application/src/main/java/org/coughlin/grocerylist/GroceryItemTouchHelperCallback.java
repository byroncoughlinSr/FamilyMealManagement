package org.coughlin.grocerylist;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.List;

public class GroceryItemTouchHelperCallback extends ItemTouchHelper.Callback {
    private final ProductAdapter adapter;
    private final ProductDao productDao;
    private final HistoryViewModel historyViewModel;
    private MutableLiveData<List<Product>> selectedProductsLive = new MutableLiveData<>(new ArrayList<>());

    // Constructor to inject dependencies
    public GroceryItemTouchHelperCallback(ProductAdapter adapter, ProductDao productDao,
                                          HistoryViewModel historyViewModel,
                                          MutableLiveData selectedProductsLive) {
        this.adapter = adapter;
        this.productDao = productDao;
        this.historyViewModel = historyViewModel;
        this.selectedProductsLive = selectedProductsLive;
    }

    public GroceryItemTouchHelperCallback(ProductAdapter mProductAdapter, ProductAdapter adapter, ProductDao productDao, HistoryViewModel historyViewModel) {
        this.adapter = adapter;
        this.productDao = productDao;
        this.historyViewModel = historyViewModel;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        // Enable swipe right and left
        return makeMovementFlags(0, ItemTouchHelper.RIGHT);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false; // We don't support drag & drop, only swipe
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        if (direction == ItemTouchHelper.RIGHT) {
            int position = viewHolder.getAdapterPosition();

            // Get the swiped product
            Product product = adapter.getProductAtPosition(position);

            if (product != null) {
                GroceryListDatabase.databaseWriteExecutor.execute(() -> {
                    productDao.unSelectProduct(product.getId());
                    LiveData<List<Product>> updatedSelectedProducts = productDao.getSelectedProducts();

                    // Post observation to the main thread
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        updatedSelectedProducts.observeForever(new Observer<List<Product>>() {
                            @Override
                            public void onChanged(List<Product> products) {
                                // Update the MutableLiveData with the new list of products
                                selectedProductsLive.postValue(products);

                                // Remove this observer to prevent memory leaks
                                updatedSelectedProducts.removeObserver(this);
                            }
                        });
                    });

                    // Log the unselected product in the history
                    historyViewModel.insertNewHistory(product.getId(), product.getName());
                });
            } else {
                Log.e("ItemTouchHelper", "Product not found at position " + position);
            }
        }
    }
}

