package com.byron.familymeal;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.io.IOException;
import java.util.HashMap;

public class FamilyMealActivity extends AppCompatActivity
        implements  LoaderManager.LoaderCallbacks<Cursor> {
    private DrawerLayout mDrawerLayout;
    private NavigationView nvDrawer;
    private String[] mDrawerContents;
    private ListView mDrawerListView;
    private ListView mGroceryListView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle mDrawerToggle;
    private DatabaseAdapter mDatabaseAdapter;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private StableArrayAdapter mAdapter;
    //private SimpleAdapter mAdapter;
    private String[] mProjections = {Grocerylist.Products.ROW_ID, Grocerylist.Products.PRO_NAME};
    private String mSelections;
    private MenuItem searchItem;
    private VelocityTracker mVelocityTracker;
    private BackgroundContainer mBackgroundContainer;
    boolean mSwiping = false;
    boolean mItemPressed = false;
    HashMap<Long, Integer> mItemIdTopMap = new HashMap<Long, Integer>();
    private static final int SWIPE_DURATION = 250;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Setup the database
        mDatabaseAdapter = new DatabaseAdapter(this);


        try {
            mDatabaseAdapter.databaseHelper.createDatabase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

        try {
            mDatabaseAdapter.databaseHelper.openDatabase();
        } catch (SQLiteException sqle) {
            throw sqle;
        }


        mTitle = getTitle();
        setContentView(R.layout.activity_family_meal);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Handle intent entries
        getIntent();
        handleIntent(getIntent());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //Initialize main view objects
        mBackgroundContainer = findViewById(R.id.view1);
        mGroceryListView = findViewById(R.id.grocerylistview);
        mGroceryListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // Set home icon as up to home display and set home button as enabled
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        //Setup and create navigation drawer
        mDrawerTitle = "Navigational Drawer";
        mDrawerContents = getResources().getStringArray(R.array.drawer_titles);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerListView = findViewById(R.id.left_drawer);

       //Create Adapter and set to DrawerListView
        mDrawerListView.setAdapter(new ArrayAdapter<String>(this, R.layout.item_drawer, mDrawerContents));

        // Add a header to the ListView
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.listview_header, mDrawerListView, false);
        mDrawerListView.addHeaderView(header);

        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Navigation!");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the click listener for item in the drawer
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Go to activity of item clicked
                if (position == 1) {
                    Intent intent = new Intent(FamilyMealActivity.this, ProductActivity.class);
                    startActivity(intent);
                } else if (position == 2) {
                    Intent intent = new Intent(FamilyMealActivity.this, HistoryActivity.class);
                    startActivity(intent);
                } else if (position == 3) {
                    Intent intent = new Intent(FamilyMealActivity.this, MenuActivity.class);
                    startActivity(intent);
                } else if (position == 4) {
                    Intent intent = new Intent(FamilyMealActivity.this, MenuItemActivity.class);
                    startActivity(intent);
                }

                //Close drawer
                mDrawerLayout.closeDrawer(mDrawerListView);
            }
        });

        //Get list of products to display on grocerylist
        getGrocerylist();
    }

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    /**
     *
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.family_meal, menu);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    /**
     * handleIntent()
     * Author: byron
     * Description: Handles search intent
     *
     * @param intent
     */
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            String id = data.getLastPathSegment();
            addToList(id);
        }
    }

    /**
     * Handle touch events to fade/move dragged items as they are swiped out
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        float mDownX;
        private int mSwipeSlop = -1;

        /**                onTouch()
         * Author: byron
         * Description: Handles the swiping of an item to delete from list
         * @param v
         * @param event
         * @return true or false
         */
        @Override
        public boolean onTouch(final View v, MotionEvent event) {
            if (mSwipeSlop < 0) {
                mSwipeSlop = ViewConfiguration.get(FamilyMealActivity.this).
                        getScaledTouchSlop();
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mItemPressed) {
                        // Multi-item swipes not handled
                        return false;
                    }
                    if (mVelocityTracker == null) {
                        mVelocityTracker = VelocityTracker.obtain();
                        mVelocityTracker.recycle();
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

                case MotionEvent.ACTION_MOVE: {
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
                }
                break;

                case MotionEvent.ACTION_UP: {
                    // User let go - figure out whether to animate the view out, or back into place
                    if (mSwiping) {
                        float x = event.getX() + v.getTranslationX();
                        float deltaX = x - mDownX;
                        float deltaXAbs = Math.abs(deltaX);
                        float fractionCovered;
                        float endX;
                        float endAlpha;
                        final boolean remove;
                        if (deltaXAbs > v.getWidth() / 16) {
                            // Greater than a quarter of the width - animate it out
                            fractionCovered = deltaXAbs / v.getWidth();
                            endX = deltaX < 0 ? -v.getWidth() : v.getWidth();
                            endAlpha = 0;
                            remove = true;
                        } else {
                            // Not far enough - animate it back
                            fractionCovered = 1 - (deltaXAbs / v.getWidth());
                            endX = 0;
                            endAlpha = 1;
                            remove = false;
                        }
                        // Animate position and alpha of swiped item
                        // NOTE: This is a simplified version of swipe behavior, for the
                        // purposes of this demo about animation. A real version should use
                        // velocity (via the VelocityTracker class) to send the item off or
                        // back at an appropriate speed.
                        long duration = (int) ((1 - fractionCovered) * SWIPE_DURATION);
                        mGroceryListView.setEnabled(false);
                        v.animate().setDuration(duration).
                                alpha(endAlpha).translationX(endX).
                                withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Restore animated values
                                        v.setAlpha(1);
                                        v.setTranslationX(0);
                                        if (remove) {
                                            animateRemoval(mGroceryListView, v);
                                        } else {
                                            mBackgroundContainer.hideBackground();
                                            mSwiping = false;
                                            mGroceryListView.setEnabled(true);
                                        }
                                    }
                                });
                    }
                }
                mItemPressed = false;
                break;
                default:
                    return false;
            }
            return true;
        }
    };

    /**
     * animateRemoval()
     * Author: byron
     * Description: This method animates all other views in the ListView container (not including ignoreView)
     * into their final positions. It is called after ignoreView has been removed from the
     * adapter, but before layout has been run. The approach here is to figure out where
     * everything is now, then allow layout to run, then figure out where everything is after
     * layout, and then to run animations between all of those start/end positions.
     *
     * @param listview
     * @param viewToRemove
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

        getLoaderManager().restartLoader(0, null, this);
        mBackgroundContainer.hideBackground();
    }

    /**
     * removeFromList()
     * Author: byron
     * Description: Called when removing item from list.
     * This toggles the product selected field from 1 to 0.
     * Afterward the cursor is reloaded with item removed
     *
     * @param position
     */
    public void removeFromList(int position) {
        ContentValues mContentValues = new ContentValues();
        Cursor c = ((StableArrayAdapter) mGroceryListView.getAdapter()).getCursor();
        c.moveToPosition(position);
        String id = Integer.toString(c.getInt(0));
        mSelections = id;
        mContentValues.put(Grocerylist.Products.PRO_SELECTED, 0);
        getContentResolver().update(Grocerylist.Products.CONTENT_URI, mContentValues, mSelections, null);
        moveToHistory(id);
    }

    private void moveToHistory(String id) {
        mDatabaseAdapter.open();
        mDatabaseAdapter.moveToHistory(id);
        mDatabaseAdapter.close();
    }

    /**
     * addToList()
     * Author: byron
     * Description: Adss item selected in search to gocerylist
     */
    public void addToList(String id) {
        ContentValues mContentValues = new ContentValues();

        mContentValues.put(Grocerylist.Products.PRO_SELECTED, 1);
        mSelections = id;
        getContentResolver().update(Grocerylist.Products.CONTENT_URI, mContentValues, mSelections, null);
        getLoaderManager().restartLoader(0, null, this);
        //searchItem.collapseActionView();
    }

    /**
     * onCreateLoader()
     * Description: Start a new loader or re-connect to existing one. Calls the contentprovider query
     *
     * @param: arg0
     * @param: arg1
     * @return: Loader<cursor>
     */
    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        mSelections = Grocerylist.Products.PRO_SELECTED + "=1";
        CursorLoader cursorLoader = new CursorLoader(this, Grocerylist.Products.CONTENT_URI, mProjections, mSelections, null, null);
        return cursorLoader;
    }

    /**
     * onLoadFinished()
     * Author: byron
     * Description:This is called after the loader has finished its load.
     * Cursor data is what has been loaded from the database using the contentprovider query
     *
     * @param loader
     * @param data
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
       if (data == null || data.getCount() == 0) {
           TextView emptyText = (TextView)findViewById(android.R.id.empty);
       }
       else {
           mAdapter.swapCursor(data);
           mAdapter.setCursor(data);
       }
    }

    /**
     * onLoaderReset()
     * Author: byron
     * Description: Resets the loader
     *
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    /**
     * getGrocerylist()
     * Author: byron
     * Description: Items in the list are designated by a 1 value being stored in the ProSelectected column of the tblProduct.
     * This method gets the cursor from the contentprovider that holds the names of the product for the listview.
     */
    private void getGrocerylist() {
        String[] from = {Grocerylist.Products.PRO_NAME};
       int[] to = {R.id.item};


        mAdapter = new StableArrayAdapter(this, R.layout.grocerylist_item, null, from, to, 0, mTouchListener);
        mGroceryListView.setAdapter(mAdapter);
        TextView emptyText = (TextView)findViewById(android.R.id.empty);
        mGroceryListView.setEmptyView(emptyText);
        getLoaderManager().initLoader(0, null, this);

    }
}
