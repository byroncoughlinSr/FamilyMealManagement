package com.byron.shoppinglist;

import java.util.ArrayList;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

public class SearchableActivity extends ListActivity  {
	private TextView statusView;
    private ListView listView;
    private DatabaseAdapter dbAdapter;
	
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); {        
        	setContentView(R.layout.search);          
        	handleIntent(getIntent());
        	getActionBar().setDisplayHomeAsUpEnabled(true);
        	listView = (ListView) findViewById(android.R.id.list);       
        	listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        	dbAdapter = new DatabaseAdapter(this);        
        	dbAdapter.checkDb(this);        		
        }
	}
	public void onNewInent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}
        
    public void handleIntent(Intent intent) {
    	if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
    		String query = intent.getStringExtra(SearchManager.QUERY);
    	}
    	
    }
	  
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
	    getMenuInflater().inflate(R.menu.options_menu, menu);
	        
	    // Associate searchable configuration with the SearchView
	    SearchManager searchManager =
	    		(SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    SearchView searchView =
	            (SearchView) menu.findItem(R.id.search).getActionView();
	    searchView.setSearchableInfo(
	            searchManager.getSearchableInfo(getComponentName()));
	    searchView.setOnQueryTextListener(new OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String query) {
				showList(query);
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				showList(newText);
				return false;
			}
		});
	    	return true;
	 }
	 @Override
	    public boolean onOptionsItemSelected (MenuItem item) {
	    	switch (item.getItemId()) {	
	    	case android.R.id.home:
	    		NavUtils.navigateUpFromSameTask(this);
	    		case R.id.add:
	    	addToList();
	    		return true;
	    	default:
	    		return super.onOptionsItemSelected(item);
	    	}
	    }
	
	public void showList(String qry) {
		dbAdapter.open();
    	Cursor items = dbAdapter.getSearch(qry);
    	String[] from = {dbAdapter.COL_1};
    	int[] to = {R.id.text1};
    	SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.listview_layout, items, from, to, 0);
    	listView.setAdapter(adapter); 
    	 dbAdapter.close();
	}
	
	 public void addToList() {
	    	dbAdapter.open();
	    	SparseBooleanArray checked = listView.getCheckedItemPositions();	
	    	ArrayList<String> selectedItems = new ArrayList<String>();
	    	for(int i = 0; i < checked.size(); i++) {
	    		int position = checked.keyAt(i); 
	    		String text = (String)((Cursor)listView.getItemAtPosition(position)).getString(0);
	    		dbAdapter.selectItem(text);
	    	}
	    	dbAdapter.close();
	    }
	 
	  
}
