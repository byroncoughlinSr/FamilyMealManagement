package org.coughlin.grocerylist;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "tblRecipe",
        indices = {@Index("dailyMenuId")},
        foreignKeys = @ForeignKey(
                entity = DailyMenu.class,
                parentColumns = "_id",
                childColumns = "dailyMenuId",
                onDelete = ForeignKey.CASCADE))
public class Recipe {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private int id;

    @ColumnInfo(name = "dailyMenuId")
    private int dailyMenuId;

    @ColumnInfo(name = "imageUrl")
    @NonNull
    private String imageUrl;

    @ColumnInfo(name = "procedure")
    @NonNull
    private String procedure;

    public Recipe(int dailyMenuId, @NonNull String imageUrl, @NonNull String procedure) {
        this.dailyMenuId = dailyMenuId;
        this.imageUrl = imageUrl;
        this.procedure = procedure;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDailyMenuId() {
        return dailyMenuId;
    }

    public void setDailyMenuId(int dailyMenuId) {
        this.dailyMenuId = dailyMenuId;
    }

    @NonNull
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(@NonNull String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @NonNull
    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(@NonNull String procedure) {
        this.procedure = procedure;
    }
}
