package org.coughlin.grocerylist;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Turns the grocery list into a plain-text shopping list for the share sheet.
 *
 * Only items still on the list ({@code proSelected}) and not yet checked off
 * ({@code proChecked}) are included, so the shared list shows what is left to buy.
 * Pure Java with no Android dependencies, so it is easy to unit test.
 */
public final class ShoppingListFormatter {

    static final String BOX = "☐ ";   // ☐ empty ballot box

    private ShoppingListFormatter() { }

    /** Grocery list items that still need to be bought, in the order given. */
    @NonNull
    public static List<String> itemsToBuy(List<Product> products) {
        List<String> items = new ArrayList<>();
        if (products == null) return items;
        for (Product p : products) {
            if (p != null && p.isSelected() && !p.isChecked()) {
                String name = p.getName().trim();
                if (!name.isEmpty()) items.add(name);
            }
        }
        return items;
    }

    /** "Shopping List – Mon, Nov 23" in the device's language. */
    @NonNull
    public static String title(@NonNull Date date, @NonNull Locale locale) {
        DateFormat fmt = new SimpleDateFormat("EEE, MMM d", locale);
        return "Shopping List – " + fmt.format(date);
    }

    /** The full message: title, one "☐ item" line per item, and a footer. */
    @NonNull
    public static String format(@NonNull List<String> items, @NonNull Date date, @NonNull Locale locale) {
        StringBuilder sb = new StringBuilder(title(date, locale)).append("\n\n");
        for (String item : items) sb.append(BOX).append(item).append('\n');
        sb.append("\nSent from Family Meals");
        return sb.toString();
    }
}
