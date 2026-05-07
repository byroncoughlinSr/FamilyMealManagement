# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**FamilyMealManagement** — Android app for family meal planning and grocery list management.

- **Package:** `org.coughlin.grocerylist`
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

### Navigation

`DrawerHandler` manages the side navigation drawer present in most activities. The drawer provides access to all major sections.

`GroceryContentProvider` (authority: `org.coughlin.provider.grocery`) exposes grocery data for inter-component sharing.