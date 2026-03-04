package org.coughlin.grocerylist;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tblDailyMenu")
public class DailyMenu {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private int id;

    @ColumnInfo(name = "menuDate")
    @NonNull
    private String menuDate;

    @ColumnInfo(name = "mealType")
    @NonNull
    private String mealType;

    @ColumnInfo(name = "mealDescription")
    @NonNull
    private String mealDescription;

    public DailyMenu(@NonNull String menuDate, @NonNull String mealType, @NonNull String mealDescription) {
        this.menuDate = menuDate;
        this.mealType = mealType;
        this.mealDescription = mealDescription;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getMenuDate() {
        return menuDate;
    }

    public void setMenuDate(@NonNull String menuDate) {
        this.menuDate = menuDate;
    }

    @NonNull
    public String getMealType() {
        return mealType;
    }

    public void setMealType(@NonNull String mealType) {
        this.mealType = mealType;
    }

    @NonNull
    public String getMealDescription() {
        return mealDescription;
    }

    public void setMealDescription(@NonNull String mealDescription) {
        this.mealDescription = mealDescription;
    }
}
