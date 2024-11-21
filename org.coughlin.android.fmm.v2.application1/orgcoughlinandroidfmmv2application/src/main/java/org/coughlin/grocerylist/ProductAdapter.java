package org.coughlin.grocerylist;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<String> productNames;
    private final SparseBooleanArray checkedStates = new SparseBooleanArray();
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final CheckedTextView productNameTextView;
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.item);
        }
        public CheckedTextView getTextView() {
            return productNameTextView;
        }
    }
    // Constructor
    public ProductAdapter(@NonNull List<String> productList) {
        this.productNames = productList;
    }
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grocerylist_item, parent, false);
        return new ProductViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.getTextView().setText(productNames.get(position));
    }
    @Override
    public int getItemCount() {
        return productNames.size();
    }
}
