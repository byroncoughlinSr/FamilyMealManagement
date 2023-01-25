package org.coughlin.grocerylist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ProductActivity extends Activity{
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerListView;
	private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
	private EditText text;
    private String product;
    private DatabaseAdapter mDatabaseAdapter;
	private final String[] mProjections = {FamilyMealContracts.Products.ROW_ID, FamilyMealContracts.Products.PRO_NAME};
	private final SQLiteQueryBuilder mSQLiteQueryBuilder = new SQLiteQueryBuilder();
	private final String mSelections = null;

	/**				onCreate()
	 * Author: byron
	 * Description:
	 * @param savedInstanceState saved instance
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_product);
		
		//Setup the database 
	    mDatabaseAdapter = new DatabaseAdapter(this, mSQLiteQueryBuilder, mProjections, mSelections);

		//Setup and create navigation drawer
		mDrawerTitle = "Navigation Drawer";
		String[] mDrawerContents = getResources().getStringArray(R.array.drawer_titles);
		mDrawerListView = findViewById(R.id.left_drawer);
		mDrawerLayout = findViewById(R.id.drawer_layout);

		//Create Adapter and set to DrawerListView
		mDrawerListView.setAdapter(new ArrayAdapter<>(this, R.layout.item_drawer, mDrawerContents));

		// Set the click listener for item in the drawer
		mDrawerListView.setOnItemClickListener((parent, view, position, id) -> {
			if (position == 0) {
				Intent intent = new Intent(ProductActivity.this, GrocerylistActivity.class);
				startActivity(intent);
			}
			else if (position == 2) {
				Intent intent = new Intent(ProductActivity.this, HistoryActivity.class);
				startActivity(intent);
			}

			else if (position == 1) {
				Intent intent = new Intent(ProductActivity.this, ProductActivity.class);
				startActivity(intent);
			}
			else if (position == 3) {
				Intent intent = new Intent(ProductActivity.this, MenuActivity.class);
				startActivity(intent);
			}
			else if (position == 4) {
				Intent intent = new Intent(ProductActivity.this, MenuItemActivity.class);
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
			 * @param drawerView drawer view
			 */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu();
			}
		};	

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.addDrawerListener(mDrawerToggle);

		Button button = findViewById(R.id.addProductBtn);

		//Handle button
		button.setOnClickListener(v -> {
			text = findViewById(R.id.newProductTxt);
			product = text.getText().toString();
			if (!checkProductExists(product)) {
				addProduct(product);
			}
			text.setText("");
			InputMethodManager inputMethodManager =(InputMethodManager)
					getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
		});
	}
	
	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.product, menu);
		return true;
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		
		// Pass the event to ActionBarDrawerToggle, if it returns
	    // true, then it has handled the app icon touch event
	    if (mDrawerToggle.onOptionsItemSelected(item)) {
	    	return true;
	    } 
	    
		return super.onOptionsItemSelected(item);
	}

	/**				checkProductExists()
	 * Author: byron
	 * Description: Checks if the product exist in the database
	 * @param product product
	 * @return boolean true if product exists
	 */
	private boolean checkProductExists(String product) {
		mDatabaseAdapter.open();
		if (mDatabaseAdapter.checkForProduct(product)) {
			mDatabaseAdapter.close();
			return true;			
		}
		else {
			mDatabaseAdapter.close();
			return false;
		}
		
	}
	
	private void addProduct(String product) {
		mDatabaseAdapter.open();
		mDatabaseAdapter.addProduct(product);
		mDatabaseAdapter.close();
	}
}

	
