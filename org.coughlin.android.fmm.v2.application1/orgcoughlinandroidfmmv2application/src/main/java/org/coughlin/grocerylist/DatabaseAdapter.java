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
	public boolean checkForProduct(String product) {
		Cursor cursor;
		String sqlStr;
		
		sqlStr = "SELECT " + FamilyMealContracts.Products.PRO_NAME + " FROM " + FamilyMealContracts.Products.PRO_TABLE_NAME + " WHERE " + FamilyMealContracts.Products.PRO_NAME + "='" + product + "'";
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
		
		sqlStr = "SELECT " + FamilyMealContracts.MenuItems.MEN_NAME + " FROM " + FamilyMealContracts.MenuItems.MEN_TABLE_NAME + " WHERE " + FamilyMealContracts.MenuItems.MEN_NAME + "='" + menuItem + "'";
		cursor = mDatabase.rawQuery(sqlStr, null);
		cursor.moveToFirst();
		cursor.close();
		return cursor.getCount() > 0;
	}
	
	/**
	 * addProduct()
	 * Author: byron
	 * Description: Adds new product to database
	 */
	public void addProduct(String product) {
		ContentValues mContentValues;
		mContentValues = new ContentValues();
		mContentValues.put(FamilyMealContracts.Products.PRO_NAME, product);

		mDatabase.insert(FamilyMealContracts.Products.PRO_TABLE_NAME, null, mContentValues);
	}
	
	/**
	 * addMenuItem
	 * Description: Adds new menu Item to database
	 */
	public void addMenuItem(String menuItem) {
		ContentValues mContentValues;
		mContentValues = new ContentValues();
		mContentValues.put(FamilyMealContracts.MenuItems.MEN_NAME, menuItem);

		mDatabase.insert(FamilyMealContracts.MenuItems.MEN_TABLE_NAME, null, mContentValues);
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

} // DatabaseAdapter
