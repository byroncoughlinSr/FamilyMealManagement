package org.coughlin.grocerylist;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GrocerylistActivity extends AppCompatActivity {
    private RecyclerView mGroceryListView;
    private DrawerHandler mDrawerHandler;
    private ProductAdapter mProductAdapter;
    private GroceryListViewModel groceryListViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocerylist);
        // Initialize variables
        mGroceryListView = findViewById(R.id.grocerylistView);
        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        mGroceryListView = findViewById(R.id.grocerylistView);
        mGroceryListView.setLayoutManager(new LinearLayoutManager(this));
        groceryListViewModel = new ViewModelProvider(this).get(GroceryListViewModel.class);
        RecyclerView mDrawerListView = findViewById(R.id.left_drawer);
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

        ItemTouchHelper itemTouchHelper = groceryListViewModel.getItemTouchHelper();
        itemTouchHelper.attachToRecyclerView(mGroceryListView);

        groceryListViewModel.getSelectedProductsLive().observe(this, productNames -> {
            if (productNames != null) {
                mProductAdapter = new ProductAdapter(productNames, groceryListViewModel);
                mGroceryListView.setAdapter(mProductAdapter);
            }
        });
        groceryListViewModel.getSelectedProductsLive().observe(this, products -> {
            if (products == null) {
                mProductAdapter.updateProducts(new ArrayList<>()); // Pass an empty list
            } else {
                mProductAdapter = new ProductAdapter(products, groceryListViewModel);
                mProductAdapter.updateProducts(products);
            }
        });
        groceryListViewModel.getSelectedProductLive().observe(this, product -> {
            if(product != null) {
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
    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_SEARCH.equals(action)) {
            // Handle search query
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (query != null) {
                groceryListViewModel.filterProducts(query);
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



