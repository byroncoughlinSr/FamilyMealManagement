package org.coughlin.grocerylist;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Application;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

import androidx.lifecycle.LiveData;

public class GroceryContentProvider  extends ContentProvider {
	private GroceryListRepository repository;
	private DatabaseHelper database;
	private SQLiteQueryBuilder mSQLiteQueryBuilder;

	@Override
	public boolean onCreate() {
		Application application = (Application) getContext().getApplicationContext();
		repository = new GroceryListRepository(application);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String query = null;

		// Check if the query is for search suggestions
		if (SearchManager.SUGGEST_URI_PATH_QUERY.equals(uri.getLastPathSegment())) {
			query = (selectionArgs != null && selectionArgs.length > 0) ? selectionArgs[0] : "";
		}

		// Ensure the query is not null
		query = (query == null) ? "" : "%" + query + "%"; // Add wildcards for SQL LIKE

		// Query the database for matching products
		List<Product> products = repository.getFilteredProducts(query);

		// Convert List<Product> to Cursor
		MatrixCursor cursor = new MatrixCursor(new String[]{
				BaseColumns._ID,
				SearchManager.SUGGEST_COLUMN_TEXT_1,
				SearchManager.SUGGEST_COLUMN_INTENT_DATA
		});

		for (Product product : products) {
			cursor.addRow(new Object[]{
					product.getId(),
					product.getName(),
					"content://org.coughlin.provider.grocery/" + product.getId()
			});
		}
		return cursor;
	}


	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = database.getWritableDatabase();
		String where = FamilyMealContracts.Products.COLUMN_NAME_PRODUCT_ID + " = " + selection;
		db.update(FamilyMealContracts.Products.TABLE_NAME, values, where, null);
		return 0;
	}

}
