package org.coughlin.grocerylist;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class HistoryTouchHelperCallback extends ItemTouchHelper.Callback {
    private final HistoryAdapter adapter;
    private final HistoryDao historyDao;
    private final HistoryViewModel historyViewModel;
    private MutableLiveData<List<Product>> selectedProductsLive = new MutableLiveData<>(new ArrayList<>());

    public HistoryTouchHelperCallback(HistoryAdapter adapter, HistoryDao historyDao, HistoryViewModel historyViewModel) {
        this.adapter = adapter;
        this.historyDao = historyDao;
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
            ProductHistory product = adapter.getProductAtPosition(position);
            if (product != null) {
                GroceryListDatabase.databaseWriteExecutor.execute(() -> {
                    historyViewModel.deleteHistoryById(product.getId());
                });
            } else {
                Log.e("ItemTouchHelper", "Product not found at position " + position);
            }
        }
    }
}

