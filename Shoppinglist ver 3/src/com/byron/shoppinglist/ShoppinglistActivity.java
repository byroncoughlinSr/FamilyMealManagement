package com.byron.shoppinglist;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

/**
 * @author byron
 * Class Description: Activity implementation for shoppinglist
 * Methods:
 * onCreate() Called when starting activity and initializing activity variables
 * onCreateOptionsMenu() initialzize standard contents of menu and inflates menu
 *
 */
public class ShoppinglistActivity extends Activity {
	public DatabaseAdapter databaseAdapter;
	public ListView listView;
	
	

	/** 				onCreate() 
	 * Description:	Called when starting activity. Initializes activity variables
	 * Param: Bundle
	 * Returns:	Void
	 */
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shoppinglist_layout);        
        listView = (ListView) findViewById(R.id.listView1);
        
        
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        databaseAdapter = new DatabaseAdapter(this);
        
        databaseAdapter.checkDb(this);
        
        getItems();
       
    }

	/** 				onCreateOptionsMenu() 
	 * Description: initialzize standard contents and inflates menu
	 * Param: Menu
	 * Return boolean
	 */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.shoppinglist_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }  
    
   /**	onOptionsItemSelected() 
    * Description :Handles menu item selected
    * param: MenuItem
    * returns: boolean
    */
		   
    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
    	switch (item.getItemId()) {	
    	case R.id.search:
    		addToList();
    		return true;
    	case R.id.remove_btn:
    	removeFromList();
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    /**  getItems
     * Description: gets grocery list from the database and inserts into listview
     * param: None
     * returns: void 
     */
    public void getItems() {
    	databaseAdapter.open();
    	Cursor items = databaseAdapter.getList();
    	String[] from = {databaseAdapter.COL_1};
    	int[] to = {R.id.text1};
    	SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.listview_layout, items, from, to, 0);
    	listView.setAdapter(adapter); 
    	 databaseAdapter.close();
    }
    
    /** addToList 
     * Description: starts the Searchable activity to add items to grocery list
     * param: None
     * return: void
     */
    public void addToList() {
    	Intent intent = new Intent(this, SearchableActivity.class);
    	startActivity(intent);
    }
    
    /** removeFromList()
     * Description: removes checked items from list
     * param: none
     * return: void
     */
    public void removeFromList() {
    	databaseAdapter.open();
    	SparseBooleanArray checked = listView.getCheckedItemPositions();	
    	ArrayList<String> selectedItems = new ArrayList<String>();
    	for(int i = 0; i < checked.size(); i++) {
    		int position = checked.keyAt(i); 
    		String text = (String)((Cursor)listView.getItemAtPosition(position)).getString(0);
    		databaseAdapter.unselectItem(text);
    	}
    	databaseAdapter.close();
    	getItems();
    }
}
