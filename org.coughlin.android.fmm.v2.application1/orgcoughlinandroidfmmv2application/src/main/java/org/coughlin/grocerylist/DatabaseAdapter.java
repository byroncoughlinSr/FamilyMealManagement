/**
 */
package org.coughlin.grocerylist;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

/**						DatabaseAdapter
 * @description class database adapter does CRUD operations on SQLite database
 * @author byron
 *
 */
public class DatabaseAdapter {
	public DatabaseHelper databaseHelper;
	public SQLiteDatabase mDatabase;
	private final SQLiteQueryBuilder mSQLiteQueryBuilder;
	private final String[] mProjections;
	private final String mSelections;
	
	
	/**
	 * @description constructor builds database adapter
	 * @param mContext allows access to application-specific resources and classes,
	 *                 as well as up-calls for application-level operations such as launching activities,
	 *                 broadcasting and receiving intents, etc.
	 * @param mSQLiteQueryBuilder query builder
	 * @param mProjections projections
	 * @param mSelections selections
	 */
	public DatabaseAdapter(Context mContext, SQLiteQueryBuilder mSQLiteQueryBuilder, String[] mProjections, String mSelections) {
		databaseHelper = new DatabaseHelper(mContext);
		this.mSQLiteQueryBuilder = mSQLiteQueryBuilder;
		this.mProjections = mProjections;
		this.mSelections = mSelections;
	}

	/**
	 * @description Opens database with write permissions
	 * @throws SQLException throws an exception if database can not be opened
	 */
	public void open() throws SQLException {
		mDatabase = databaseHelper.getWritableDatabase();
	}

	/**			close()
	 * @description closes database
	 */
	public void close() {
		databaseHelper.close();
	}

	/**			getList()
	 * @description gets list of items with value 1 set in proSelected field
	 * @return cursor cursor to database list
	 */
	public Cursor getGrocerylist() {
		Cursor cursor;
		cursor = mSQLiteQueryBuilder.query(mDatabase, mProjections, mSelections, null, null, null, null);	
		return cursor;
	}

	/**				checkForProduct()
	 * Author: byron
	 * Description: Checks to see if product exists in the database
	 * @param product product to check if exist
	 * @return boolean
	 */
	public boolean checkForProduct(Product product) {
		Cursor cursor;
		String sqlStr;
		
		sqlStr = "SELECT " + product.getName() + " FROM " + Product.TABLE_NAME + " WHERE " + product.getName() + "='" + product + "'";
		cursor = mDatabase.rawQuery(sqlStr, null);
		cursor.moveToFirst();
		cursor.close();
		return cursor.getCount() > 0;
	}
				
	/**				checkForMenuItem
	 * Description: Checks to see if menu item exists in database
	 * @param menuItem menu item to check for existence in database
	 */
	public boolean checkForMenuItem(String menuItem) {
		Cursor cursor;
		String sqlStr;
	return true;
	}



} // DatabaseAdapter
