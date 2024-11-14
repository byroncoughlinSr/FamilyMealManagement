package org.coughlin.grocerylist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;

import java.util.List;

/**
 * StableArrayAdapter for managing a list of grocery items using a List<Product>.
 */
public class StableArrayAdapter extends ArrayAdapter<String> {
    private final Context mContext;
    private final int mResource;
    private final View.OnTouchListener mTouchListener;
    private List<String> mProductNames;

    public StableArrayAdapter(@NonNull Context context, int resource, List<String> names, View.OnTouchListener listener) {
        super(context, resource, names);
        this.mContext = context;
        this.mResource = resource;
        this.mProductNames = names;
        this.mTouchListener = listener;
    }
    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate the view if it's not already created
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(mResource, parent, false);
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
    public void setProducts(List<String> products) {
        this.mProductNames = products;
        notifyDataSetChanged();
    }
    public void setSelectedProducts(List<String> products) {
        this.mProductNames = products;
        notifyDataSetChanged();  // Notify the adapter that the data has changed
    }

    public void setProductNames(List<String> productNames) {
        this.mProductNames = productNames;
        notifyDataSetChanged();
    }
}

