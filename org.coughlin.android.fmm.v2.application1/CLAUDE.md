# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**FamilyMealManagement** — Android app for family meal planning and grocery list management.

- **Package:** `org.coughlin.grocerylist` (Java namespace)
- **applicationId:** `org.coughlin.familymeals.planner` (Play Store listing "Family Meals: AI Meal Planner"; the old `org.coughlin.familymeals` belonged to a closed Play account and can't be reused)
- **Language:** Java
- **Min SDK:** 26 (Android 8.0) / **Target SDK:** 35 (Android 15)
- **Build System:** Gradle with Android Gradle Plugin

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean
```

No test framework is currently configured. There are no unit or instrumentation test files.

## Architecture

MVVM + Repository pattern with LiveData throughout.

### Data Layer

**Room Database** (`GroceryListDatabase`, DB name: `dbFamilyMeal`, current version: 5)

Entities and their tables:
- `Product` → `tblProduct` — grocery items (name, selected, checked states)
- `DailyMenu` → `tblDailyMenu` — meal slots (date, mealType: breakfast/lunch/dinner, mealDescription, manuallySet)
- `ProductHistory` → `tblHistory` — purchase history
- `Recipe` → `tblRecipe` — recipe details (imageUrl, procedure), FK → DailyMenu CASCADE DELETE
- `RecipeIngredient` → `tblRecipeIngredient` — ingredients (name, quantity), FK → Recipe CASCADE DELETE

Migrations exist from v1 through v5. When adding new columns, always add a migration in `GroceryListDatabase`.

DAOs: `ProductDao`, `DailyMenuDao`, `HistoryDao`, `RecipeDao`, `RecipeIngredientDao`

Repositories: `ProductRepository`, `DailyMenuRepository`, `GroceryListRepository`, `HistoryRepository`

### UI Layer

Activities (launcher: `GrocerylistActivity`):
- `GrocerylistActivity` — grocery list with swipe gestures and navigation drawer
- `MenuActivity` — weekly meal view using ViewPager2 (one page per day)
- `ProductActivity`, `HistoryActivity`, `SearchActivity`, `MealSearchActivity`
- `MenuItemActivity` — view/edit a single meal slot
- `CreateRecipeActivity`, `RecipeActivity` — recipe creation and viewing
- `SettingsActivity` — Ollama API config, prompts, and schedule settings

Fragment: `DailyMenuFragment` — renders a single day's meals inside `MenuActivity`'s ViewPager2.

ViewModels: `GroceryListViewModel`, `DailyMenuViewModel`, `HistoryViewModel`, `ProductViewModel`, `RecipeViewModel`

### Background Processing

**WorkManager** handles all background tasks.

- `MenuGenerationWorker` — calls Ollama API to generate meal descriptions for empty meal slots in a date range; only fills slots where `manuallySet = false`; chains `RecipeGenerationWorker` on completion
- `RecipeGenerationWorker` — calls Ollama API to generate recipe procedure, image URL, and ingredients for each meal
- `MenuGenerationScheduler` — schedules a weekly `MenuGenerationWorker` using day/time from `AppSettings`

Workers use `java.net.HttpURLConnection` directly (no Retrofit/OkHttp). JSON parsing uses the built-in `org.json` library. Threading uses `java.util.concurrent.Executors`.

### Settings / Configuration

`AppSettings` wraps `SharedPreferences` and exposes:
- Ollama server URL (default: `http://192.168.4.249:11434/api/generate`)
- Ollama model name (default: `llama3.1:8b`)
- Menu generation prompt
- Recipe generation prompt
- Schedule day and time for automatic generation

`SettingsActivity` is the UI for all of the above and also provides a connection test button.

Network security config (`res/xml/network_security_config.xml`) explicitly permits cleartext HTTP to the local Ollama host.

### Grocery List Sharing

The share icon in `GrocerylistActivity`'s toolbar (`R.id.action_share_grocery_list`, `res/menu/grocerylist.xml`, `res/drawable/ic_share.xml`) sends the list as a plain-text shopping list through the Android share sheet, like the camera's share button.

- `GroceryListViewModel.loadItemsToBuy()` reads `productDao.getSelectedProductsSync()` on `databaseWriteExecutor` and posts the result to the main thread.
- `ShoppingListFormatter` (plain Java, no Android deps) keeps items with `proSelected = 1` and `proChecked = 0` (on the list, not checked off), trims names, and drops blanks. It formats them as a title (`Shopping List – Mon, Nov 23`), one `☐ item` line each, and a `Sent from Family Meals` footer.
- `GrocerylistActivity.shareShoppingList()` sends `Intent.ACTION_SEND` (`text/plain`) with `EXTRA_TEXT`, plus the title as `EXTRA_SUBJECT` (email subject), wrapped in `Intent.createChooser`. No permissions or `FileProvider` are needed.
- If nothing is left to buy, it shows a toast (`R.string.share_list_nothing_to_buy`) instead of opening the chooser.
- User instructions are in the Help screen (`activity_help.xml`, "Sharing Your Shopping List", text in `strings.xml`) and in the root `README.md`.

### Navigation

`DrawerHandler` manages the side navigation drawer present in most activities. The drawer provides access to all major sections.

`GroceryContentProvider` (authority: `org.coughlin.familymeals.planner.provider.grocery`, i.e. `${applicationId}.provider.grocery`; must stay unique because the old `org.coughlin.familymeals` app used `org.coughlin.provider.grocery`) exposes grocery data for inter-component sharing.