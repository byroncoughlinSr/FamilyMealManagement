package com.byron.shoppinglist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *  @author byron
 *  Description: Class that handles database functions
 *
 */
public class DatabaseAdapter {
	private static final String SL_DATABASE_NAME = "dbShoppinglist";
	private static final String SL_TABLE_NAME = "tblShoppinglist";
	private static final String ROW_ID = "_id";
	private static final String SHO_ID = "shoid";
	protected static final String COL_1 = "shoItem";
	protected static final String SHO_CHOOSEN = "shoChoosen";
	private static final int DATABASE_VERSION = 1;
	DatabaseHelper databaseHelper;
	public SQLiteDatabase database;
	private final Context context;
	
	public DatabaseAdapter(Context ctx) {
		this.context=ctx;
		databaseHelper = new DatabaseHelper(context);
	}
	
	
	/**
	 *  @author byronshoppinglist
	 *  Description: Subclass to handle database creation
	 */
	private class DatabaseHelper extends SQLiteOpenHelper {		
		
		DatabaseHelper(Context context) {
			super(context, SL_DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		/**
		 * 		onCreate() 
		 * Description:	Creates database. Runs one time. Inserts list into database
		 * Param: SQLiteDatabase
		 * Return: void
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			
		}
		
		
		
		/**			onUpgrade()
		 * Description: Recreates database when database needs to be upgraded
		 * @param	SQLiteDatabase, int, int)
		 * @return void
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}			
	}
	
	/** 		checkDb()
	 * @description Checks to see if database in /data/data/app exists. 
	 * 				If it does not exist. Then copy database from asset directory
	 * @param Context
	 * @return void
	 */
	public void checkDb(Context c) {
		try {
			//Android default database location is : data/data/com.byron.repowerAssets/databases
			String packageName = c.getPackageName();
			String fullPath = "/data/data/" + packageName + "/databases/" + SL_DATABASE_NAME;
			String destPath = "/data/data/" + packageName + "/databases/";
		
			// this database folder location
			File f = new File(destPath);
		
			//this database file location
			File obj = new File(fullPath);
		
			//check if database exists. If not create it
			if(!f.exists()) {
				f.mkdirs();
				f.createNewFile();
			}
		
			//check database file exists or if not copy database from assets
			if (!obj.exists()) {
				this.copyDB(fullPath, c);
			}			
		
		} catch (FileNotFoundException e) {
			e.printStackTrace(); 
		} catch (IOException e) {
			e.printStackTrace();
		}
}
	/**			copyDB()
	 * @description Copies database from assets directory into /data/data/apps/database
	 * 				directory 
	 * @param path
	 * @param c
	 * @throws IOException
	 */
	public void copyDB(String path, Context c) throws IOException {
		InputStream databaseInput = null;
		String outFileName = path;
		OutputStream databaseOutput = new FileOutputStream(outFileName);
	
		byte[] buffer = new byte[1024];
		int length;
	
		//open database file from asset folder
		databaseInput = c.getAssets().open(SL_DATABASE_NAME);
		while ((length = databaseInput.read(buffer)) > 0) {
			databaseOutput.write( buffer, 0 , length);
			databaseOutput.flush();
		}
		databaseInput.close();
		databaseOutput.flush();
		databaseOutput.close();
	}
		
	/**			openWritable()
	 * @description Opens database with write permissions
	 * @throws SQLException
	 * @returns void
	 */
	public void open() throws SQLException {			
		database = databaseHelper.getWritableDatabase();
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
	 * @description gets list of items with value 1 set in shoChoosen field
	 * @param none
	 * @return Cursor
	 */
	public Cursor getList() {
		Cursor cursor;
		String sqlStr;
		sqlStr = "SELECT " + COL_1 + "," + ROW_ID + " , " + SHO_CHOOSEN + " FROM " + SL_TABLE_NAME + " WHERE " + SHO_CHOOSEN + "= 1;";
		cursor = database.rawQuery(sqlStr, null);
		return cursor;
	}
	
	/**			unselectItem()
	 * @description Removes item from shopping list by changing its shoChoosen value to 0
	 * @param String
	 * @return void
	 */
	public void unselectItem(String item) {
		ContentValues args = new ContentValues();
		args.put(SHO_CHOOSEN, 0);
		database.update(SL_TABLE_NAME, args, COL_1 + "=?", new String[]{item});
	}
	
	public void selectItem(String item) {
		ContentValues args = new ContentValues();
		args.put(SHO_CHOOSEN, 1);
		database.update(SL_TABLE_NAME, args, COL_1 + "=?", new String[]{item});
	}
	
	public Cursor getSearch(String str) {
		Cursor cursor;
		String sqlStr;
		sqlStr = "SELECT " + COL_1 + "," + ROW_ID + " , " + SHO_CHOOSEN + " FROM " + SL_TABLE_NAME + " WHERE " + COL_1 + " LIKE '%" + str + "%';";
		cursor = database.rawQuery(sqlStr, null);
		return cursor;
	}
}

