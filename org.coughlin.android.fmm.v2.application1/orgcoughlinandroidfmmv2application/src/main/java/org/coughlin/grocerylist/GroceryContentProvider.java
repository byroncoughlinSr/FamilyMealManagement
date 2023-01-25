package org.coughlin.grocerylist;

import java.util.HashMap;
import java.util.Map;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

public class GroceryContentProvider  extends ContentProvider {
	private DatabaseHelper database;
	private SQLiteQueryBuilder mSQLiteQueryBuilder;
		
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
	public boolean onCreate() {
		database = new DatabaseHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor cursor = null;
		
		Map<String, String> projectionMap = new HashMap<String, String>();
		projectionMap.put(BaseColumns._ID,  FamilyMealContracts.Products.ROW_ID + " AS " + BaseColumns._ID);
		projectionMap.put(FamilyMealContracts.Products.PRO_NAME, FamilyMealContracts.Products.PRO_NAME);
		projectionMap.put(SearchManager.SUGGEST_COLUMN_TEXT_1, FamilyMealContracts.Products.PRO_NAME + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);
		projectionMap.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,  FamilyMealContracts.Products.ROW_ID + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
				
		String query = uri.getLastPathSegment();
		
		if(SearchManager.SUGGEST_URI_PATH_QUERY.equals(query)) {
			
			selection = FamilyMealContracts.Products.PRO_NAME + " like '%" + selectionArgs[0] + "%'";
			mSQLiteQueryBuilder = new SQLiteQueryBuilder();
						mSQLiteQueryBuilder.setTables(FamilyMealContracts.Products.PRO_TABLE_NAME);
			mSQLiteQueryBuilder.setProjectionMap(projectionMap);
			
			SQLiteDatabase db = database.getWritableDatabase();		
			cursor = mSQLiteQueryBuilder.query(db, null, selection, null, null, null, null);
 			cursor.moveToFirst();
 			return cursor;
		} 
		else {
			mSQLiteQueryBuilder = new SQLiteQueryBuilder();
			mSQLiteQueryBuilder.setTables(FamilyMealContracts.Products.PRO_TABLE_NAME);
			SQLiteDatabase db = database.getWritableDatabase();
			
			cursor = mSQLiteQueryBuilder.query(db, null, selection, selectionArgs, null, null, null);
			cursor.moveToFirst();
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
			return cursor;	
		}
		
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = database.getWritableDatabase();
		String where = FamilyMealContracts.Products.ROW_ID + " = " + selection;
		db.update(FamilyMealContracts.Products.PRO_TABLE_NAME, values, where, null);
		return 0;
	}

}
