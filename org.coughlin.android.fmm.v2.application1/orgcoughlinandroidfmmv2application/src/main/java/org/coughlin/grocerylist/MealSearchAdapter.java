package org.coughlin.grocerylist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MealSearchAdapter extends RecyclerView.Adapter<MealSearchAdapter.ViewHolder> {

    public interface OnMealSelectedListener {
        void onMealSelected(DailyMenu meal);
    }

    private final List<DailyMenu> meals;
    private final OnMealSelectedListener listener;

    public MealSearchAdapter(List<DailyMenu> meals, OnMealSelectedListener listener) {
        this.meals = meals;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DailyMenu meal = meals.get(position);
        holder.textMealDescription.setText(meal.getMealDescription());
        holder.textMealDate.setText(meal.getMealType() + " - " + meal.getMenuDate());
        holder.itemView.setOnClickListener(v -> listener.onMealSelected(meal));
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textMealDescription;
        final TextView textMealDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textMealDescription = itemView.findViewById(R.id.textMealDescription);
            textMealDate = itemView.findViewById(R.id.textMealDate);
        }
    }
}
