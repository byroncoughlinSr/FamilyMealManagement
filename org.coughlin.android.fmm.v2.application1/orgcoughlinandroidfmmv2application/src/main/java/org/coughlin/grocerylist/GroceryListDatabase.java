package org.coughlin.grocerylist;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Product.class, ProductHistory.class, DailyMenu.class}, version = 3, exportSchema = false)
public abstract class GroceryListDatabase extends RoomDatabase {

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);
    public abstract ProductDao productDao();
    public abstract HistoryDao historyDao();
    public abstract DailyMenuDao dailyMenuDao();
    private static volatile GroceryListDatabase INSTANCE;
    private static final String DATABASE_NAME = "dbFamilyMeal";
    public static GroceryListDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (GroceryListDatabase.class) {
                if (INSTANCE == null) {
                    try {
                        DatabaseHelper dbHelper = new DatabaseHelper(context);
                        dbHelper.createDatabase();
                        INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                        GroceryListDatabase.class, DATABASE_NAME)
                                .createFromAsset("databases/" + DATABASE_NAME) // Load prebuilt DB
                                .addCallback(prepopulateCallback()) // Optional: Additional setup after DB is created
                                .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // Add migrations
                                .build();
                    } catch (IOException e) {
                        throw new RuntimeException("Error copying database from assets", e);
                    }
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Define migrations for schema updates.
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE tblHistory_new (" +
                    "_id INTEGER NOT NULL PRIMARY KEY, " +
                    "proId INTEGER NOT NULL, " +
                    "hisDate TEXT NOT NULL)");
            database.execSQL("INSERT INTO tblHistory_new (_id, proId, hisDate) " +
                    "SELECT _id, proId, hisDate FROM tblHistory");
            database.execSQL("DROP TABLE tblHistory");
            database.execSQL("ALTER TABLE tblHistory_new RENAME TO tblHistory");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS tblDailyMenu (" +
                    "_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "menuDate TEXT NOT NULL, " +
                    "mealType TEXT NOT NULL, " +
                    "mealDescription TEXT NOT NULL)");
        }
    };

    /**
     * Optional: Callback to run code after the database is created.
     * Useful for inserting default data or performing initial setup.
     */
    private static RoomDatabase.Callback prepopulateCallback() {
        return new RoomDatabase.Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);
                // Prepopulate data if needed
                databaseWriteExecutor.execute(() -> {
                    GroceryListDatabase database = INSTANCE;
                    if (database != null) {
                        ProductDao productDao = database.productDao();
                    }
                });
            }
        };
    }
}
