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
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Product product);
    @Update
    void update(Product product);
    @Delete
    void delete(Product product);
    @Query("UPDATE tblProduct SET proChecked = 1 WHERE _id = :id")
    void checkProduct(int id);
    @Query("UPDATE tblProduct SET proChecked = 0 WHERE _id = :id")
    void uncheckProduct(int id);
    @Query("UPDATE tblProduct SET proSelected = 1 where _id =:id")
    void updateSelectProduct(int id);
    @Query("UPDATE tblProduct SET proSelected = 0 where _id =:id")
    void unSelectProduct(int id);
    @Query("SELECT * FROM tblProduct WHERE _id = :id")
    LiveData<Product> getProductById(int id);
    @Query("SELECT * FROM tblProduct WHERE _id = :id")
    Product getProductByIdSync(int id);
    @Query("SELECT * FROM tblProduct WHERE proName = :productName")
    LiveData<List<Product>> getProductByName(String productName);
    @Query("SELECT * FROM tblProduct WHERE proName = :productName LIMIT 1")
    Product getProductByNameSync(String productName);
    
    @Query("SELECT * FROM tblProduct WHERE LOWER(proName) = LOWER(:productName) LIMIT 1")
    Product getProductByNameIgnoreCaseSync(String productName);

    @Query("SELECT * FROM tblProduct ORDER BY proName ASC")
    LiveData<List<Product>> getAllProducts();
    @Query("SELECT * FROM tblProduct ORDER BY proName ASC")
    List<Product> getAllProductsSync();
    @Query("SELECT * FROM tblProduct WHERE proSelected = 1 ORDER BY proName ASC")
    LiveData<List<Product>> getSelectedProducts();
    @Query("SELECT * FROM tblProduct WHERE proSelected = 1 ORDER BY proName ASC")
    List<Product> getSelectedProductsSync();

    @Query("SELECT * FROM tblProduct WHERE proName LIKE :query ORDER BY proName ASC")
    List<Product> searchSelectedProducts(String query);

    @Query("SELECT * FROM tblProduct WHERE proName LIKE :query ORDER BY proName ASC LIMIT 20")
    List<Product> searchProductsByName(String query);
}
