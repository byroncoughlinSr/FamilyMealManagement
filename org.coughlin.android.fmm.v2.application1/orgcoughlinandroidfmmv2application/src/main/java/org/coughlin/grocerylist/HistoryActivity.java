package org.coughlin.grocerylist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.concurrent.atomic.AtomicInteger;

import android.util.Log;
import android.view.Menu;
import android.widget.CursorAdapter;
import android.widget.ListView;

public class HistoryActivity extends AppCompatActivity {
	private RecyclerView mHistroyListView;
    private CharSequence mTitle;
	DrawerHandler mDrawerHandler;
	private HistoryAdapter mHistoryAdapter;
	LiveData<List<ProductHistory>> mHistoryList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		//Initialize variables
		mHistroyListView = findViewById(R.id.historylistview);
		DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
		mHistroyListView.setLayoutManager(new LinearLayoutManager(this));
		HistoryViewModel historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
		mHistoryList = historyViewModel.getAllHistory();
		List <ProductHistory> mProductDetailHistoryList = new ArrayList<>();
		RecyclerView mDrawerListView = findViewById(R.id.left_drawer);
		CharSequence mDrawerTitle = "Navigation Drawer";
		Toolbar toolbar = findViewById(R.id.toolbar);
		String[] drawerTitles = getResources().getStringArray(R.array.drawer_titles);
		List<String> drawerContents = Arrays.asList(drawerTitles);
		mDrawerHandler = new DrawerHandler(
				this,
				mDrawerLayout,
				mDrawerListView,
				toolbar,
				drawerContents,
				mDrawerTitle,
				mTitle
		);
		mTitle = getTitle();
		handleIntent(getIntent());
		mHistroyListView = findViewById(R.id.historylistview);
		mHistoryAdapter = new HistoryAdapter(new ArrayList<>(), historyViewModel);
		mHistroyListView.setAdapter(mHistoryAdapter);

		historyViewModel.getAllHistory().observe(this, historyList -> {
			if (historyList == null || historyList.isEmpty()) {
				mHistoryAdapter.updateHistorylist(new ArrayList<>());
				return;
			} else {
				for (ProductHistory product : historyList) {

				}
			}
		});


	}
	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
		}
	}
	@Override
	public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.history, menu);
		return true;
	}
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	    mDrawerHandler.syncState();
	}
}
