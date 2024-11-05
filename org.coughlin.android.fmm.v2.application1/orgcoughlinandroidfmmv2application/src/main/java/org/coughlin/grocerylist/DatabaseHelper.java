package org.coughlin.grocerylist;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Description: We do not create a new database because we want to have it filled with products
 * There is a database already created that we can copy from the asset directory into database 
 * directory of the application. This is done one time when the application is first ran.
 * @author byron
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	public static final String DATABASE_NAME = "dbFamilyMeal";
	public static final int DATABASE_VERSION = 1;
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	/**
	 * 		onCreate() 
	 * Description:	This is a mandatory method. But is not used
	 * @param db database to create
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
	
	}		
	
	/**			onUpgrade()
	 * Description: This is a mandatory method. But is not used
	 * @param	db, oldVersion, newVersion)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

	/**
	 *
	 * @param mContext context
	 * @throws IOException exception
	 */
	public void createDatabase(Context mContext) throws IOException {
		boolean dbExists = checkForDatabase(mContext);
		if(dbExists) {
			//Don't do anything
		} else {
			this.getReadableDatabase();
			try {
				copyDB(mContext);
			} catch (IOException e) {
				Log.e("dbFMeal - create", e.getMessage());
			}
		}
	}
	
	/**				checkForDatabase()
	 * Author: byron
	 * Description: Checks to see if the database exists in the database directory
	 * of the application. If it does not then calls copyDb.
	 * @param mContext context
	 */
	public boolean checkForDatabase(Context mContext) {
		SQLiteDatabase tempDB = null;
		try {

			String packageName = mContext.getPackageName();
			String fullPath = "/data/data/" + packageName + "/databases/" + DATABASE_NAME;

			tempDB = SQLiteDatabase.openDatabase(fullPath, null, SQLiteDatabase.OPEN_READWRITE);
		} catch (SQLiteException e) {
			Log.e("dbFMeal -check", e.getMessage());
		}

		if(tempDB != null) {
			tempDB.close();
		}
		return tempDB != null;
	}
	/**			copyDB()
	 * @description Copies database from assets directory into /data/data/apps/database
	 * 				directory
	 * @param mContext context
	 * @throws IOException throws exception
	 */
	public void copyDB(Context mContext) throws IOException {
		try {
			InputStream databaseInput = mContext.getAssets().open(DATABASE_NAME);
			String packageName = mContext.getPackageName();
			String fullPath = "/data/data/" + packageName + "/databases/" + DATABASE_NAME;
			OutputStream databaseOutput = Files.newOutputStream(Paths.get(fullPath));

			byte[] buffer = new byte[1024];
			int length;

			//open database file from asset folder
			while ((length = databaseInput.read(buffer)) > 0) {
				databaseOutput.write(buffer, 0, length);
			}
			databaseInput.close();
			databaseOutput.flush();
			databaseOutput.close();
		} catch (Exception e) {
			Log.e("dbFMeal - copyDatabase", e.getMessage());
		}
	}
}
	

