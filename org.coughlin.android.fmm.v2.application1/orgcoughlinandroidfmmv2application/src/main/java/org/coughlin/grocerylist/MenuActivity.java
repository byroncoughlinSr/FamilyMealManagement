package org.coughlin.grocerylist;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;

public class MenuActivity extends AppCompatActivity {
    private DrawerHandler drawerHandler;
    private View generatingOverlay;
    private static final DateTimeFormatter DB_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_menu);

        DailyMenuViewModel viewModel =
                new ViewModelProvider(this).get(DailyMenuViewModel.class);

        ViewPager2 viewPager = findViewById(R.id.viewPagerDailyMenu);
        DailyMenuPagerAdapter pagerAdapter = new DailyMenuPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(pagerAdapter.getCenterPosition(), false);

        generatingOverlay = findViewById(R.id.generatingOverlay);

        setupDrawer();

        MenuGenerationScheduler.scheduleWeeklyMenuGeneration(this);
    }

    private void setupDrawer() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        RecyclerView drawerRecyclerView = findViewById(R.id.left_drawer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        String[] drawerTitles = getResources().getStringArray(R.array.drawer_titles);
        List<String> drawerContents = Arrays.asList(drawerTitles);
        CharSequence drawerTitle = "Navigation Drawer";
        CharSequence activityTitle = getTitle();

        drawerHandler = new DrawerHandler(
                this,
                drawerLayout,
                drawerRecyclerView,
                toolbar,
                drawerContents,
                drawerTitle,
                activityTitle
        );

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.daily_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_generate_menu) {
            OneTimeWorkRequest menuRequest = new OneTimeWorkRequest.Builder(
                    MenuGenerationWorker.class).build();
            WorkManager workManager = WorkManager.getInstance(this);
            workManager.enqueue(menuRequest);

            generatingOverlay.setVisibility(View.VISIBLE);

            workManager.getWorkInfoByIdLiveData(menuRequest.getId())
                    .observe(this, workInfo -> {
                        if (workInfo != null && workInfo.getState().isFinished()) {
                            generatingOverlay.setVisibility(View.GONE);
                            if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                Toast.makeText(this, "Menu generated! Recipes loading in background...",
                                        Toast.LENGTH_SHORT).show();
                                startRecipeGeneration();
                            } else {
                                Toast.makeText(this, "Menu generation failed",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            return true;
        }
        if (drawerHandler.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startRecipeGeneration() {
        // Compute the same date range the menu worker used
        boolean isMonthly = false; // Must match MenuGenerationWorker toggle
        LocalDate startDate;
        LocalDate endDate;

        if (isMonthly) {
            LocalDate today = LocalDate.now();
            startDate = today.getDayOfMonth() > 1 ? today : today.withDayOfMonth(1);
            endDate = today.with(TemporalAdjusters.lastDayOfMonth());
        } else {
            startDate = LocalDate.now()
                    .with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            endDate = startDate.plusDays(6);
        }

        Data inputData = new Data.Builder()
                .putString(RecipeGenerationWorker.KEY_START_DATE,
                        startDate.format(DB_FORMATTER))
                .putString(RecipeGenerationWorker.KEY_END_DATE,
                        endDate.format(DB_FORMATTER))
                .putInt(RecipeGenerationWorker.KEY_CURRENT_INDEX, 0)
                .build();

        OneTimeWorkRequest recipeRequest = new OneTimeWorkRequest.Builder(
                RecipeGenerationWorker.class)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(this).enqueue(recipeRequest);
    }
}
