package org.coughlin.grocerylist;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DailyMenuFragment extends Fragment {
    private static final String ARG_DATE = "arg_date";
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
    private static final DateTimeFormatter DB_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DailyMenu breakfastMeal;
    private DailyMenu lunchMeal;
    private DailyMenu dinnerMeal;

    public static DailyMenuFragment newInstance(String date) {
        DailyMenuFragment fragment = new DailyMenuFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_daily_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String dateStr = requireArguments().getString(ARG_DATE);
        LocalDate date = LocalDate.parse(dateStr, DB_FORMATTER);

        TextView dateTextView = view.findViewById(R.id.textViewDate);
        TextView breakfastTextView = view.findViewById(R.id.textViewBreakfastContent);
        TextView lunchTextView = view.findViewById(R.id.textViewLunchContent);
        TextView dinnerTextView = view.findViewById(R.id.textViewDinnerContent);

        CardView cardBreakfast = view.findViewById(R.id.cardViewBreakfast);
        CardView cardLunch = view.findViewById(R.id.cardViewLunch);
        CardView cardDinner = view.findViewById(R.id.cardViewDinner);

        ImageButton btnEditBreakfast = view.findViewById(R.id.btnEditBreakfast);
        ImageButton btnEditLunch = view.findViewById(R.id.btnEditLunch);
        ImageButton btnEditDinner = view.findViewById(R.id.btnEditDinner);

        dateTextView.setText(date.format(DISPLAY_FORMATTER));

        cardBreakfast.setOnClickListener(v -> launchRecipe(breakfastMeal));
        cardLunch.setOnClickListener(v -> launchRecipe(lunchMeal));
        cardDinner.setOnClickListener(v -> launchRecipe(dinnerMeal));

        btnEditBreakfast.setOnClickListener(v -> showMealOptionsDialog("breakfast", dateStr));
        btnEditLunch.setOnClickListener(v -> showMealOptionsDialog("lunch", dateStr));
        btnEditDinner.setOnClickListener(v -> showMealOptionsDialog("dinner", dateStr));

        GroceryListDatabase db = GroceryListDatabase.getDatabase(
                requireActivity().getApplication());
        db.dailyMenuDao().getMealsForDate(dateStr).observe(getViewLifecycleOwner(), meals -> {
            breakfastTextView.setText("--");
            lunchTextView.setText("--");
            dinnerTextView.setText("--");
            breakfastMeal = null;
            lunchMeal = null;
            dinnerMeal = null;

            if (meals != null) {
                for (DailyMenu meal : meals) {
                    switch (meal.getMealType()) {
                        case "breakfast":
                            breakfastTextView.setText(meal.getMealDescription());
                            breakfastMeal = meal;
                            break;
                        case "lunch":
                            lunchTextView.setText(meal.getMealDescription());
                            lunchMeal = meal;
                            break;
                        case "dinner":
                            dinnerTextView.setText(meal.getMealDescription());
                            dinnerMeal = meal;
                            break;
                    }
                }
            }
        });
    }

    private void launchRecipe(DailyMenu meal) {
        if (meal == null) return;
        Intent intent = new Intent(requireActivity(), RecipeActivity.class);
        intent.putExtra(RecipeActivity.EXTRA_DAILY_MENU_ID, meal.getId());
        intent.putExtra(RecipeActivity.EXTRA_MEAL_DESCRIPTION, meal.getMealDescription());
        startActivity(intent);
    }

    private void showMealOptionsDialog(String mealType, String dateStr) {
        String[] options = {"Type a new meal", "Search past meals", "Clear meal"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Edit " + mealType)
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            launchCreateRecipe(mealType, dateStr);
                            break;
                        case 1:
                            launchMealSearch(mealType, dateStr);
                            break;
                        case 2:
                            clearMeal(mealType);
                            break;
                    }
                })
                .show();
    }

    private void launchCreateRecipe(String mealType, String dateStr) {
        Intent intent = new Intent(requireActivity(), CreateRecipeActivity.class);
        intent.putExtra(CreateRecipeActivity.EXTRA_MEAL_TYPE, mealType);
        intent.putExtra(CreateRecipeActivity.EXTRA_DATE, dateStr);
        DailyMenu existing = getMealForType(mealType);
        if (existing != null) {
            intent.putExtra(CreateRecipeActivity.EXTRA_MEAL_DESCRIPTION,
                    existing.getMealDescription());
        }
        startActivity(intent);
    }

    private void launchMealSearch(String mealType, String dateStr) {
        Intent intent = new Intent(requireActivity(), MealSearchActivity.class);
        intent.putExtra(MealSearchActivity.EXTRA_MEAL_TYPE, mealType);
        intent.putExtra(MealSearchActivity.EXTRA_DATE, dateStr);
        startActivity(intent);
    }

    private void clearMeal(String mealType) {
        DailyMenu meal = getMealForType(mealType);
        if (meal != null) {
            GroceryListDatabase db = GroceryListDatabase.getDatabase(requireActivity().getApplication());
            GroceryListDatabase.databaseWriteExecutor.execute(() -> db.dailyMenuDao().delete(meal));
        }
    }

    private DailyMenu getMealForType(String mealType) {
        switch (mealType) {
            case "breakfast": return breakfastMeal;
            case "lunch": return lunchMeal;
            case "dinner": return dinnerMeal;
            default: return null;
        }
    }
}
