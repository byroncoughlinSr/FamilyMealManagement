package org.coughlin.grocerylist;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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
    private GroceryListViewModel groceryListViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocerylist);
        // Initialize variables
        mGroceryListView = findViewById(R.id.grocerylistView);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mGroceryListView = findViewById(R.id.grocerylistView);
        mGroceryListView.setLayoutManager(new LinearLayoutManager(this));
        groceryListViewModel = new ViewModelProvider(this).get(GroceryListViewModel.class);
        RecyclerView mDrawerListView = findViewById(R.id.left_drawer);
        mDrawerListView.setLayoutManager(new LinearLayoutManager(this));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        handleIntent(getIntent());
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
        groceryListViewModel.getSelectedProductsLive().observe(this, products -> {
            if (products != null) {
                mProductAdapter.updateProducts(products); // Update adapter data
                mProductAdapter.notifyDataSetChanged(); // Notify changes
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
        // Get the SearchView and set the searchable configuration.
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        // Assumes current activity is the searchable activity.
        assert searchView != null;
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Don't iconify the widget. Expand it by default.
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
    private void handleIntent(Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_SEARCH.equals(action)) {
            // Handle search query
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (query != null) {
                groceryListViewModel.filterProducts(query); // Pass query to ViewModel
            }
        } else if (Intent.ACTION_VIEW.equals(action)) {
            // Handle item selection from search suggestions
            Uri data = intent.getData();
            if (data != null) {
                String productId = data.getLastPathSegment(); // Extract product ID
                groceryListViewModel.selectProductById(Integer.parseInt(productId)); // Select product
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }
}



