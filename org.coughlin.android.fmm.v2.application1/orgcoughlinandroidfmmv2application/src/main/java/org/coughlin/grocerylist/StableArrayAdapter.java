package org.coughlin.grocerylist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * StableArrayAdapter for managing a list of grocery items using a List<Product>.
 */
public class StableArrayAdapter extends ArrayAdapter<Product> {
    private final Context mContext;
    private List<Product> mProducts;
    private final int mResource;
    private final View.OnTouchListener mTouchListener;

    public StableArrayAdapter(@NonNull Context context, int resource, @NonNull List<Product> objects, View.OnTouchListener listener) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
        this.mProducts = objects;
        this.mTouchListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate the view if it's not already created
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(mResource, parent, false);
        }

        // Get the current product
        Product product = mProducts.get(position);

        // Find the CheckedTextView and set its properties
        CheckedTextView item = view.findViewById(R.id.item);
        if (item != null) {
            item.setText(product.getName());
            item.setChecked(product.isChecked());
        }

        // Add touch listener to track swipe motion
        view.setOnTouchListener(mTouchListener);

        return view;
    }

    /**
     * Update the list of products and notify the adapter.
     *
     * @param products The new list of products.
     */
    public void setProducts(List<Product> products) {
        this.mProducts = products;
        notifyDataSetChanged();
    }
}

