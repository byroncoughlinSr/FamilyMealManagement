package org.coughlin.grocerylist;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.widget.Toolbar;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ProductActivity extends AppCompatActivity {
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mTitle;
	private EditText text;
	private String product;
	DrawerHandler mDrawerHandler;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_product);
        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        ProductViewModel productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

		//Setup and create navigation drawer
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

		Button button = findViewById(R.id.addProductBtn);

		//Handle button
		button.setOnClickListener(v -> {
			text = findViewById(R.id.newProductTxt);
			product = text.getText().toString();
			text.setText("");
			productViewModel.insertNewProduct(product);
		});
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		Objects.requireNonNull(getActionBar()).setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.product, menu);
		return true;
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerHandler.syncState();
	}
}


	
