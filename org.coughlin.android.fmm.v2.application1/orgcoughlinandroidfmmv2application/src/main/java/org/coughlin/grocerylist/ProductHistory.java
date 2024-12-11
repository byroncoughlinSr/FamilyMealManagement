package org.coughlin.grocerylist;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "tblHistory") // Matches the table name
public class ProductHistory {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    private int id;
    @ColumnInfo(name = "hisDate")
    @NonNull
    private String hisDate;
    @ColumnInfo(name = "proId")
    private int proId;
    // Constructor
    public ProductHistory(@NonNull String hisDate, int proId) {
        this.hisDate = hisDate;
        this.proId = proId;
    }
    // Getters and Setters
    @NonNull
    public String getHisDate() {
        return hisDate;
    }
    public void setHisDate(@NonNull String hisDate) {
        this.hisDate = hisDate;
    }
    public int getProId() {
        return proId;
    }
    public void setProId(int proId) {
        this.proId = proId;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
}

