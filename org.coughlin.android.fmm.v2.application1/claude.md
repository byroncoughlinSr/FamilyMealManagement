# FamilyMealManagement

An Android application for family meal planning and grocery list management.

## Project Overview

**Package:** `org.coughlin.grocerylist`  
**Min SDK:** 26 (Android 8.0)  
**Target SDK:** 35 (Android 15)  
**Language:** Java  
**Build System:** Gradle  

## Architecture

### Data Layer
- **Room Database** (v2.8.4) - Local persistence
- **Content Provider** (`GroceryContentProvider`) - Data sharing between components
- **DAOs:** `DailyMenuDao`, `ProductDao`, `HistoryDao`
- **Repositories:** `DailyMenuRepository`, `GroceryListRepository`, `ProductRepository`, `HistoryRepository`

### Key Tables
- `tblProduct` - Grocery products with selected/checked states
- `tblDailyMenu` - Daily menu items
- `tblHistory` - Product purchase history

### Entity/Model Classes
- `Product`, `DailyMenu`, `ProductHistory`, `ProductHistoryDetail`
- `Menulist`, `Historylist`, `BackgroundContainer`

### UI Layer
- **Activities:** 
  - `GrocerylistActivity` (main launcher)
  - `MenuActivity`
  - `ProductActivity`
  - `HistoryActivity`
  - `SearchActivity`
  - `MenuItemActivity`
- **Fragments:** `DailyMenuFragment`
- **Adapters:** `ProductAdapter`, `SearchAdapter`, `HistoryAdapter`, `DrawerAdapter`, `DailyMenuPagerAdapter`, `GrocerylistDatabaseAdapter`, `DatabaseAdapter`, `StableArrayAdapter`
- **ViewModels:** `GroceryListViewModel`, `DailyMenuViewModel`, `HistoryViewModel`, `ProductViewModel`

### Utility/Helper Classes
- `DatabaseHelper` - Database migration helper
- `FamilyMealContracts` - Contract constants
- `DrawerHandler` - Navigation drawer management
- `GroceryItemTouchHelperCallback`, `HistoryTouchHelperCallback` - Swipe/touch gesture handlers

### Background Processing
- **WorkManager** (v2.11.1) - `MenuGenerationWorker` for scheduled menu generation
- **MenuGenerationScheduler** - Manages scheduled background tasks

## Dependencies

- AndroidX AppCompat, ConstraintLayout, CardView, DrawerLayout
- AndroidX ViewPager2
- Google Material Design
- AndroidX Room (runtime + common + compiler)
- AndroidX Work Runtime

## Build

```bash
cd org.coughlin.android.fmm.v2.application1
./gradlew assembleDebug
```

## Key Features

1. **Grocery List Management** - Create and manage grocery items with check-off functionality
2. **Daily Menu Planning** - Plan meals for each day with different meal types (breakfast, lunch, dinner)
3. **Product History** - Track previously purchased products
4. **Search** - Search across products and menu items
5. **Navigation Drawer** - Side menu for quick access to all features

## Notes

- App uses MVVM architecture with ViewModels and LiveData
- Network security config allows internet access for future API integration
- Content provider authority: `org.coughlin.provider.grocery`
