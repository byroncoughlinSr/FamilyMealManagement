package org.coughlin.grocerylist;
public class ProductHistoryDetail {
    public String productName;
    public String productDate;

    public ProductHistoryDetail(String productName, String productDate) {
        this.productName = productName;
        this.productDate = productDate;
    }
    public String getProductName() {
        return productName;
    }
    public String getProductDate() {
        return productDate;
    }

    public void setProductDate(String productDate) {
        this.productDate = productDate;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }
}

