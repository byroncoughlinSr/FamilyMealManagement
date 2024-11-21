package org.coughlin.grocerylist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * StableArrayAdapter for managing a list of grocery items using a List<Product>.
 */
public class StableArrayAdapter extends ArrayAdapter<String> {
    private final Context mContext;
    private final int mResource;
    private int mGrocerylistItem;

    private List<String> mProductNames;

    public StableArrayAdapter(@NonNull Context context, int resource, int grocerylist_item, List<String> names) {
        super(context, resource, names);
        this.mContext = context;
        this.mResource = resource;
        this.mProductNames = new ArrayList<>(names);
        this.mGrocerylistItem = grocerylist_item;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(mResource, parent, false);
        }

        // Bind data to the view
        String productName = mProductNames.get(position);
        TextView textView = view.findViewById(mGrocerylistItem); // mGrocerylistItem should be the ID of the TextView
        textView.setText(productName);

        return view;
    }


    @Override
    public void notifyDataSetChanged() {
        // Custom behavior before notifying changes
        if (mProductNames != null) {
            System.out.println("NotifyDataSetChanged called. Total items: " + mProductNames.size());
        }

        // Call the parent class's method to trigger the UI update
        super.notifyDataSetChanged();
    }


    public void setProductNames(List<String> productNames) {
        if (mProductNames != null) {
            mProductNames.clear();
            mProductNames.addAll(productNames);
            notifyDataSetChanged();
        }
    }
}


