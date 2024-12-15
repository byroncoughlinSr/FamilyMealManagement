package org.coughlin.grocerylist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.Menu;
public class HistoryActivity extends AppCompatActivity {
    DrawerHandler mDrawerHandler;
	private HistoryAdapter mHistoryAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		//Initialize variables

        final RecyclerView mHistroyListView = findViewById(R.id.historylistview);
		DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
		mHistroyListView.setLayoutManager(new LinearLayoutManager(this));
		HistoryViewModel historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
		RecyclerView mDrawerListView = findViewById(R.id.left_drawer);
		CharSequence mDrawerTitle = "Navigation Drawer";
		Toolbar toolbar = findViewById(R.id.toolbar);
		String[] drawerTitles = getResources().getStringArray(R.array.drawer_titles);
		List<String> drawerContents = Arrays.asList(drawerTitles);
        CharSequence mTitle = getTitle();
		mDrawerHandler = new DrawerHandler(
				this,
				mDrawerLayout,
				mDrawerListView,
				toolbar,
				drawerContents,
				mDrawerTitle,
                mTitle
		);
		historyViewModel.getAllHistory().observe(this, historyList -> {
			if (historyList != null && !historyList.isEmpty()) {
				if (mHistoryAdapter == null) {
					mHistoryAdapter = new HistoryAdapter(historyList, historyViewModel );
					mHistroyListView.setAdapter(mHistoryAdapter);
				} else {
					mHistoryAdapter.updateHistorylist(historyList);
				}
			} else {
				if (mHistoryAdapter != null) {
					mHistoryAdapter.updateHistorylist(new ArrayList<>());
				}
			}
		});
		handleIntent(getIntent());
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
