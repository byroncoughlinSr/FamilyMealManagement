package org.coughlin.grocerylist;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class HistoryContentProvider  extends ContentProvider {
	private DatabaseHelper database;
	private SQLiteQueryBuilder mSQLiteQueryBuilder;
		
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = database.getWritableDatabase();
		int id = Integer.parseInt(selection);
		db.delete(Historylist.HIS_TABLE_NAME, Historylist.PRO_ID + "=" + id, null);
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
		
		String tables = "tblHistory LEFT OUTER JOIN tblProduct ON (tblHistory.proId = tblProduct._id)";
		String[] mColumns = {"tblProduct." + FamilyMealContracts.Products.ROW_ID + ", tblProduct.'" + FamilyMealContracts.Products.PRO_NAME + "', tblHistory.'" +  Historylist.HIS_DATE + "'"};
		mSQLiteQueryBuilder = new SQLiteQueryBuilder();
		mSQLiteQueryBuilder.setTables(tables);
		SQLiteDatabase db = database.getWritableDatabase();
		cursor = mSQLiteQueryBuilder.query(db, null, selection, selectionArgs, null, null,
				" tblHistory.'" + Historylist.HIS_DATE + "' DESC" );
		cursor.moveToFirst();
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;	
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
