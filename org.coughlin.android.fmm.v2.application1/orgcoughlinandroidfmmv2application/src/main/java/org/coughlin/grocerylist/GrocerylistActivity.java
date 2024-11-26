package org.coughlin.grocerylist;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GrocerylistActivity extends AppCompatActivity {
    private RecyclerView mGroceryListView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private ProductAdapter mProductAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocerylist);
        // Initialize variables
        mGroceryListView = findViewById(R.id.grocerylistView);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mGroceryListView = findViewById(R.id.grocerylistView);
        mGroceryListView.setLayoutManager(new LinearLayoutManager(this));
        GroceryListViewModel groceryListViewModel = new ViewModelProvider(this).get(GroceryListViewModel.class);
        RecyclerView mDrawerListView = findViewById(R.id.left_drawer);
        mDrawerListView.setLayoutManager(new LinearLayoutManager(this));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        );
        // Set up drawer titles
        String[] drawerTitles = getResources().getStringArray(R.array.drawer_titles);
        List<String> drawerContents = Arrays.asList(drawerTitles);
        // Set up adapter and LayoutManager
        DrawerAdapter drawerAdapter = new DrawerAdapter(drawerContents);
        mDrawerListView.setAdapter(drawerAdapter);
        // Set up the toolbar and drawer toggle
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        mDrawerTitle = "Navigational Drawer";
        mTitle = getTitle();
        setupDrawerToggle();
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        // Setup product adapter and ViewModel
        groceryListViewModel.getSelectedProductsLive().observe(this, productNames -> {
            if (productNames != null) {
                mProductAdapter = new ProductAdapter(productNames, groceryListViewModel);
                mGroceryListView.setAdapter(mProductAdapter);
            }
        });
    }
    private void setupDrawerToggle() {
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                Objects.requireNonNull(getSupportActionBar()).setTitle(mTitle);
                invalidateOptionsMenu();
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Objects.requireNonNull(getSupportActionBar()).setTitle(mDrawerTitle);  // Use getSupportActionBar()
                invalidateOptionsMenu();
            }
        };
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.grocerylist, menu);
        return true;
    }
    private void onDrawerItemClick(int position) {
        switch (position) {
            case 0:
                // Example: Open a fragment or activity for the first item
                break;
            case 1:
                // Handle other items
                break;
            // Add more cases if you have more items
            default:
                break;
        }
        // Close the drawer
        mDrawerLayout.closeDrawers();
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }
    @Override
    public  boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            GroceryListViewModel groceryListViewModel = new ViewModelProvider(this).get(GroceryListViewModel.class);
            groceryListViewModel.filterProducts(query); // Custom method in ViewModel to filter products
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                String productId = data.getLastPathSegment();
                // Open detail view or perform action based on the selected suggestion
                //openProductDetail(productId);
            }
        }
    }


}



