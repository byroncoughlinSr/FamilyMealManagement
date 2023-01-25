package org.coughlin.grocerylist;

import java.util.HashMap;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

public class MenuActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
	private DrawerLayout mDrawerLayout;	
	private String[] mDrawerContents;
	private ListView mDrawerListView;
	private ListView mMenuListView;
	private ActionBarDrawerToggle mDrawerToggle;
	private DatabaseAdapter mDatabaseAdapter;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private StableArrayAdapter mAdapter;
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
		
		setContentView(R.layout.activity_menu);
		mTitle = getTitle();
		
		// Handle intent entries
		getIntent();
		handleIntent(getIntent());
		
		//Initialize main view objects
		mBackgroundContainer = (BackgroundContainer) findViewById(R.id.view1);		
		mMenuListView = (ListView)findViewById(R.id.menulistview);
		mMenuListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);	
		
		//Setup the database 
	    mDatabaseAdapter = new DatabaseAdapter(this);	
		
		//Setup and create navigation drawer
		mDrawerTitle = "Navigational Drawer";
		mDrawerContents = getResources().getStringArray(R.array.drawer_titles);
		mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
		mDrawerListView = (ListView)findViewById(R.id.left_drawer);
		
			

		//Create Adapter and set to DrawerListView
		mDrawerListView.setAdapter(new ArrayAdapter<String>(this, R.layout.item_drawer, mDrawerContents));
		
		// Set the click listener for item in the drawer
		mDrawerListView.setOnItemClickListener(new OnItemClickListener(){	    	
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				//Go to activity of item clicked
				if (position == 0) {
					Intent intent = new Intent(MenuActivity.this, GrocerylistActivity.class);
					startActivity(intent);				
				}
				else if (position == 1) {
					Intent intent = new Intent(MenuActivity.this, ProductActivity.class);
					startActivity(intent);				
				}
				else if (position == 2) {
					Intent intent = new Intent(MenuActivity.this, HistoryActivity.class);
					startActivity(intent);				
				}	
				else if (position == 3) {
					Intent intent = new Intent(MenuActivity.this, MenuActivity.class);
					startActivity(intent);				
				}
				else if (position == 4) {
					Intent intent = new Intent(MenuActivity.this, MenuItemActivity.class);
					startActivity(intent);	
				}
				//Close drawer				
				mDrawerLayout.closeDrawer(mDrawerListView);
			}		    	
		});	

		// Set home icon as up to home display and set home button as enabled
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// Handle the drawer icon on the actionbar
		mDrawerToggle = new ActionBarDrawerToggle(
				this,                  /* host Activity */
				mDrawerLayout,         /* DrawerLayout object */
				R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
				R.string.drawer_open,  /* "open drawer" description */
				R.string.drawer_close  /* "close drawer" description */
				) {
			/**				onDrawerClosed()
			 * Author: byron
			 * Description: Called when a drawer has settled in a completely closed state.
			 * @param view
			 */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu();
			}

			/**				onDrawerOpened()
			 * Author: byron
			 * Description: Called when a drawer has settled in a completely open state.
			 * @param drawerView
			 */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu();
			}
		};			
	
		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle); 
		
		getMenulist();
	}	
	
	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}
	
	@Override
	public void onNewIntent(Intent intent) {
		handleIntent(intent);
	}
	
	 /**				handleIntent()
		 * Author: byron
		 * Description: Handles search intent
		 * @param intent
		 */
		private void handleIntent(Intent intent) {
			if (Intent.ACTION_VIEW.equals(intent.getAction())) {
				Uri data = intent.getData();
				String id = data.getLastPathSegment();
				addToList(id);
			}		
		} 
	
	/**				onPostCreate()
	 * Author: byron
	 * Description:
	 * @param savedInstanceState
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	    // Sync the toggle state after onRestoreInstanceState has occurred.
	    mDrawerToggle.syncState();
	}
	
	/**				onConfigurationChanged()
	 * Author: byron
	 * Description:
	 * @param newConfig
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	    mDrawerToggle.onConfigurationChanged(newConfig);
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
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
   private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        float mDownX;
        private int mSwipeSlop = -1;
        
        /**				onTouch()
         * Author: byron
         * Description: Handles the swiping of an item to delete from list
         * @param v
         * @param event
         * @return true or false
         */
        @Override
        public boolean onTouch(final View v, MotionEvent event) {
        	if (mSwipeSlop < 0) {
                mSwipeSlop = ViewConfiguration.get(MenuActivity.this).
                		getScaledTouchSlop();
            }
           
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mItemPressed) {
                    // Multi-item swipes not handled
                    return false;
                }                
                if (mVelocityTracker == null) {
            		mVelocityTracker= VelocityTracker.obtain();
            		mVelocityTracker.recycle();
            	}
                else {
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
                {
                    float x = event.getX() + v.getTranslationX();
                    float deltaX = x - mDownX;
                    float deltaXAbs = Math.abs(deltaX);
                    mVelocityTracker.computeCurrentVelocity(1000 );
                    if (!mSwiping) {
                        if (deltaXAbs > mSwipeSlop) {
                            mSwiping = true;
                            mMenuListView.requestDisallowInterceptTouchEvent(true);
                            mBackgroundContainer.showBackground(v.getTop(), v.getHeight());
                        }
                    }
                    if (mSwiping) {
                        v.setTranslationX((x - mDownX));
                        v.setAlpha(1 - deltaXAbs / v.getWidth());
                    }
                }
                break;   

            case MotionEvent.ACTION_UP:
                {
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
                        mMenuListView.setEnabled(false);
                        v.animate().setDuration(duration).
                                alpha(endAlpha).translationX(endX).
                                withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Restore animated values
                                        v.setAlpha(1);
                                        v.setTranslationX(0);
                                        if (remove) {
                                             animateRemoval(mMenuListView, v);
                                        } else {
                                            mBackgroundContainer.hideBackground();
                                            mSwiping = false;
                                            mMenuListView.setEnabled(true);
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
    
    /**				animateRemoval()
     * Author: byron
     * Description: This method animates all other views in the ListView container (not including ignoreView)
     * into their final positions. It is called after ignoreView has been removed from the
     * adapter, but before layout has been run. The approach here is to figure out where
     * everything is now, then allow layout to run, then figure out where everything is after
     * layout, and then to run animations between all of those start/end positions.
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
        int position = mMenuListView.getPositionForView(viewToRemove);
        removeFromList(position);       
              
        getLoaderManager().restartLoader(0, null, this);
        mBackgroundContainer.hideBackground();
    }

	/**				onPostCreate()
	 * Author: byron
	 * Description:
	 * @param savedInstanceState
	 */
	

	/**				onConfigurationChanged()
	 * Author: byron
	 * Description:
	 * @param newConfig
	 */
	

	/**				onOptionsItemSelected()
	 * Author: byron
	 * Description:
	 * @param item
	 * @return
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
	
	/**					onCreateLoader()
	 * Description: Start a new loader or re-connect to existing one. Calls the contentprovider query
	 * @param: arg0
	 * @param: arg1
	 * @return: Loader<cursor>
	 */
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {		
		mSelections = Menulist.Menu.MEN_SELECTED + "=1";				
		CursorLoader cursorLoader = new CursorLoader(this, Menulist.Menu.CONTENT_URI, mProjections, mSelections, null, null);
		return cursorLoader;
	}

	/**				onLoadFinished()
	 * Author: byron
	 * Description:This is called after the loader has finished its load. 
	 * Cursor data is what has been loaded from the database using the contentprovider query
	 * @param loader
	 * @param data
	 */
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);
		mAdapter.setCursor(data);		
	}

	/**				onLoaderReset()
	 * Author: byron
	 * Description: Resets the loader 
	 * @param loader
	 */
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);		
	}
	
	/**				addToList()
	 * Author: byron
	 * Description: Adss item selected in search to menu list
	 */
	public void addToList(String id) {
			ContentValues mContentValues = new ContentValues();
			    		
	    		mContentValues.put(Menulist.Menu.MEN_SELECTED,1);
	    		mSelections = id;
	    		getContentResolver().update(Menulist.Menu.CONTENT_URI, mContentValues, mSelections, null);
	    		getLoaderManager().restartLoader(0, null, this);
	    		//searchItem.collapseActionView();	    	
	 }	
	
	/**				removeFromList()
	 * Author: byron
	 * Description: Called when removing item from list. 
	 * This toggles the product selected field from 1 to 0.
	 * Afterward the cursor is reloaded with item removed
	 * @param position
	 */
	public void removeFromList(int position) {
		ContentValues mContentValues = new ContentValues();
			Cursor c = ((StableArrayAdapter)mMenuListView.getAdapter()).getCursor();
			c.moveToPosition(position);
			String id = Integer.toString(c.getInt(0));
			mSelections = id;
    		mContentValues.put(Menulist.Menu.MEN_SELECTED,0);
    		getContentResolver().update(Menulist.Menu.CONTENT_URI, mContentValues, mSelections, null);
   		  		
    	}
	
	
	/**				getMenulist()
	 * Author: byron
	 * Description: Items in the list are designated by a 1 value being stored in the ProSelectected column of the tblProduct.
	 * This method gets the cursor from the contentprovider that holds the names of the product for the listview.
	 */
	private void getMenulist() {
		String[] from = {Menulist.Menu.MEN_NAME};
    	int[] to = {R.id.item};
		getLoaderManager().initLoader(0, null, this);
		mAdapter = new StableArrayAdapter(this, R.layout.menulist_item, null, from, to,0, mTouchListener);
    	mMenuListView.setAdapter(mAdapter); 
  
    }

}
