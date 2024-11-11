package org.coughlin.grocerylist;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines a contract between the FamilyMealContracts content provider and its clients.
 * A contract defines the information that a client needs to access the provider as one or more data tables.
 */
public final class FamilyMealContracts {
    public static final String AUTHORITY = "org.coughlin.provider.grocery";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // This class cannot be instantiated
    private FamilyMealContracts() {}

    /**
     * Products table contract
     */
    public static final class Products implements BaseColumns {
        public static final String TABLE_NAME = "tblProduct";

        // Column definitions
        public static final String COLUMN_NAME_PRODUCT_ID = "proId";       // Product ID
        public static final String COLUMN_NAME_PRODUCT_NAME = "proName";   // Product Name
        public static final String COLUMN_NAME_PRODUCT_SELECTED = "proSelected"; // Selected state
        public static final String COLUMN_NAME_PRODUCT_CHECKED = "proChecked";   // Checked state

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_NAME);

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "proName ASC";
    }

    /**
     * Menu items table contract
     */
    public static final class MenuItems implements BaseColumns {
        public static final String TABLE_NAME = "tblMenu";

        // Column definitions
        public static final String COLUMN_NAME_MENU_ID = "menId";       // Menu Item ID
        public static final String COLUMN_NAME_MENU_NAME = "menName";   // Menu Item Name
        public static final String COLUMN_NAME_MENU_SELECTED = "menSelected"; // Selected state

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_NAME);

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "menName ASC";
    }
}
