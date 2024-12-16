package org.coughlin.grocerylist;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;
@Dao
public interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(ProductHistory history);
    @Query("SELECT * FROM tblHistory ORDER BY hisDate DESC")
    LiveData<List<ProductHistory>> getAllHistory();
    @Query("SELECT tblProduct.proName FROM tblProduct INNER JOIN tblHistory ON tblProduct._id = tblHistory.proId WHERE tblHistory.proId = :productId")
    String getProductNameById(int productId);
    @Query("DELETE FROM tblHistory WHERE _id = :productId")
    void deleteByProductId(int productId);
}
