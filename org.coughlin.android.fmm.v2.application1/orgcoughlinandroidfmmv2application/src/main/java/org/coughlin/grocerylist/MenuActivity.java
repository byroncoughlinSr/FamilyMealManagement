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
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.database.sqlite.SQLiteQueryBuilder;

public class MenuActivity extends Activity  {
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerListView;
	private ListView mMenuListView;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private StableArrayAdapter mAdapter;
    private final String[] mProjections = {FamilyMealContracts.Products.ROW_ID, FamilyMealContracts.Products.PRO_NAME};
	private final SQLiteQueryBuilder mSQLiteQueryBuilder = new SQLiteQueryBuilder();
    private String mSelections;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//set content view
		mTitle = getTitle();
		setContentView( R.layout.activity_menu);

		// Handle intent entries
		handleIntent(getIntent());

		
		//Setup the database 
		new DatabaseAdapter(this, mSQLiteQueryBuilder, mProjections, mSelections);
		
		//Setup and create navigation drawer
		mDrawerTitle = "Navigational Drawer";
		String[] mDrawerContents = getResources().getStringArray(R.array.drawer_titles);
		mDrawerLayout = findViewById(R.id.drawer_layout);
		mDrawerListView = findViewById(R.id.left_drawer);

		//Create Adapter and set to DrawerListView
		mDrawerListView.setAdapter(new ArrayAdapter<>(this, R.layout.item_drawer, mDrawerContents));
		
		// Set the click listener for item in the drawer
		mDrawerListView.setOnItemClickListener((parent, view, position, id) -> {

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
		});

		// Set home icon as up to home display and set home button as enabled
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// Handle the drawer icon on the actionbar
		mDrawerToggle = new ActionBarDrawerToggle(
				this,                  /* host Activity */
				mDrawerLayout,         /* DrawerLayout object */
				null,
				R.string.drawer_open,  /* "open drawer" description */
				R.string.drawer_close  /* "close drawer" description */
				) {
			/**				onDrawerClosed()
			 * Author: byron
			 * Description: Called when a drawer has settled in a completely closed state.
			 * @param view view
			 */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu();
			}

			/**				onDrawerOpened()
			 * Author: byron
			 * Description: Called when a drawer has settled in a completely open state.
			 * @param drawerView view of drawer
			 */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu();
			}
		};			
	
		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);
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
	  * @param intent intent
	  */
		private void handleIntent(Intent intent) {
			if (Intent.ACTION_VIEW.equals(intent.getAction())) {
				Uri data = intent.getData();
				String id = data.getLastPathSegment();
			}		
		} 
	
	/**				onPostCreate()
	 * Author: byron
	 * Description:
	 * @param savedInstanceState saved instance
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
	 * @param newConfig new config
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
    	
    	// Associate searchable configuration with the SearchView
		SearchManager searchManager =
				(SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView =
				(SearchView) menu.findItem(R.id.search).getActionView();
		searchView.setSearchableInfo(
				searchManager.getSearchableInfo(getComponentName()));		   
		return true;				
	}
}
