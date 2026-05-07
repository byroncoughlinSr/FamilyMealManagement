package org.coughlin.grocerylist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProductSuggestionAdapter extends RecyclerView.Adapter<ProductSuggestionAdapter.ViewHolder> {

    public interface OnProductSelectedListener {
        void onProductSelected(Product product);
    }

    public interface OnProductLongClickListener {
        void onProductLongClick(Product product);
    }

    private final List<Product> products;
    private final OnProductSelectedListener selectListener;
    private final OnProductLongClickListener longClickListener;

    public ProductSuggestionAdapter(List<Product> products,
                                    OnProductSelectedListener selectListener,
                                    OnProductLongClickListener longClickListener) {
        this.products = products;
        this.selectListener = selectListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.textProductName.setText(product.getName());
        holder.itemView.setOnClickListener(v -> selectListener.onProductSelected(product));
        holder.itemView.setOnLongClickListener(v -> {
            longClickListener.onProductLongClick(product);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textProductName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textProductName = itemView.findViewById(R.id.textProductName);
        }
    }
}
