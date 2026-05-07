# Room — keep DAOs, entities, and generated implementations
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep class androidx.room.** { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# WorkManager workers (constructed reflectively by name)
-keep class * extends androidx.work.Worker { <init>(...); }
-keep class * extends androidx.work.ListenableWorker { <init>(...); }

# App entities (referenced by Room generated code via reflection on field/getter names)
-keep class org.coughlin.grocerylist.Product { *; }
-keep class org.coughlin.grocerylist.ProductHistory { *; }
-keep class org.coughlin.grocerylist.DailyMenu { *; }
-keep class org.coughlin.grocerylist.Recipe { *; }
-keep class org.coughlin.grocerylist.RecipeIngredient { *; }

# Suppress warnings for AI Edge SDK (added when on-device provider lands)
-dontwarn com.google.ai.edge.**
