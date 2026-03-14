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

@Database(entities = {Product.class, ProductHistory.class, DailyMenu.class, Recipe.class, RecipeIngredient.class}, version = 5, exportSchema = false)
public abstract class GroceryListDatabase extends RoomDatabase {

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);
    public abstract ProductDao productDao();
    public abstract HistoryDao historyDao();
    public abstract DailyMenuDao dailyMenuDao();
    public abstract RecipeDao recipeDao();
    public abstract RecipeIngredientDao recipeIngredientDao();
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
                                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5) // Add migrations
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

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS tblRecipe (" +
                    "_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "dailyMenuId INTEGER NOT NULL, " +
                    "imageUrl TEXT NOT NULL DEFAULT '', " +
                    "procedure TEXT NOT NULL DEFAULT '', " +
                    "FOREIGN KEY(dailyMenuId) REFERENCES tblDailyMenu(_id) ON DELETE CASCADE)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_tblRecipe_dailyMenuId ON tblRecipe(dailyMenuId)");
            database.execSQL("CREATE TABLE IF NOT EXISTS tblRecipeIngredient (" +
                    "_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "recipeId INTEGER NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "quantity TEXT NOT NULL, " +
                    "FOREIGN KEY(recipeId) REFERENCES tblRecipe(_id) ON DELETE CASCADE)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_tblRecipeIngredient_recipeId ON tblRecipeIngredient(recipeId)");
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tblDailyMenu ADD COLUMN manuallySet INTEGER NOT NULL DEFAULT 0");
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
