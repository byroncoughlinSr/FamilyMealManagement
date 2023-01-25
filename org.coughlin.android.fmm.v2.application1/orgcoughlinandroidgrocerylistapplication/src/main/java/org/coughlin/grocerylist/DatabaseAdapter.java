/**
 */
package org.coughlin.grocerylist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DatabaseAdapter {
	public DatabaseHelper databaseHelper;
	public SQLiteDatabase mDatabase;

	public DatabaseAdapter(Context mContext) {
		databaseHelper = new DatabaseHelper(mContext);
	}
	public void open() throws SQLException {			
		mDatabase = databaseHelper.getWritableDatabase();
	}
	public void close() {
		databaseHelper.close();
	}
	public boolean checkForProduct(String product) {
		Cursor cursor;
		String sqlStr;
		sqlStr = "SELECT " + Grocerylist.Products.PRO_NAME + " FROM " + Grocerylist.Products.PRO_TABLE_NAME + " WHERE " + Grocerylist.Products.PRO_NAME + "='" + product + "'";
		cursor = mDatabase.rawQuery(sqlStr, null);
		cursor.moveToFirst();
		cursor.close();
		return cursor.getCount() > 0;
	}
	public void addProduct(String product) {
		ContentValues mContentValues;
		mContentValues = new ContentValues();
		mContentValues.put(Grocerylist.Products.PRO_NAME, product);
		mDatabase.insert(Grocerylist.Products.PRO_TABLE_NAME, null, mContentValues);
	}
	public void moveToHistory(String id) {
		ContentValues mContentValues;
		Date date;
		long time = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
		date = new Date(time);
		sdf.format(date);
		String currentDate = date.toString();
		mContentValues = new ContentValues();
		mContentValues.put(Historylist.PRO_ID, id);
		mContentValues.put(Historylist.HIS_DATE, currentDate);
		mDatabase.insert(Historylist.HIS_TABLE_NAME, null, mContentValues);
	}
}
