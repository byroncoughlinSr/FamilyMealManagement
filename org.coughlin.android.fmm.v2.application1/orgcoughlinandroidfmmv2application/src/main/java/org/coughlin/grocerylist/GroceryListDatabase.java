package org.coughlin.grocerylist;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Product.class}, version = 1, exportSchema = false)
public abstract class GroceryListDatabase extends RoomDatabase {

    public abstract ProductDao productDao();

    private static volatile GroceryListDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // Method to get the database instance
    public static GroceryListDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (GroceryListDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    GroceryListDatabase.class, "grocery_list_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
