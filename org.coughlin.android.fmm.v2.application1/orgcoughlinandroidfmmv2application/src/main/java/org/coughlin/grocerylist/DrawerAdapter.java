package org.coughlin.grocerylist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ViewHolder> {
    private final List<String> drawerContents;
    private final OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public DrawerAdapter(List<String> drawerContents, OnItemClickListener listener) {
        this.drawerContents = drawerContents;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_drawer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(drawerContents.get(position));
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return drawerContents.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textView;

        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.drawer_item);
        }
    }
}
