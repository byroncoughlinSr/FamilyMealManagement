/**
 * 
 */
package org.coughlin.grocerylist;

import org.coughlin.grocerylist.DatabaseHelper;

import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @author byron
 * 
 * Defines a contract between the Grocerylist content provider and its clients. A contract defines the
 * information that a client needs to access the provider as one or more data tables. A contract
 * is a public, non-extendable (final) class that contains constants defining column names and
 * URIs. 
 */
public final class Historylist {
	public static final String AUTHORITY = "org.coughlin.provider.history";
	public static final String BASE_PATH = "tblHistory";
	public static final Uri CONTENT_URI= Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + BASE_PATH;
	public static final String DATABASE_NAME = "dbFamilyMeal";
	public static final int DATABASE_VERSION = 1;
	
	//History table definitions
	public static final String HIS_TABLE_NAME = "tblHistory";	
	protected static final String HIS_DATE="hisDate";
	protected static final String ROW_ID = "_id";
    protected static final String PRO_ID = "proId";
    
    	// This class cannot be instantiated	
    	private Historylist() {
    		
    	}

        /*
         * URI definitions
         */

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        /**
         * Path parts for the URIs
         */

               
        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";

        /*
         * Column definitions
         */

        /**
         * Column name for the title of the note
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_NAME_TITLE = "title";

        /**
         * Column name of the note content
         * <P>Type: TEXT</P>
         */
        public static final String COLUMN_NAME_NOTE = "note";

        /**
         * Column name for the creation timestamp
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String COLUMN_NAME_CREATE_DATE = "created";

        /**
         * Column name for the modification timestamp
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String COLUMN_NAME_MODIFICATION_DATE = "modified";
    }

