package org.coughlin.grocerylist;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ProductDao {

    // Insert a product if it does not already exist (based on primary key).
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Product product);

    // Update an existing product
    @Update
    void update(Product product);

    // Mark a product as unselected (proSelected = 0)
    @Query("UPDATE tblProduct SET proChecked = 0 WHERE _id = :id")
    void uncheckProduct(int id);

    @Query("UPDATE tblProduct SET proChecked = 1 WHERE _id = :id")
    void checkProduct(int id);

    // Delete a specific product
    @Delete
    void delete(Product product);

    // Get all products that are selected for the grocery list
    @Query("SELECT * FROM tblProduct WHERE proSelected = 1 ORDER BY proName ASC")
    LiveData<List<Product>> getSelectedProductsLive();
    @Query("SELECT proName FROM tblProduct WHERE proSelected = 1 ORDER BY proName ASC")
    LiveData<List<String>> getSelectedProductsName();

    // Search selected products by a query string (e.g., when user searches in the toolbar)
    @Query("SELECT * FROM tblProduct WHERE proName LIKE :query AND proSelected = 1 ORDER BY proName ASC")
    LiveData<List<Product>> searchSelectedProducts(String query);  // Fetches filtered selected items by search query

    // Fetch a product by its ID
    @Query("SELECT * FROM tblProduct WHERE _id = :id")
    Product getProductById(int id);

    // Fetch a product by its name (useful for adding from search)
    @Query("SELECT * FROM tblProduct WHERE proName = :productName")
    List<Product> getProductByName(String productName);

    // Fetch all products, useful for initial loading and displaying all products
    @Query("SELECT * FROM tblProduct ORDER BY proName ASC")
    List<Product> getAllProductsLive();
}
