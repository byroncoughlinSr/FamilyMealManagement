package org.coughlin.grocerylist;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteQueryBuilder;

public class GrocerylistDatabaseAdapter {
    public DatabaseHelper databaseHelper;
    public SQLiteDatabase mDatabase;
    private final SQLiteQueryBuilder mSQLiteQueryBuilder;
    private final String[] mProjections;
    private final String mSelections;

    public GrocerylistDatabaseAdapter(Context mContext, SQLiteQueryBuilder mSQLiteQueryBuilder, String[] mProjections, String mSelections) {
        databaseHelper = new DatabaseHelper(mContext);
        this.mSQLiteQueryBuilder = mSQLiteQueryBuilder;
        this.mProjections = mProjections;
        this.mSelections = mSelections;
    }
}

