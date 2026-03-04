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
    private final GroceryListViewModel viewModel;
    private final SparseBooleanArray checkedStates = new SparseBooleanArray();
    private List<Product> productList;

    // ViewHolder class
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
    public ProductAdapter(@NonNull List<Product> productList,
                          @NonNull GroceryListViewModel viewModel) {
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
    public Product getProductAtPosition(int position) {
        return productList.get(position);
    }
    @Override
    public int getItemCount() {
        return productList.size();
    }

    // Method to update the product list and refresh the RecyclerView
    public void updateProducts(List<Product> products) {
        this.productList.clear(); // Clear old data
        this.productList.addAll(products); // Add new data
        notifyDataSetChanged(); // Notify the adapter to refresh the UI
    }

}
