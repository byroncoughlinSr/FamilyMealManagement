package org.coughlin.grocerylist;

import java.util.List;
import java.util.Objects;
import android.app.Application;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;

public class GroceryContentProvider  extends ContentProvider {
	private ProductRepository repository;
	private DatabaseHelper database;

	@Override
	public boolean onCreate() {
		Application application = (Application) Objects.requireNonNull(getContext()).getApplicationContext();
		repository = new ProductRepository(application);
		return true;
	}
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String query = null;

		if (SearchManager.SUGGEST_URI_PATH_QUERY.equals(uri.getLastPathSegment())) {
			query = (selectionArgs != null && selectionArgs.length > 0) ? selectionArgs[0] : "";
		}

		query = (query == null) ? "" : "%" + query + "%";
		List<Product> products = repository.getFilteredProducts(query);
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
	public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(@NonNull Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(@NonNull Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int update(@NonNull Uri uri, ContentValues values, String selection,
					  String[] selectionArgs) {
		SQLiteDatabase db = database.getWritableDatabase();
		String where = Product.COLUMN_NAME_PRODUCT_ID + " = " + selection;
		db.update(Product.TABLE_NAME_PRODUCT, values, where, null);
		return 0;
	}

}
