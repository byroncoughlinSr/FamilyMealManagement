package org.coughlin.grocerylist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

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

public class GrocerylistActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    DatabaseHelper dbHelper;
    private ListView mDrawerListView;
    private ListView mGroceryListView;
    private ActionBarDrawerToggle mDrawerToggle;
    private StableArrayAdapter mAdapter;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private GroceryListViewModel groceryListViewModel;
    private String mSelections;
    private MenuItem searchItem;
    private VelocityTracker mVelocityTracker;
    private BackgroundContainer mBackgroundContainer;
    boolean mSwiping = false;
    boolean mItemPressed = false;
    HashMap<Long, Integer> mItemIdTopMap = new HashMap<>();
    private static final int SWIPE_DURATION = 250;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTitle = getTitle();
        setContentView(R.layout.activity_grocerylist);

        // Handle intent entries
        handleIntent(getIntent());

        // Initialize main view objects
        mBackgroundContainer = findViewById(R.id.view1);
        mGroceryListView = findViewById(R.id.grocerylistview);
        mGroceryListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // Setup and create navigation drawer
        mDrawerTitle = "Navigational Drawer";
        String[] mDrawerContents = getResources().getStringArray(R.array.drawer_titles);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerListView = findViewById(R.id.left_drawer);

        // Create Adapter and set to DrawerListView
        mDrawerListView.setAdapter(new ArrayAdapter<>(this, R.layout.item_drawer, mDrawerContents));

        // Set the click listener for item in the drawer
        mDrawerListView.setOnItemClickListener(this::onItemClick);

        // Connect to database
        dbHelper = new DatabaseHelper(getApplicationContext());
        try {
            dbHelper.createDatabase(getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set home icon as up to home display and set home button as enabled
        if(getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }
        // Handle the drawer icon on the actionbar
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {
            /** onDrawerClosed()
             * Called when a drawer has settled in a completely closed state.
             */
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }

            /** onDrawerOpened()
             * Called when a drawer has settled in a completely open state.
             */
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        // Initialize ViewModel
        groceryListViewModel = new ViewModelProvider((ViewModelStoreOwner) this).get(GroceryListViewModel.class);


        // Initialize Adapter with an empty list
        mAdapter = new StableArrayAdapter(this, R.layout.grocerylist_item, new ArrayList<>(), mTouchListener);
        mGroceryListView.setAdapter(mAdapter);

        // Observe the LiveData from ViewModel
        groceryListViewModel.getAllProducts().observe(this, products -> {
            mAdapter.setProducts(products); // Update the adapter's list of products
        });

    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    /**
     * handleIntent()
     * Handles search intent
     */
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            String id = data.getLastPathSegment();
            addToList(id);
        }
    }

    /**
     * onCreateOptionsMenu()
     * Inflates menu and then associates searchable configuration with SearchView
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.grocerylist, menu);

        searchItem = menu.findItem(R.id.search);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    /**
     * Handle touch events to fade/move dragged items as they are swiped out
     */
    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        float mDownX;
        private int mSwipeSlop = -1;

        /**
         * onTouch()
         * Handles the swiping of an item to delete from list
         *
         * @return true or false
         */
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
                        // Multi-item swipes not handled
                        return false;
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
                        // Existing swipe behavior logic
                        handleSwipeAnimation(v, event);
                    } else {
                        // Toggle the checked state and handle item check in the database
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
                // Toggle the checked state
                product.setChecked(!product.isChecked());
                // Update the product in the database
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
                            mSwiping = false;
                            mGroceryListView.setEnabled(true);
                        }
                    });
        }
    };

    /**
     * onPostCreate()
     * Sync the toggle state after onRestoreInstanceState has occurred.
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    /**
     * getGroceryList()
     * Retrieves and displays the list of products.
     */
    private void getGroceryList() {
        // This method is no longer needed as we're using ViewModel and LiveData
        // You can remove or repurpose it as needed
    }

    /**
     * onOptionsItemSelected()
     * Handles action bar item clicks.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * addToList()
     * Adds an item selected in search to the grocery list.
     *
     * @param id ID of item to be added to grocery list
     */
    public void addToList(String id) {
        groceryListViewModel.addToList(id);
        searchItem.collapseActionView();
    }

    /**
     * removeFromList()
     * Called when removing an item from the list.
     *
     * @param position Position of the item to be removed
     */
    private void removeFromList(int position) {
        List<Product> products = groceryListViewModel.getAllProducts().getValue();
        if (products != null && position < products.size()) {
            Product product = products.get(position);
            product.setSelected(false);
            groceryListViewModel.update(product);
            //moveToHistory(String.valueOf(product.getId()));
        }
    }

    /**
     * animateRemoval()
     * Animates the removal of an item from the list.
     *
     * @param listview      The ListView where the item is to be removed
     * @param viewToRemove  The view to be removed
     */
    private void animateRemoval(final ListView listview, View viewToRemove) {
        int firstVisiblePosition = listview.getFirstVisiblePosition();
        for (int i = 0; i < listview.getChildCount(); ++i) {
            View child = listview.getChildAt(i);
            if (child != viewToRemove) {
                int position = firstVisiblePosition + i;
                long itemId = mAdapter.getItemId(position);
                mItemIdTopMap.put(itemId, child.getTop());
            }
        }
        // Delete the item from the adapter
        int position = mGroceryListView.getPositionForView(viewToRemove);
        removeFromList(position);
    }

    /**
     * moveToHistory()
     * Moves an item swiped to history.
     *
     * @param id ID of item being swiped
     */
    /**
    rivate void moveToHistory(String id) {
        mDatabaseAdapter.open();
        mDatabaseAdapter.moveToHistory(id);
        mDatabaseAdapter.close();
    }
     **/

    /**
     * onItemClick()
     * Handles items clicked in the left-drawer menu.
     *
     * @param parent   View's parent
     * @param view     Left drawer view
     * @param position Item's clicked position
     * @param id       ID of menu item
     */
    private void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Go to activity of item clicked
        Intent intent;
        switch (position) {
            case 1:
                intent = new Intent(GrocerylistActivity.this, ProductActivity.class);
                startActivity(intent);
                break;
            case 2:
                intent = new Intent(GrocerylistActivity.this, HistoryActivity.class);
                startActivity(intent);
                break;
            case 3:
                intent = new Intent(GrocerylistActivity.this, MenuActivity.class);
                startActivity(intent);
                break;
            case 4:
                intent = new Intent(GrocerylistActivity.this, MenuItemActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }

        // Close drawer
        mDrawerLayout.closeDrawer(mDrawerListView);
    }
}
