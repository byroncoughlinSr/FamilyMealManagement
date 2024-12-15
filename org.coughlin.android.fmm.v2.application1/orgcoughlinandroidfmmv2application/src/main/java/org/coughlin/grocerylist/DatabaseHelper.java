package org.coughlin.grocerylist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String TAG = "DatabaseHelper";
	public static final String DATABASE_NAME = "dbFamilyMeal";
	public static final int DATABASE_VERSION = 1;

	private final Context context;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Not used because we are using a prebuilt database
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Not used, as Room handles migration
	}

	/**
	 * Copies the database from the assets folder to the app's data directory if it doesn't already exist.
	 */
	public void createDatabase() throws IOException {
		if (!checkForDatabase()) {
			// Create an empty database file to overwrite
			this.getReadableDatabase().close();
			copyDatabaseFromAssets();
		}
	}

	/**
	 * Checks whether the database already exists in the app's data directory.
	 */
	private boolean checkForDatabase() {
		SQLiteDatabase tempDB = null;
		try {
			String fullPath = getDatabasePath();
			tempDB = SQLiteDatabase.openDatabase(fullPath, null, SQLiteDatabase.OPEN_READWRITE);
		} catch (SQLiteException e) {
			Log.i(TAG, "Database not found, it will be copied from assets.");
		}

		if (tempDB != null) {
			tempDB.close();
		}
		return tempDB != null;
	}

	/**
	 * Copies the database file from the assets directory to the app's database directory.
	 */
	private void copyDatabaseFromAssets() throws IOException {
		String fullPath = getDatabasePath();
		try (InputStream input = context.getAssets().open(DATABASE_NAME);
			 OutputStream output = Files.newOutputStream(Paths.get(fullPath))) {

			byte[] buffer = new byte[1024];
			int length;
			while ((length = input.read(buffer)) > 0) {
				output.write(buffer, 0, length);
			}

			Log.i(TAG, "Database successfully copied to: " + fullPath);
		} catch (Exception e) {
			Log.e(TAG, "Error copying database from assets", e);
			throw e;
		}
	}

	/**
	 * Gets the full path to the database in the app's data directory.
	 */
	private String getDatabasePath() {
		return context.getDatabasePath(DATABASE_NAME).getPath();
	}
}
