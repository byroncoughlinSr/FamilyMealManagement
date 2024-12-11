package org.coughlin.grocerylist;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Product.class, ProductHistory.class}, version = 1, exportSchema = false)
public abstract class GroceryListDatabase extends RoomDatabase {
    // Define the executor as an ExecutorService for background operations
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4); // Adjust thread count as needed

    public abstract ProductDao productDao();
    public abstract HistoryDao historyDao();

    private static volatile GroceryListDatabase INSTANCE;
    private static final String DATABASE_NAME = "dbFamilyMeal";

    public static GroceryListDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (GroceryListDatabase.class) {
                if (INSTANCE == null) {
                    // Ensure database copy is completed before Room initializes
                    DatabaseHelper dbHelper = new DatabaseHelper(context);
                    try {
                        dbHelper.createDatabase(context); // Copy the prebuilt database
                    } catch (IOException e) {
                        throw new RuntimeException("Error copying database from assets", e);
                    }

                    // Initialize Room database
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    GroceryListDatabase.class, DATABASE_NAME)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
