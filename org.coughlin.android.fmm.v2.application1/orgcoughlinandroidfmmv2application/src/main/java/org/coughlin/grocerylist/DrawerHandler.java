package org.coughlin.grocerylist;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

public class DrawerHandler {
    private final DrawerLayout mDrawerLayout;
    private final ActionBarDrawerToggle mDrawerToggle;
    private final RecyclerView mDrawerListView;
    private final CharSequence mDrawerTitle;
    private final AppCompatActivity mActivity;

    public DrawerHandler(@NonNull AppCompatActivity activity,
                         @NonNull DrawerLayout drawerLayout,
                         @NonNull RecyclerView drawerListView,
                         @NonNull Toolbar toolbar,
                         @NonNull List<String> drawerContents,
                         @NonNull CharSequence drawerTitle,
                         @NonNull CharSequence activityTitle) {
        this.mActivity = activity;
        this.mDrawerLayout = drawerLayout;
        this.mDrawerListView = drawerListView;
        this.mDrawerTitle = drawerTitle;

        // Set up toolbar
        activity.setSupportActionBar(toolbar);
        Objects.requireNonNull(activity.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(activity.getSupportActionBar()).setHomeButtonEnabled(true);

        // Set up drawer list
        mDrawerListView.setLayoutManager(new LinearLayoutManager(activity));
        DrawerAdapter drawerAdapter = new DrawerAdapter(drawerContents, this::onDrawerItemClick);
        mDrawerListView.setAdapter(drawerAdapter);


        // Set up drawer toggle
        this.mDrawerToggle = new ActionBarDrawerToggle(
                activity,
                drawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                Objects.requireNonNull(activity.getSupportActionBar()).setTitle(activityTitle);
                activity.invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Objects.requireNonNull(activity.getSupportActionBar()).setTitle(drawerTitle);
                activity.invalidateOptionsMenu();
            }
        };
        drawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }
    public void syncState() {
        mDrawerToggle.syncState();
    }

    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item);
    }

    private void onDrawerItemClick(int position) {
        Intent intent = null;

        // Navigate to the corresponding activity based on position
        switch (position) {
            case 0:
                intent = new Intent(mActivity, HomeActivity.class);
                break;
            case 1:
                intent = new Intent(mActivity, GrocerylistActivity.class);
                break;
            case 2:
                intent = new Intent(mActivity, ProductActivity.class);
                break;
            case 3:
                intent = new Intent(mActivity, HistoryActivity.class);
                break;
            case 4:
                intent = new Intent(mActivity, MenuActivity.class);
                break;
            case 5:
                intent = new Intent(mActivity, MenuItemActivity.class);
                break;
            case 6:
                intent = new Intent(mActivity, HelpActivity.class);
                break;
            default:
                Log.d("DrawerHandler", "Unhandled drawer position: " + position);
                break;
        }

        // Start the activity if the intent is not null
        if (intent != null) {
            mActivity.startActivity(intent);
        }

        // Close the drawer after an item is clicked
        mDrawerLayout.closeDrawers();
    }

}

