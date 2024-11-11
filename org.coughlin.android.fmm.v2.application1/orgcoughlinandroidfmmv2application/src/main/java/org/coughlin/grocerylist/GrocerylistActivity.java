package org.coughlin.grocerylist;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

public class GrocerylistActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private ListView mGroceryListView;
    private ActionBarDrawerToggle mDrawerToggle;
    private StableArrayAdapter mAdapter;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private GroceryListViewModel groceryListViewModel;
    private VelocityTracker mVelocityTracker;
    private BackgroundContainer mBackgroundContainer;
    private boolean mSwiping = false;
    private boolean mItemPressed = false;
    private static final int SWIPE_DURATION = 250;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocerylist);
        groceryListViewModel = new ViewModelProvider(this).get(GroceryListViewModel.class);
        mAdapter = new StableArrayAdapter(this, R.layout.grocerylist_item, new ArrayList<>(), mTouchListener);
        mBackgroundContainer = findViewById(R.id.view1);
        Toolbar toolbar = findViewById(R.id.toolbar);
        mTitle = getTitle();
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerTitle = "Navigational Drawer";
        mGroceryListView = findViewById(R.id.grocerylistview);
        mGroceryListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(mTitle);
        String[] mDrawerContents = getResources().getStringArray(R.array.drawer_titles);

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        setupDrawerToggle();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        ListView mDrawerListView = findViewById(R.id.left_drawer);
        mDrawerListView.setAdapter(new ArrayAdapter<>(this, R.layout.item_drawer, R.id.drawer_item, mDrawerContents));
        mDrawerListView.setOnItemClickListener(this::onItemClick);
        groceryListViewModel = new ViewModelProvider(this).get(GroceryListViewModel.class);

        handleIntent(getIntent());
        mGroceryListView.setAdapter(mAdapter);
        groceryListViewModel.getAllProducts().observe(this, products -> {
            mAdapter.setProducts(products);
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
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.grocerylist, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) searchItem.getActionView();
        if(searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }
        return true;
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                String id = data.getLastPathSegment();
                if (id != null && !id.isEmpty()) {
                    groceryListViewModel.addToList(id);
                }
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        float mDownX;
        private int mSwipeSlop = -1;

        @Override
        public boolean onTouch(final View v, MotionEvent event) {
            CheckedTextView item = v.findViewById(R.id.item);
            int position = mGroceryListView.getPositionForView(v);
            v.performClick();

            if (mSwipeSlop < 0) {
                mSwipeSlop = ViewConfiguration.get(GrocerylistActivity.this).getScaledTouchSlop();
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mItemPressed) {
                        return false; // Multi-item swipes not handled
                    }
                    if (mVelocityTracker == null) {
                        mVelocityTracker = VelocityTracker.obtain();
                    } else {
                        mVelocityTracker.clear();
                    }
                    mVelocityTracker.addMovement(event);
                    mItemPressed = true;
                    mDownX = event.getX();
                    break;

                case MotionEvent.ACTION_CANCEL:
                    v.setAlpha(1);
                    v.setTranslationX(0);
                    mItemPressed = false;
                    break;

                case MotionEvent.ACTION_MOVE:
                    float x = event.getX() + v.getTranslationX();
                    float deltaX = x - mDownX;
                    float deltaXAbs = Math.abs(deltaX);
                    mVelocityTracker.computeCurrentVelocity(1000);
                    if (!mSwiping) {
                        if (deltaXAbs > mSwipeSlop) {
                            mSwiping = true;
                            mGroceryListView.requestDisallowInterceptTouchEvent(true);
                            mBackgroundContainer.showBackground(v.getTop(), v.getHeight());
                        }
                    }
                    if (mSwiping) {
                        v.setTranslationX((x - mDownX));
                        v.setAlpha(1 - deltaXAbs / v.getWidth());
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (mSwiping) {
                        handleSwipeAnimation(v, event);
                    } else {
                        toggleCheckedState(position, item);
                    }
                    mItemPressed = false;
                    break;

                default:
                    return false;
            }
            return true;
        }

        private void toggleCheckedState(int position, CheckedTextView item) {
            List<Product> products = groceryListViewModel.getAllProducts().getValue();
            if (products != null && position < products.size()) {
                Product product = products.get(position);
                product.setChecked(!product.isChecked());
                groceryListViewModel.update(product);
            }
        }
        private void handleSwipeAnimation(View v, MotionEvent event) {
            float x = event.getX() + v.getTranslationX();
            float deltaX = x - mDownX;
            float deltaXAbs = Math.abs(deltaX);
            float fractionCovered;
            float endX;
            float endAlpha;
            final boolean remove;

            if (deltaXAbs > v.getWidth() / 16.0) {
                fractionCovered = deltaXAbs / v.getWidth();
                endX = deltaX < 0 ? -v.getWidth() : v.getWidth();
                endAlpha = 0;
                remove = true;
            } else {
                fractionCovered = 1 - (deltaXAbs / v.getWidth());
                endX = 0;
                endAlpha = 1;
                remove = false;
            }

            long duration = (long) ((1 - fractionCovered) * SWIPE_DURATION);
            mGroceryListView.setEnabled(false);
            v.animate().setDuration(duration)
                    .alpha(endAlpha).translationX(endX)
                    .withEndAction(() -> {
                        v.setAlpha(1);
                        v.setTranslationX(0);
                        if (remove) {
                            animateRemoval(mGroceryListView, v);
                        } else {
                            mBackgroundContainer.hideBackground();
                            mGroceryListView.setEnabled(true);
                        }
                    });
        }

        private void animateRemoval(ListView listView, View v) {
            int position = listView.getPositionForView(v);
            groceryListViewModel.delete(groceryListViewModel.getAllProducts().getValue().get(position));
            mBackgroundContainer.hideBackground();
            listView.setEnabled(true);
        }
    };

    private void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Handle drawer item clicks here
    }
}
