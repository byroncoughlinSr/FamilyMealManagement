package org.coughlin.grocerylist;

/**
 * Shared logic for keeping the grocery list in sync with recipe ingredients.
 * All methods perform synchronous DB access and must be called off the main thread.
 */
public final class GroceryListHelper {

    private GroceryListHelper() {
    }

    /**
     * Finds the product matching an ingredient name (case-insensitive), falling
     * back to a naive singular form ("Eggs" → "Egg").
     */
    public static Product findProductForIngredient(String ingredientName, ProductDao productDao) {
        if (ingredientName == null) return null;
        String normalized = ingredientName.trim();
        if (normalized.isEmpty()) return null;

        Product product = productDao.getProductByNameIgnoreCaseSync(normalized);
        if (product == null && normalized.toLowerCase().endsWith("s") && normalized.length() > 3) {
            product = productDao.getProductByNameIgnoreCaseSync(
                    normalized.substring(0, normalized.length() - 1));
        }
        return product;
    }

    /** Selects (and unchecks) the matching product, creating it if it doesn't exist. */
    public static void addIngredient(String ingredientName, ProductDao productDao) {
        if (ingredientName == null) return;
        String normalized = ingredientName.trim();
        if (normalized.isEmpty()) return;

        Product existing = findProductForIngredient(normalized, productDao);
        if (existing != null) {
            productDao.updateSelectProduct(existing.getId());
            productDao.uncheckProduct(existing.getId());
        } else {
            String formattedName = normalized.substring(0, 1).toUpperCase() + normalized.substring(1);
            productDao.insert(new Product(formattedName, true, false));
        }
    }

    /** Removes the matching product from the grocery list (keeps it in the catalog). */
    public static void removeIngredient(String ingredientName, ProductDao productDao) {
        Product existing = findProductForIngredient(ingredientName, productDao);
        if (existing != null) {
            productDao.unSelectProduct(existing.getId());
        }
    }
}
