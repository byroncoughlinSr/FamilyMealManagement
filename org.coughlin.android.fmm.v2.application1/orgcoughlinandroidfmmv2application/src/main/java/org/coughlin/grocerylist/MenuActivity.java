package org.coughlin.grocerylist;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

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
    private ViewPager2 viewPager;
    private DailyMenuPagerAdapter pagerAdapter;
    private static final DateTimeFormatter DB_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String[] CALENDAR_PERMISSIONS = {
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
    };

    // True when the permission request came from "Add Week", false for the one-time
    // auto-sync prompt shown on first launch.
    private boolean shareWeekAfterPermission;

    private final ActivityResultLauncher<String[]> calendarPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    results -> {
                        if (!hasCalendarPermission()) {
                            if (shareWeekAfterPermission) {
                                Toast.makeText(this, R.string.calendar_permission_denied,
                                        Toast.LENGTH_LONG).show();
                            }
                        } else if (shareWeekAfterPermission) {
                            chooseCalendarAndShareWeek();
                        } else {
                            autoSyncUpcomingMeals();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_menu);

        DailyMenuViewModel viewModel =
                new ViewModelProvider(this).get(DailyMenuViewModel.class);

        viewPager = findViewById(R.id.viewPagerDailyMenu);
        pagerAdapter = new DailyMenuPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(pagerAdapter.getCenterPosition(), false);

        generatingOverlay = findViewById(R.id.generatingOverlay);

        setupDrawer();

        MenuGenerationScheduler.scheduleWeeklyMenuGeneration(this);
        askForCalendarAccessOnce();
    }

    /**
     * Auto-sync runs in the background worker, which can't prompt, so ask for calendar
     * access once up front. After that every generated menu lands on the calendar.
     */
    private void askForCalendarAccessOnce() {
        if (!AppSettings.isCalendarAutoSync(this) || hasCalendarPermission()
                || AppSettings.wasCalendarPermissionAsked(this)) {
            return;
        }
        AppSettings.setCalendarPermissionAsked(this);
        shareWeekAfterPermission = false;
        calendarPermissionLauncher.launch(CALENDAR_PERMISSIONS);
    }

    /** Adds menus that were generated before calendar access was granted. */
    private void autoSyncUpcomingMeals() {
        LocalDate startDate = MenuGenerationWorker.currentWeekStart();
        LocalDate endDate = startDate.plusDays(13);
        GroceryListDatabase.databaseWriteExecutor.execute(() -> {
            int written = MealCalendarHelper.autoSyncRange(this, startDate, endDate);
            if (written > 0) {
                runOnUiThread(() -> Toast.makeText(this,
                        "Added " + written + " meals to Google Calendar",
                        Toast.LENGTH_SHORT).show());
            }
        });
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
        menu.findItem(R.id.action_calendar_auto_sync)
                .setChecked(AppSettings.isCalendarAutoSync(this));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_generate_menu) {
            showGenerateMenuConfirmation();
            return true;
        }
        if (item.getItemId() == R.id.action_regenerate_week) {
            showRegenerateWeekConfirmation();
            return true;
        }
        if (item.getItemId() == R.id.action_clear_next_week) {
            showClearNextWeekConfirmation();
            return true;
        }
        if (item.getItemId() == R.id.action_share_week_to_calendar) {
            if (hasCalendarPermission()) {
                chooseCalendarAndShareWeek();
            } else {
                shareWeekAfterPermission = true;
                calendarPermissionLauncher.launch(CALENDAR_PERMISSIONS);
            }
            return true;
        }
        if (item.getItemId() == R.id.action_calendar_auto_sync) {
            boolean enabled = !item.isChecked();
            item.setChecked(enabled);
            AppSettings.setCalendarAutoSync(this, enabled);
            if (enabled) {
                if (hasCalendarPermission()) {
                    autoSyncUpcomingMeals();
                } else {
                    shareWeekAfterPermission = false;
                    calendarPermissionLauncher.launch(CALENDAR_PERMISSIONS);
                }
            }
            return true;
        }
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (drawerHandler.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showGenerateMenuConfirmation() {
        if (!AppSettings.isProviderConfigured(this)) {
            Toast.makeText(this, R.string.ai_not_configured, Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, SettingsActivity.class));
            return;
        }
        boolean isMonthly = false;
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

        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        String message = "This will generate new menu items from "
                + startDate.format(displayFormatter) + " to "
                + endDate.format(displayFormatter) + ". Continue?";

        new AlertDialog.Builder(this)
                .setTitle("Generate Menu")
                .setMessage(message)
                .setPositiveButton("Continue", (dialog, which) ->
                        executeMenuGeneration(startDate, endDate))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showRegenerateWeekConfirmation() {
        if (!AppSettings.isProviderConfigured(this)) {
            Toast.makeText(this, R.string.ai_not_configured, Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, SettingsActivity.class));
            return;
        }

        LocalDate startDate = MenuGenerationWorker.currentWeekStart();
        LocalDate endDate = startDate.plusDays(6);

        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        String message = "This will replace the generated meals from "
                + startDate.format(displayFormatter) + " to "
                + endDate.format(displayFormatter)
                + ", including days already past. Meals you set yourself are kept. Continue?";

        new AlertDialog.Builder(this)
                .setTitle("Regenerate This Week")
                .setMessage(message)
                .setPositiveButton("Regenerate", (dialog, which) ->
                        executeMenuGeneration(startDate, endDate))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showClearNextWeekConfirmation() {
        LocalDate startDate = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDate endDate = startDate.plusDays(6);

        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        String message = "This will clear all menu items from "
                + startDate.format(displayFormatter) + " to "
                + endDate.format(displayFormatter) + ". Continue?";

        new AlertDialog.Builder(this)
                .setTitle("Clear Next Week's Menu")
                .setMessage(message)
                .setPositiveButton("Clear", (dialog, which) ->
                        executeClearNextWeek(startDate, endDate))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void executeClearNextWeek(LocalDate startDate, LocalDate endDate) {
        GroceryListDatabase db = GroceryListDatabase.getDatabase(getApplication());
        GroceryListDatabase.databaseWriteExecutor.execute(() -> {
            db.dailyMenuDao().deleteMealsForDateRange(
                    startDate.format(DB_FORMATTER),
                    endDate.format(DB_FORMATTER));
            // With the week empty this just removes its meal events from the calendar.
            MealCalendarHelper.autoSyncRange(getApplicationContext(), startDate, endDate);
        });
        Toast.makeText(this, "Next week's menu cleared", Toast.LENGTH_SHORT).show();
    }

    private boolean hasCalendarPermission() {
        return MealCalendarHelper.hasCalendarPermission(this);
    }

    private void chooseCalendarAndShareWeek() {
        // Share the Monday–Sunday week of the day currently on screen.
        LocalDate startDate = pagerAdapter.getDateForPosition(viewPager.getCurrentItem())
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endDate = startDate.plusDays(6);

        GroceryListDatabase.databaseWriteExecutor.execute(() -> {
            List<MealCalendarHelper.CalendarChoice> calendars =
                    MealCalendarHelper.getWritableGoogleCalendars(this);
            runOnUiThread(() -> {
                if (isFinishing()) return;
                if (calendars.isEmpty()) {
                    Toast.makeText(this, R.string.calendar_no_google_calendars,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                String[] labels = new String[calendars.size()];
                for (int i = 0; i < calendars.size(); i++) {
                    MealCalendarHelper.CalendarChoice choice = calendars.get(i);
                    labels[i] = choice.name.equals(choice.account)
                            ? choice.name
                            : choice.name + " (" + choice.account + ")";
                }
                DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("MMM d");
                new AlertDialog.Builder(this)
                        .setTitle("Add " + startDate.format(displayFormatter) + " – "
                                + endDate.format(displayFormatter) + " to")
                        .setItems(labels, (dialog, which) -> {
                            // Remember the pick so automatic syncs use the same calendar.
                            AppSettings.setCalendarId(this, calendars.get(which).id);
                            executeShareWeek(calendars.get(which).id, startDate, endDate);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        });
    }

    private void executeShareWeek(long calendarId, LocalDate startDate, LocalDate endDate) {
        GroceryListDatabase db = GroceryListDatabase.getDatabase(getApplication());
        GroceryListDatabase.databaseWriteExecutor.execute(() -> {
            List<DailyMenu> meals = db.dailyMenuDao().getMealsInRangeSync(
                    startDate.format(DB_FORMATTER), endDate.format(DB_FORMATTER));
            String message;
            if (meals.isEmpty()) {
                message = "No meals planned for that week";
            } else {
                try {
                    int written = MealCalendarHelper.syncMealsToCalendar(
                            this, calendarId, startDate, endDate, meals);
                    message = "Added " + written + " meals to Google Calendar";
                } catch (SecurityException e) {
                    message = getString(R.string.calendar_permission_denied);
                }
            }
            String toast = message;
            runOnUiThread(() -> Toast.makeText(this, toast, Toast.LENGTH_SHORT).show());
        });
    }

    private void executeMenuGeneration(LocalDate startDate, LocalDate endDate) {
        Data inputData = new Data.Builder()
                .putString(MenuGenerationWorker.KEY_START_DATE,
                        startDate.format(DB_FORMATTER))
                .putString(MenuGenerationWorker.KEY_END_DATE,
                        endDate.format(DB_FORMATTER))
                .build();

        OneTimeWorkRequest menuRequest = new OneTimeWorkRequest.Builder(
                MenuGenerationWorker.class)
                .setInputData(inputData)
                .build();
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
                            // MenuGenerationWorker already chains RecipeGenerationWorker as
                            // unique work for this range — don't enqueue a second chain.
                        } else {
                            String reason = workInfo.getOutputData().getString(
                                    MenuGenerationWorker.KEY_ERROR_MESSAGE);
                            String msg = reason != null && !reason.isEmpty()
                                    ? "Menu generation failed: " + reason
                                    : "Menu generation failed";
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

}
