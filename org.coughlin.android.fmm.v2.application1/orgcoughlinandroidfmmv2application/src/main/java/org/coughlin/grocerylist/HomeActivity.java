package org.coughlin.grocerylist;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private DrawerHandler drawerHandler;
    private DailyMenuViewModel dailyMenuViewModel;
    private static final DateTimeFormatter DB_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AppSettings.isProviderConfigured(this)) {
            startActivity(new Intent(this, ProviderSetupActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_home);

        setupDrawer();

        // Date display
        TextView dateText = findViewById(R.id.textViewDate);
        dateText.setText(LocalDate.now().format(DISPLAY_FORMATTER));

        // ViewModels
        dailyMenuViewModel = new ViewModelProvider(this).get(DailyMenuViewModel.class);
        GroceryListViewModel groceryViewModel =
                new ViewModelProvider(this).get(GroceryListViewModel.class);

        // Today's meals
        TextView breakfastText = findViewById(R.id.textViewBreakfast);
        TextView lunchText = findViewById(R.id.textViewLunch);
        TextView dinnerText = findViewById(R.id.textViewDinner);

        dailyMenuViewModel.getMealsForCurrentDate().observe(this, meals -> {
            breakfastText.setText("--");
            lunchText.setText("--");
            dinnerText.setText("--");

            if (meals != null) {
                for (DailyMenu meal : meals) {
                    switch (meal.getMealType()) {
                        case "breakfast":
                            breakfastText.setText(meal.getMealDescription());
                            break;
                        case "lunch":
                            lunchText.setText(meal.getMealDescription());
                            break;
                        case "dinner":
                            dinnerText.setText(meal.getMealDescription());
                            break;
                    }
                }
            }
        });

        // Grocery count
        TextView groceryCount = findViewById(R.id.textViewGroceryCount);
        groceryViewModel.getSelectedProducts().observe(this, products -> {
            int count = products != null ? products.size() : 0;
            groceryCount.setText(getString(R.string.home_items_format, count));
        });

        // Card click listeners
        CardView cardTodaysMeals = findViewById(R.id.cardTodaysMeals);
        CardView cardWeeklyMenu = findViewById(R.id.cardWeeklyMenu);
        CardView cardGroceryList = findViewById(R.id.cardGroceryList);
        CardView cardRecipes = findViewById(R.id.cardRecipes);
        CardView cardSettings = findViewById(R.id.cardSettings);

        cardTodaysMeals.setOnClickListener(v ->
                startActivity(new Intent(this, MenuActivity.class)));
        cardWeeklyMenu.setOnClickListener(v ->
                startActivity(new Intent(this, MenuActivity.class)));
        cardGroceryList.setOnClickListener(v ->
                startActivity(new Intent(this, GrocerylistActivity.class)));
        cardRecipes.setOnClickListener(v ->
                startActivity(new Intent(this, CreateRecipeActivity.class)));
        cardSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        dailyMenuViewModel.setDate(LocalDate.now().format(DB_FORMATTER));
        TextView dateText = findViewById(R.id.textViewDate);
        dateText.setText(LocalDate.now().format(DISPLAY_FORMATTER));
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
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (drawerHandler.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerHandler.syncState();
    }
}
