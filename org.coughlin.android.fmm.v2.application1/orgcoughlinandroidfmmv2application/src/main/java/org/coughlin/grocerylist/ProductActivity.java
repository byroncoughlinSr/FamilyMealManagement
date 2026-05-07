package org.coughlin.grocerylist;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ProductActivity extends AppCompatActivity {
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mTitle;
    private EditText text;
    private String product;
    DrawerHandler mDrawerHandler;

    private ProductViewModel productViewModel;
    private ProductSuggestionAdapter suggestionAdapter;
    private final List<Product> suggestionResults = new ArrayList<>();
    private RecyclerView suggestionsRecyclerView;
    private boolean isSelectingSuggestion = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        //Setup and create navigation drawer
        RecyclerView mDrawerListView = findViewById(R.id.left_drawer);
        CharSequence mDrawerTitle = "Navigation Drawer";
        Toolbar toolbar = findViewById(R.id.toolbar);
        String[] drawerTitles = getResources().getStringArray(R.array.drawer_titles);
        List<String> drawerContents = Arrays.asList(drawerTitles);
        mDrawerHandler = new DrawerHandler(
                this,
                mDrawerLayout,
                mDrawerListView,
                toolbar,
                drawerContents,
                mDrawerTitle,
                mTitle
        );

        text = findViewById(R.id.newProductTxt);
        Button button = findViewById(R.id.addProductBtn);

        // Setup suggestions RecyclerView
        suggestionsRecyclerView = findViewById(R.id.suggestionsRecyclerView);
        suggestionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        suggestionAdapter = new ProductSuggestionAdapter(
                suggestionResults,
                this::onSuggestionSelected,
                this::onSuggestionLongClick
        );
        suggestionsRecyclerView.setAdapter(suggestionAdapter);

        // Add text watcher for autocomplete
        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isSelectingSuggestion) return;
                String query = s.toString().trim();
                if (query.length() >= 2) {
                    performSearch(query);
                } else {
                    suggestionResults.clear();
                    suggestionAdapter.notifyDataSetChanged();
                    suggestionsRecyclerView.setVisibility(View.GONE);
                }
            }
        });

        //Handle button
        button.setOnClickListener(v -> {
            product = text.getText().toString();
            text.setText("");
            suggestionsRecyclerView.setVisibility(View.GONE);
            productViewModel.insertNewProduct(product);
        });
    }

    private void performSearch(String query) {
        GroceryListDatabase.databaseWriteExecutor.execute(() -> {
            List<Product> results = productViewModel.searchProductsByName("%" + query + "%");
            runOnUiThread(() -> {
                suggestionResults.clear();
                if (results != null) {
                    suggestionResults.addAll(results);
                }
                suggestionAdapter.notifyDataSetChanged();
                suggestionsRecyclerView.setVisibility(
                        suggestionResults.isEmpty() ? View.GONE : View.VISIBLE);
            });
        });
    }

    private void onSuggestionSelected(Product product) {
        isSelectingSuggestion = true;
        text.setText(product.getName());
        text.setSelection(product.getName().length());
        suggestionsRecyclerView.setVisibility(View.GONE);
        isSelectingSuggestion = false;
    }

    private void onSuggestionLongClick(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product?")
                .setMessage("Delete \"" + product.getName() + "\" from the database?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    productViewModel.deleteProduct(product);
                    String currentQuery = text.getText().toString().trim();
                    if (currentQuery.length() >= 2) {
                        performSearch(currentQuery);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        Objects.requireNonNull(getActionBar()).setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.product, menu);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerHandler.syncState();
    }
}


	
