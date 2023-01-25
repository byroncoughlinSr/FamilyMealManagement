/**
 * 
 */
package org.coughlin.grocerylist;

import android.content.ContentResolver;
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
public final class Grocerylist {
	public static final String AUTHORITY = "org.coughlin.provider.grocery";
	public static final String BASE_PATH = "tblProduct";
	public static final Uri CONTENT_URI= Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + BASE_PATH;
	public static final String DATABASE_NAME = "dbFamilyMeal";
	public static final int DATABASE_VERSION = 1;
	
	//History table definitions
	public static final String HIS_TABLE_NAME = "tblHistory";	
	public static final String HIS_ID="hisId";
	protected static final String HIS_DATE="hisDate";
		
	// This class cannot be instantiated	
	private Grocerylist() {
		
	}
	
	/**
    * Products table contract
    */
    public static final class Products implements BaseColumns {
    	//Product table definitios
    	public static final String PRO_TABLE_NAME = "tblProduct";
    	protected static final String ROW_ID = "_id";
    	protected static final String PRO_NAME = "proName";
    	protected static final String PRO_ID = "proId";
    	protected static final String PRO_SELECTED="proSelected";

        // This class cannot be instantiated
        private Products() {}

        /**
         * The table name offered by this provider
         */
        public static final String TABLE_NAME = "tblProducts";

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
         * Path part for the Notes URI
         */
        private static final String PATH_NOTES = "/tblProducts";

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);

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
    
    public static final class MenuItems implements BaseColumns {
    	//Product table definitios
    	public static final String MEN_TABLE_NAME = "tblMenu";
    	protected static final String ROW_ID = "_id";
    	protected static final String MEN_NAME = "menName";
    	protected static final String MEN_ID = "menId";
    	protected static final String MEN_SELECTED="menSelected";

        // This class cannot be instantiated
        private MenuItems() {}

        /**
         * The table name offered by this provider
         */
        public static final String TABLE_NAME = "tblMenu";

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
         * Path part for the Notes URI
         */
        private static final String PATH_NOTES = "/tblMenu";

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + PATH_NOTES);

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
	

}
