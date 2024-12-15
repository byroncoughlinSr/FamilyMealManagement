package org.coughlin.grocerylist;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;


public class ProductHistoryDetail {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "hisDate")
    private String hisDate;

    @ColumnInfo(name = "productId")
    private int productId;

    // Add other fields as needed...

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHisDate() {
        return hisDate;
    }

    public void setHisDate(String hisDate) {
        this.hisDate = hisDate;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }
}
