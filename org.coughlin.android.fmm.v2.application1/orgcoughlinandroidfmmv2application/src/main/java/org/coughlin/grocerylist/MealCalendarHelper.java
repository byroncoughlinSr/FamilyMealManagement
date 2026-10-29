package org.coughlin.grocerylist;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Shares meals to Google Calendar.
 *
 * Single meals go through the calendar app's insert screen (no permission needed).
 * A whole week is written straight to a chosen Google calendar through
 * CalendarContract, which needs READ/WRITE_CALENDAR. Events written that way are
 * tagged with EVENT_MARKER so re-sharing a week replaces them instead of duplicating.
 */
public final class MealCalendarHelper {
    private static final String TAG = "MealCalendarHelper";
    private static final String GOOGLE_CALENDAR_PACKAGE = "com.google.android.calendar";
    private static final String GOOGLE_ACCOUNT_TYPE = "com.google";
    private static final String EVENT_MARKER = "[Family Meals]";
    private static final int MEAL_DURATION_MINUTES = 60;
    private static final DateTimeFormatter DB_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private MealCalendarHelper() {
    }

    /** A Google calendar the user can write meal events to. */
    public static final class CalendarChoice {
        public final long id;
        public final String name;
        public final String account;

        CalendarChoice(long id, String name, String account) {
            this.id = id;
            this.name = name;
            this.account = account;
        }
    }

    /** Opens the calendar app's "new event" screen prefilled with this meal. */
    public static boolean addMealToCalendar(Context context, DailyMenu meal) {
        long start = startMillis(meal);
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis(start))
                .putExtra(CalendarContract.Events.TITLE, eventTitle(meal))
                .putExtra(CalendarContract.Events.DESCRIPTION, meal.getMealDescription())
                .putExtra(CalendarContract.Events.AVAILABILITY,
                        CalendarContract.Events.AVAILABILITY_FREE);

        // Prefer Google Calendar, but fall back to whatever calendar app is installed.
        Intent googleIntent = new Intent(intent).setPackage(GOOGLE_CALENDAR_PACKAGE);
        try {
            context.startActivity(googleIntent);
            return true;
        } catch (ActivityNotFoundException ignored) {
        }
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    /** Google calendars on the device that the user can add events to. Requires READ_CALENDAR. */
    public static List<CalendarChoice> getWritableGoogleCalendars(Context context) {
        List<CalendarChoice> calendars = new ArrayList<>();
        String[] projection = {
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.ACCOUNT_NAME
        };
        String selection = CalendarContract.Calendars.ACCOUNT_TYPE + " = ? AND "
                + CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL + " >= ? AND "
                + CalendarContract.Calendars.VISIBLE + " = 1";
        String[] args = {
                GOOGLE_ACCOUNT_TYPE,
                String.valueOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR)
        };
        try (Cursor cursor = context.getContentResolver().query(
                CalendarContract.Calendars.CONTENT_URI, projection, selection, args,
                CalendarContract.Calendars.IS_PRIMARY + " DESC, "
                        + CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)) {
            if (cursor == null) return calendars;
            while (cursor.moveToNext()) {
                calendars.add(new CalendarChoice(
                        cursor.getLong(0), cursor.getString(1), cursor.getString(2)));
            }
        }
        return calendars;
    }

    public static boolean hasCalendarPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Adds the meals in [startDate, endDate] to the saved calendar (or the primary Google
     * calendar if none was picked) when auto-sync is on and permission was granted.
     * Best-effort for background use; call off the main thread.
     *
     * @return the number of events written, or -1 if skipped or failed
     */
    public static int autoSyncRange(Context context, LocalDate startDate, LocalDate endDate) {
        try {
            long calendarId = autoSyncCalendarId(context);
            if (calendarId < 0) return -1;
            List<DailyMenu> meals = GroceryListDatabase.getDatabase(context).dailyMenuDao()
                    .getMealsInRangeSync(startDate.format(DB_FORMATTER),
                            endDate.format(DB_FORMATTER));
            return syncMealsToCalendar(context, calendarId, startDate, endDate, meals);
        } catch (RuntimeException e) {
            Log.w(TAG, "Calendar auto-sync failed", e);
            return -1;
        }
    }

    /**
     * Brings one meal slot's calendar event in line with the database after an edit:
     * replaces this app's event for that date and meal type, or removes it if the meal
     * was cleared. No-op unless auto-sync is on and permission was granted.
     * Call off the main thread, after the database write.
     */
    public static void autoSyncMeal(Context context, String date, String mealType) {
        try {
            long calendarId = autoSyncCalendarId(context);
            if (calendarId < 0) return;
            ZoneId zone = ZoneId.systemDefault();
            LocalDate day = LocalDate.parse(date, DB_FORMATTER);
            context.getContentResolver().delete(CalendarContract.Events.CONTENT_URI,
                    CalendarContract.Events.CALENDAR_ID + " = ? AND "
                            + CalendarContract.Events.DTSTART + " >= ? AND "
                            + CalendarContract.Events.DTSTART + " < ? AND "
                            + CalendarContract.Events.TITLE + " LIKE ? AND "
                            + CalendarContract.Events.DESCRIPTION + " LIKE ?",
                    new String[]{
                            String.valueOf(calendarId),
                            String.valueOf(day.atStartOfDay(zone).toInstant().toEpochMilli()),
                            String.valueOf(day.plusDays(1).atStartOfDay(zone)
                                    .toInstant().toEpochMilli()),
                            mealLabel(mealType) + ":%",
                            "%" + EVENT_MARKER + "%"
                    });
            DailyMenu meal = GroceryListDatabase.getDatabase(context).dailyMenuDao()
                    .getMealByDateAndTypeSync(date, mealType);
            if (meal != null) {
                insertEvent(context.getContentResolver(), calendarId, meal);
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "Calendar sync for " + date + " " + mealType + " failed", e);
        }
    }

    /** Saved calendar if still available, else the primary Google calendar; -1 if off. */
    private static long autoSyncCalendarId(Context context) {
        if (!AppSettings.isCalendarAutoSync(context) || !hasCalendarPermission(context)) {
            return -1;
        }
        List<CalendarChoice> calendars = getWritableGoogleCalendars(context);
        if (calendars.isEmpty()) return -1;
        long savedId = AppSettings.getCalendarId(context);
        for (CalendarChoice choice : calendars) {
            if (choice.id == savedId) return savedId;
        }
        return calendars.get(0).id; // primary sorts first
    }

    /**
     * Replaces this app's meal events in [startDate, endDate] on the given calendar with
     * the supplied meals. Requires READ/WRITE_CALENDAR; call off the main thread.
     *
     * @return the number of events written
     */
    public static int syncMealsToCalendar(Context context, long calendarId,
                                          LocalDate startDate, LocalDate endDate,
                                          List<DailyMenu> meals) {
        ContentResolver resolver = context.getContentResolver();
        ZoneId zone = ZoneId.systemDefault();
        long rangeStart = startDate.atStartOfDay(zone).toInstant().toEpochMilli();
        long rangeEnd = endDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli();

        resolver.delete(CalendarContract.Events.CONTENT_URI,
                CalendarContract.Events.CALENDAR_ID + " = ? AND "
                        + CalendarContract.Events.DTSTART + " >= ? AND "
                        + CalendarContract.Events.DTSTART + " < ? AND "
                        + CalendarContract.Events.DESCRIPTION + " LIKE ?",
                new String[]{
                        String.valueOf(calendarId),
                        String.valueOf(rangeStart),
                        String.valueOf(rangeEnd),
                        "%" + EVENT_MARKER + "%"
                });

        int written = 0;
        for (DailyMenu meal : meals) {
            if (insertEvent(resolver, calendarId, meal)) {
                written++;
            }
        }
        return written;
    }

    private static boolean insertEvent(ContentResolver resolver, long calendarId,
                                       DailyMenu meal) {
        long start = startMillis(meal);
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        values.put(CalendarContract.Events.TITLE, eventTitle(meal));
        values.put(CalendarContract.Events.DESCRIPTION,
                meal.getMealDescription() + "\n\n" + EVENT_MARKER);
        values.put(CalendarContract.Events.DTSTART, start);
        values.put(CalendarContract.Events.DTEND, endMillis(start));
        values.put(CalendarContract.Events.EVENT_TIMEZONE, ZoneId.systemDefault().getId());
        values.put(CalendarContract.Events.AVAILABILITY,
                CalendarContract.Events.AVAILABILITY_FREE);
        values.put(CalendarContract.Events.HAS_ALARM, 0);
        return resolver.insert(CalendarContract.Events.CONTENT_URI, values) != null;
    }

    private static String eventTitle(DailyMenu meal) {
        return mealLabel(meal.getMealType()) + ": " + meal.getMealDescription();
    }

    private static String mealLabel(String mealType) {
        return mealType.isEmpty() ? "Meal"
                : Character.toUpperCase(mealType.charAt(0)) + mealType.substring(1);
    }

    private static long startMillis(DailyMenu meal) {
        LocalDate date = LocalDate.parse(meal.getMenuDate(), DB_FORMATTER);
        return date.atTime(mealTime(meal.getMealType()))
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private static long endMillis(long startMillis) {
        return startMillis + MEAL_DURATION_MINUTES * 60_000L;
    }

    private static LocalTime mealTime(String mealType) {
        switch (mealType) {
            case "breakfast": return LocalTime.of(8, 0);
            case "lunch": return LocalTime.of(12, 0);
            default: return LocalTime.of(18, 0);
        }
    }
}
