package org.coughlin.grocerylist;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GrocerylistActivity extends AppCompatActivity {
    private DrawerHandler mDrawerHandler;
    private ProductAdapter mProductAdapter;
    private GroceryListViewModel groceryListViewModel;
    private ItemTouchHelper itemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocerylist);
        // Initialize variables
        GroceryListDatabase dbHelper = GroceryListDatabase.getDatabase(getApplicationContext());
        ProductDao productDao = dbHelper.productDao();
        RecyclerView mGroceryListView = findViewById(R.id.grocerylistView);
        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        mGroceryListView.setLayoutManager(new LinearLayoutManager(this));
        groceryListViewModel = new ViewModelProvider(this).get(GroceryListViewModel.class);
        RecyclerView mDrawerListView = findViewById(R.id.left_drawer);
        HistoryViewModel historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        MutableLiveData<List<Product>> selectedProductsLive = groceryListViewModel.getSelectedProductLive();
        CharSequence mDrawerTitle = "Navigational Drawer";
        Toolbar toolbar = findViewById(R.id.toolbar);
        String[] drawerTitles = getResources().getStringArray(R.array.drawer_titles);
        List<String> drawerContents = Arrays.asList(drawerTitles);
        CharSequence mTitle = getTitle();
        mDrawerHandler = new DrawerHandler(
                this,
                mDrawerLayout,
                mDrawerListView,
                toolbar,
                drawerContents,
                mDrawerTitle,
                mTitle
        );
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        // Add the toggle to the DrawerLayout
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        // Sync the toggle state
        mDrawerToggle.syncState();
        groceryListViewModel.getSelectedProducts().observe(this, products -> {
            if (products != null && !products.isEmpty()) {
                if (mProductAdapter == null) {
                    mProductAdapter = new ProductAdapter(products, groceryListViewModel);
                    mGroceryListView.setAdapter(mProductAdapter);
                     itemTouchHelper = new ItemTouchHelper(new GroceryItemTouchHelperCallback(
                            mProductAdapter,
                            productDao,
                            historyViewModel,
                            selectedProductsLive
                    ));
                    itemTouchHelper.attachToRecyclerView(mGroceryListView);
                } else {
                    mProductAdapter.updateProducts(products);
                }
            } else {
                if (mProductAdapter != null) {
                    mProductAdapter.updateProducts(new ArrayList<>()); // Pass an empty list
                }
            }
        });

        groceryListViewModel.getSelectedProductLive().observe(this, product -> {
            if (product != null && mProductAdapter != null) {
                mProductAdapter.updateProducts(product);
            }
        });
        handleIntent(getIntent());
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerHandler.syncState();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.grocerylist, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        assert searchView != null;
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share_grocery_list) {
            shareShoppingList();
            return true;
        }
        if (item.getItemId() == R.id.action_clear_grocery_list) {
            new AlertDialog.Builder(this)
                    .setTitle("Clear Grocery List")
                    .setMessage("Remove all items from the grocery list?")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        groceryListViewModel.clearGroceryList();
                        Toast.makeText(this, "Grocery list cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * Shares the items still to buy as a plain-text shopping list through the
     * Android share sheet (the same chooser the camera uses to share a photo).
     */
    private void shareShoppingList() {
        groceryListViewModel.loadItemsToBuy(items -> {
            if (isFinishing() || isDestroyed()) return;
            if (items.isEmpty()) {
                Toast.makeText(this, R.string.share_list_nothing_to_buy, Toast.LENGTH_SHORT).show();
                return;
            }
            java.util.Date now = new java.util.Date();
            java.util.Locale locale = java.util.Locale.getDefault();
            String title = ShoppingListFormatter.title(now, locale);

            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_TEXT, ShoppingListFormatter.format(items, now, locale));
            send.putExtra(Intent.EXTRA_SUBJECT, title);   // email apps use this as the subject

            startActivity(Intent.createChooser(send, getString(R.string.share_list_chooser_title)));
        });
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_SEARCH.equals(action)) {
            // Handle search query
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (query != null) {
                groceryListViewModel.filterProducts( query);
            }
        } else if (Intent.ACTION_VIEW.equals(action)) {
            // Handle item selection from search suggestions
            Uri data = intent.getData();
            if (data != null) {
                int productId = Integer.parseInt(data.getLastPathSegment());
                groceryListViewModel.selectProductById(productId);
            }
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }
}



