package org.coughlin.grocerylist;

import java.util.HashMap;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.CursorAdapter;
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

public class HistoryActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

	private DrawerLayout mDrawerLayout;	
	private String[] mDrawerContents;
	private ListView mDrawerListView;
	private ListView mHistoryView;
	private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mActionBarTitle;
    private CursorAdapter historyAdapter;
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
  
	/**				onCreate()
	 * Author: byron
	 * Description: Initializes the activity. Also checks to see if database exists.
	 * @param savedInstanceState
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set Content View
		setContentView(R.layout.activity_history);
		//Initialize objects
		mBackgroundContainer = (BackgroundContainer) findViewById(R.id.view1);
		mDrawerContents = getResources().getStringArray(R.array.drawer_titles);
		mDrawerListView = (ListView)findViewById(R.id.left_drawer);
		mHistoryView = (ListView)findViewById(R.id.historylistview);
		mHistoryView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);		
		mActionBarTitle = mDrawerTitle = getTitle();	
		
		// Handle intent entries
		getIntent();
		handleIntent(getIntent());
		
		// Set the drawer toggle as the DrawerListener
		 mDrawerLayout.setDrawerListener(mDrawerToggle);
		 
		 // Set home icon as up to home display and set home button as enabled
		 getActionBar().setDisplayHomeAsUpEnabled(true);
		 getActionBar().setHomeButtonEnabled(true);
		 
		//Create Adapter and set to DrawerListView
		mDrawerListView.setAdapter(new ArrayAdapter<String>(this, R.layout.item_drawer, mDrawerContents));
				
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
	    			getActionBar().setTitle(mActionBarTitle);
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
		    
		// Set the click listener for item in the drawer
	    mDrawerListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position == 0) {
						Intent intent = new Intent(HistoryActivity.this, GrocerylistActivity.class);
						startActivity(intent);				
					}
				if (position == 1) {
					Intent intent = new Intent(HistoryActivity.this, ProductActivity.class);
					startActivity(intent);				
				}
					
					//Close drawer
					mDrawerLayout.closeDrawer(mDrawerListView);
				}		    	
		    });	    
	    	   
	    //Get list of products to display on grocerylist
	    getHistorylist();
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
		}		
	} 

		/**				onNewIntent()
	 * Author: byron
	 * Description: Called when new intent is called
	 * @param intent
	 */
	@Override
	public void onNewIntent(Intent intent) {
		handleIntent(intent);
	}
	
	/**				onCreateOptionsMenu()
	 * Author: byron
	 * Description: Inflates menu and then associates searchable configuration with SearchView
	 * @param menu
	 * @return
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.history, menu);
					   
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
                mSwipeSlop = ViewConfiguration.get(HistoryActivity.this).
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
                            mHistoryView.requestDisallowInterceptTouchEvent(true);
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
                        mHistoryView.setEnabled(false);
                        v.animate().setDuration(duration).
                                alpha(endAlpha).translationX(endX).
                                withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Restore animated values
                                        v.setAlpha(1);
                                        v.setTranslationX(0);
                                        if (remove) {
                                             animateRemoval(mHistoryView, v);
                                        } else {
                                            mBackgroundContainer.hideBackground();
                                            mSwiping = false;
                                            mHistoryView.setEnabled(true);
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
                long itemId = historyAdapter.getItemId(position);
                mItemIdTopMap.put(itemId, child.getTop());
            }
        }
        // Delete the item from the adapter
        int position = mHistoryView.getPositionForView(viewToRemove);
        removeFromList(position);       
              
        getLoaderManager().restartLoader(0, null, this);
        mBackgroundContainer.hideBackground();
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
		CursorLoader historyLoader = new CursorLoader(this, Historylist.CONTENT_URI, mProjections, null, null, null);
		//CursorLoader cursorLoader = new CursorLoader(this, Historylist.CONTENT_URI, mProjections, null, null, null);
		return historyLoader;
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
		historyAdapter.swapCursor(data);
		//historyAdapter.setCursor(data);		
	}

	/**				onLoaderReset()
	 * Author: byron
	 * Description: Resets the loader 
	 * @param loader
	 */
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		historyAdapter.swapCursor(null);
		//mAdapter.swapCursor(null);		
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
			Cursor c = ((HistoryAdapter)mHistoryView.getAdapter()).getCursor();
			c.moveToPosition(position);
			mSelections = Integer.toString(c.getInt(1));
			
    		mContentValues.put(Grocerylist.Products.PRO_SELECTED,0);
    		getContentResolver().delete(Historylist.CONTENT_URI, mSelections, null);
    		
    	}
	
	/**				getHistorylist()
	 * Author: byron
	 * Description: Items in the list are designated by a 1 value being stored in the ProSelectected column of the tblProduct.
	 * This method gets the cursor from the contentprovider that holds the names of the product for the listview.
	 */
	private void getHistorylist() {
		String[] from = {Grocerylist.Products.PRO_NAME, Historylist.HIS_DATE, Historylist.PRO_ID};
    	int[] to = {R.id.productTxt, R.id.dateTxt};
		getLoaderManager().initLoader(0, null, this);
		historyAdapter = new HistoryAdapter(this, R.layout.historylist_item, null, from, to,0, mTouchListener);
		//mAdapter = new StableArrayAdapter(this, R.layout.historylist_item, null, from, to,0, mTouchListener);
		mHistoryView.setAdapter(historyAdapter); 
  
    }

}
