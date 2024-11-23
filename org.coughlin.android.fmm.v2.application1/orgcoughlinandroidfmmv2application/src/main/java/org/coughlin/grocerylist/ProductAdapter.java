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
    private final List<Product> productList;
    private final GroceryListViewModel viewModel;
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
    public ProductAdapter(@NonNull List<Product> productList, @NonNull GroceryListViewModel viewModel) {
        this.productList = productList;
        this.viewModel = viewModel;
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
        Product product = productList.get(position);
        holder.getTextView().setText(product.getName());
        holder.getTextView().setChecked(product.isChecked());

        holder.getTextView().setOnClickListener(view -> {
            boolean isChecked = !holder.getTextView().isChecked();
            holder.getTextView().setChecked(isChecked);

            // Save the state to the database via ViewModel
            if (isChecked) {
                viewModel.selectProduct(product.getId());
            } else {
                viewModel.unselectProduct(product.getId());
            }
        });
    }
    @Override
    public int getItemCount() {
        return productList.size();
    }
}
