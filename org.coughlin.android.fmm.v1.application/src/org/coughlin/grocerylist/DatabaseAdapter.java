/**
 */
package org.coughlin.grocerylist;

import java.sql.Date;
import java.text.SimpleDateFormat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Database Adapter</b></em>'.
 * <!-- end-user-doc -->
 *
 *
 * @see org.coughlin.grocerylist.grocerylist.GrocerylistPackage#getDatabaseAdapter()
 * @model
 * @generated
 */
/**
 * @author byron
 *
 */
public class DatabaseAdapter {
	public DatabaseHelper databaseHelper;
	public SQLiteDatabase mDatabase;
	private GroceryContentProvider myContentProvider;
	private SQLiteQueryBuilder mSQLiteQueryBuilder;
	private String[] mProjections;
	private String mSelections;	
	
	
	/**
	 * @param databaseHelper
	 */
	public DatabaseAdapter(Context mContext) {
		databaseHelper = new DatabaseHelper(mContext);
	}

	public void open() throws SQLException {			
		mDatabase = databaseHelper.getWritableDatabase();
	}

	/**			close()
	 * @description closes database
	 * @param none
	 * @return void
	 */
	public void close() {
		databaseHelper.close();
	}	
	
	/**			getList()
	 * @description gets list of items with value 1 set in proSelected field
	 * @param none
	 * @return Cursor
	 */
	public Cursor getGrocerylist(Context context) {
		Cursor cursor;
		cursor = mSQLiteQueryBuilder.query(mDatabase, mProjections, mSelections, null, null, null, null);	
		return cursor;
	}
	/**
	 * 						getHistoryList
	 * Description: Returns the history list from the database in a cursor
	 * @param: none
	 * @return: cursor
	 */
	public Cursor getHistoryList() {
		
		
		return null;
	}
	
	/**				checkForProduct()
	 * Author: byron
	 * Description: Checks to see if product exists in the database
	 * @param product
	 * @return
	 */
	public boolean checkForProduct(String product) {
		Cursor cursor;
		String sqlStr;
		
		sqlStr = "SELECT " + Grocerylist.Products.PRO_NAME + " FROM " + Grocerylist.Products.PRO_TABLE_NAME + " WHERE " + Grocerylist.Products.PRO_NAME + "='" + product + "'";
		cursor = mDatabase.rawQuery(sqlStr, null);
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			return true;
		}
		else {
			return false;
		}		
	}
				
	/**				checkForMenuItem
	 * Description: Checks to see if menu item exists in database
	 * @param menuItem
	 * @return
	 */
	public boolean checkForMenuItem(String menuItem) {
		Cursor cursor;
		String sqlStr;
		
		sqlStr = "SELECT " + Grocerylist.MenuItems.MEN_NAME + " FROM " + Grocerylist.MenuItems.MEN_TABLE_NAME + " WHERE " + Grocerylist.MenuItems.MEN_NAME + "='" + menuItem + "'";
		cursor = mDatabase.rawQuery(sqlStr, null);
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			return true;
		}
		else {
			return false;
		}		
	}
	
	/**				addProduct()
	 * Author: byron
	 * Description: Adds new product to database
	 * @param product
	 * @return
	 */
	public long addProduct(String product) { 
		ContentValues mContentValues;
		mContentValues = new ContentValues();
		mContentValues.put(Grocerylist.Products.PRO_NAME, product);
		
		return mDatabase.insert(Grocerylist.Products.PRO_TABLE_NAME, null, mContentValues);
	}
	
	/**			addMenuItem
	 * Description: Adds new menu Item to database 
	 * @param menuItem
	 * @return
	 */
	public long addMenuItem(String menuItem) { 
		ContentValues mContentValues;
		mContentValues = new ContentValues();
		mContentValues.put(Grocerylist.MenuItems.MEN_NAME, menuItem);
		
		return mDatabase.insert(Grocerylist.MenuItems.MEN_TABLE_NAME, null, mContentValues);
	}
	
	public long moveToHistory(String id) {
		ContentValues mContentValues;
		Date date;
		long time = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("mm-dd-yyyy");
		date = new Date(time);
		sdf.format(date);
		String currentDate = date.toString();
		mContentValues = new ContentValues();
		mContentValues.put(Historylist.PRO_ID, id);
		mContentValues.put(Historylist.HIS_DATE, currentDate);
		return mDatabase.insert(Historylist.HIS_TABLE_NAME, null, mContentValues);
	}

} // DatabaseAdapter
