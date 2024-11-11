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
    @Query("SELECT * FROM tblProduct ORDER BY proName ASC")
    LiveData<List<Product>> getAllProductsLive();
    @Query("SELECT * FROM tblProduct WHERE _id = :id")
    Product getProductById(int id);
    @Query("DELETE FROM tblProduct")
    void deleteAll();
}
