package org.coughlin.grocerylist;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import java.io.IOException;
import java.util.concurrent.Executor;

@Database(entities = {Product.class}, version = 1, exportSchema = false)
public abstract class GroceryListDatabase extends RoomDatabase {
    public static Executor databaseWriteExecutor;

    public abstract ProductDao productDao();

    private static volatile GroceryListDatabase INSTANCE;
    private static final String DATABASE_NAME = "dbFamilyMeal";

    public static GroceryListDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (GroceryListDatabase.class) {
                if (INSTANCE == null) {
                    // Ensure database copy is completed before Room initializes
                    DatabaseHelper dbHelper = new DatabaseHelper(context);
                    try {
                        dbHelper.createDatabase(context);
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
