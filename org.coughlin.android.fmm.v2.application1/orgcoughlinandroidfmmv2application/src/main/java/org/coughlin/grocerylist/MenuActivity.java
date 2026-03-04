package org.coughlin.grocerylist;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Arrays;
import java.util.List;

public class MenuActivity extends AppCompatActivity {
    private DrawerHandler drawerHandler;

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
            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(
                    MenuGenerationWorker.class).build();
            WorkManager.getInstance(this).enqueue(request);
            Toast.makeText(this, "Generating menu...", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (drawerHandler.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
