package org.coughlin.grocerylist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DailyMenuFragment extends Fragment {
    private static final String ARG_DATE = "arg_date";
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
    private static final DateTimeFormatter DB_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

        dateTextView.setText(date.format(DISPLAY_FORMATTER));

        GroceryListDatabase db = GroceryListDatabase.getDatabase(
                requireActivity().getApplication());
        db.dailyMenuDao().getMealsForDate(dateStr).observe(getViewLifecycleOwner(), meals -> {
            breakfastTextView.setText("--");
            lunchTextView.setText("--");
            dinnerTextView.setText("--");

            if (meals != null) {
                for (DailyMenu meal : meals) {
                    switch (meal.getMealType()) {
                        case "breakfast":
                            breakfastTextView.setText(meal.getMealDescription());
                            break;
                        case "lunch":
                            lunchTextView.setText(meal.getMealDescription());
                            break;
                        case "dinner":
                            dinnerTextView.setText(meal.getMealDescription());
                            break;
                    }
                }
            }
        });
    }
}
