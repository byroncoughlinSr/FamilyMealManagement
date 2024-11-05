package org.coughlin.grocerylist;

import android.net.Uri;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class Product {
    public static final String TABLE_NAME = "tblProduct";
    public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_NAME);

    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = "proName ASC";
    @PrimaryKey(autoGenerate = true)
    private int id;

    private static String name;
    private boolean selected;
    private boolean checked;

    // Constructor
    public Product(String name, boolean selected, boolean checked) {
        this.name = name;
        this.selected = selected;
        this.checked = checked;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
