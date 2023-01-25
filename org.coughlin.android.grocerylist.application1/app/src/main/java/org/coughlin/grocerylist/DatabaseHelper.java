/**
 * 
 */
package org.coughlin.grocerylist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Description: We do not create a new database because we want to have it filled with products
 * There is a database already created that we can copy from the asset directory into database 
 * directory of the application. This is done one time when the application is first ran.
 * @author byron
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	public SQLiteDatabase mDatabase;
	public DatabaseHelper(Context context) {
		super(context, Grocerylist.DATABASE_NAME, null, Grocerylist.DATABASE_VERSION);
		//Checks to see if the database exists if not copy database from asset directory
		checkForDatabase(context);
	}
	
	/**
	 * 		onCreate() 
	 * Description:	This is a mandatory method. But is not used
	 * @param db
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
	
	/**				checkForDatabase()
	 * Author: byron
	 * Description: Checks to see if the database exists in the database directory
	 * of the application. If it does not then calls copyDb.
	 * @param mContext
	 */
	public void checkForDatabase(Context mContext) {
		try {
			String packageName = mContext.getPackageName();
			String fullPath = "/data/data/" + packageName + "/databases/" + Grocerylist.DATABASE_NAME;
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
				this.copyDB(fullPath, mContext);
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
		databaseInput = c.getAssets().open(Grocerylist.DATABASE_NAME);
		while ((length = databaseInput.read(buffer)) > 0) {
			databaseOutput.write( buffer, 0 , length);
			databaseOutput.flush();
		}
		databaseInput.close();
		databaseOutput.flush();
		databaseOutput.close();
	}	
}
	

