package org.coughlin.grocerylist;

import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
	private DrawerHandler drawerHandler;
	private HistoryAdapter historyAdapter;
	private ItemTouchHelper itemTouchHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);

		// Initialize RecyclerView
		RecyclerView historyRecyclerView = findViewById(R.id.historylistview);
		historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

		// Initialize ViewModel
		HistoryViewModel historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

		// Initialize Adapter with empty list
		historyAdapter = new HistoryAdapter(new ArrayList<>(), historyViewModel);
		historyRecyclerView.setAdapter(historyAdapter);

		// Attach ItemTouchHelper for swipe actions
		itemTouchHelper = new ItemTouchHelper(new HistoryTouchHelperCallback(
				historyAdapter,
				historyViewModel.getHistoryDao(),
				historyViewModel
		));
		itemTouchHelper.attachToRecyclerView(historyRecyclerView);

		// Observe LiveData for updates
		historyViewModel.getAllHistory().observe(this, historyList -> {
			if (historyList != null) {
				historyAdapter.updateHistorylist(historyList);
			} else {
				historyAdapter.updateHistorylist(new ArrayList<>()); // Handle empty list
			}
		});

		// Setup navigation drawer
		setupDrawer();
	}

	private void setupDrawer() {
		DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
		RecyclerView drawerRecyclerView = findViewById(R.id.left_drawer);
		Toolbar toolbar = findViewById(R.id.toolbar);
		String[] drawerTitles = getResources().getStringArray(R.array.drawer_titles);
		List<String> drawerContents = Arrays.asList(drawerTitles);
		CharSequence drawerTitle = "Navigation Drawer";
		CharSequence activityTitle = getTitle();

		drawerHandler = new DrawerHandler(
				this,
				drawerLayout,
				drawerRecyclerView,
				toolbar,
				drawerContents,
				drawerTitle,
				activityTitle
		);

		ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
				this,
				drawerLayout,
				toolbar,
				R.string.navigation_drawer_open,
				R.string.navigation_drawer_close
		);

		drawerLayout.addDrawerListener(drawerToggle);
		drawerToggle.syncState();
	}
}
