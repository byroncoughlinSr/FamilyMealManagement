package org.coughlin.grocerylist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ProductActivity extends Activity{
	private DrawerLayout mDrawerLayout;
	private String[] mDrawerContents;
	private ListView mDrawerListView;
	private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
	private Button button;
    private EditText text;
    private String product;
    private DatabaseAdapter mDatabaseAdapter;

	/**				onCreate()
	 * Author: byron
	 * Description:
	 * @param savedInstanceState
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_product);
		
		mDatabaseAdapter = new DatabaseAdapter(this);
		mDrawerContents = getResources().getStringArray(R.array.drawer_titles);
		mDrawerListView = (ListView)findViewById(R.id.left_drawer);
		mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);		
		mTitle = mDrawerTitle = getTitle();	
		
		// Set the drawer toggle as the DrawerListener
				 mDrawerLayout.setDrawerListener(mDrawerToggle);
				 
				//Create Adapter and set to DrawerListView
					mDrawerListView.setAdapter(new ArrayAdapter<String>(this, R.layout.item_drawer, mDrawerContents));
							
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
					    
					// Set the click listener for item in the drawer
				    mDrawerListView.setOnItemClickListener(new OnItemClickListener(){
						@Override
						public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
							if (position == 0) {
								Intent intent = new Intent(ProductActivity.this, GrocerylistActivity.class);
								startActivity(intent);				
							}							
							if (position == 1) {
									Intent intent = new Intent(ProductActivity.this, ProductActivity.class);
									startActivity(intent);				
								}
								
								//Close drawer
								mDrawerLayout.closeDrawer(mDrawerListView);
							}		    	
					    });	
		
		button = (Button)findViewById(R.id.addProductBtn);		
		
		//Handle button
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				text = (EditText)findViewById(R.id.newProductTxt);
				product = text.getText().toString();
				if (!checkProductExists(product)) {
					addProduct(product);
			   }				
			}			
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.product, menu);
		return true;
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
		return super.onOptionsItemSelected(item);
	}

	/**				checkProductExists()
	 * Author: byron
	 * Description: Checks if the product exist in the database
	 * @param product
	 * @return
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

	
