package org.coughlin.grocerylist;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "tblRecipeIngredient",
        indices = {@Index("recipeId")},
        foreignKeys = @ForeignKey(
                entity = Recipe.class,
                parentColumns = "_id",
                childColumns = "recipeId",
                onDelete = ForeignKey.CASCADE))
public class RecipeIngredient {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private int id;

    @ColumnInfo(name = "recipeId")
    private int recipeId;

    @ColumnInfo(name = "name")
    @NonNull
    private String name;

    @ColumnInfo(name = "quantity")
    @NonNull
    private String quantity;

    public RecipeIngredient(int recipeId, @NonNull String name, @NonNull String quantity) {
        this.recipeId = recipeId;
        this.name = name;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(@NonNull String quantity) {
        this.quantity = quantity;
    }
}
