package org.coughlin.grocerylist;

import static org.coughlin.grocerylist.FamilyMealContracts.BASE_CONTENT_URI;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import org.jetbrains.annotations.NotNull;

@Entity(tableName = "tblProduct")
public class Product {
    public static final String TABLE_NAME = "tblProduct";
    public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_NAME);
    public static final String DEFAULT_SORT_ORDER = "proName ASC";
    public Product(@NonNull String name, boolean selected, boolean checked) {
        this.name = name;
        this.selected = selected;
        this.checked = checked;
    }
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id") // matches `_id` column in database
    private int id;
    @ColumnInfo(name = "proName")
    @NotNull
    private String name;
    @ColumnInfo(name = "proSelected") // matches `proSelected` column in database
    private boolean selected;
    @ColumnInfo(name = "proChecked") // matches `proChecked` column in database
    private boolean checked;
    // Getters and Setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    @NonNull
    public String getName() {
        return name;
    }
    public void setName(@NonNull String name) {
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
