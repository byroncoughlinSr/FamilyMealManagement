package org.coughlin.grocerylist;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DailyMenuPagerAdapter extends FragmentStateAdapter {

    private static final DateTimeFormatter DB_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int TOTAL_PAGES = 1000;
    private static final int CENTER_POSITION = 500;
    private final LocalDate anchorDate;

    public DailyMenuPagerAdapter(@NonNull FragmentActivity activity) {
        super(activity);
        this.anchorDate = LocalDate.now();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        int dayOffset = position - CENTER_POSITION;
        LocalDate date = anchorDate.plusDays(dayOffset);
        return DailyMenuFragment.newInstance(date.format(DB_FORMATTER));
    }

    @Override
    public int getItemCount() {
        return TOTAL_PAGES;
    }

    public int getCenterPosition() {
        return CENTER_POSITION;
    }

    public LocalDate getDateForPosition(int position) {
        int dayOffset = position - CENTER_POSITION;
        return anchorDate.plusDays(dayOffset);
    }
}
