package org.coughlin.grocerylist;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MenuGenerationScheduler {

    private static final String WORK_NAME = "weekly_menu_generation";

    public static void scheduleWeeklyMenuGeneration(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        int day = AppSettings.getScheduleDay(context);
        int hour = AppSettings.getScheduleHour(context);
        int minute = AppSettings.getScheduleMinute(context);
        long initialDelay = calculateDelayToNext(day, hour, minute);

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                MenuGenerationWorker.class, 7, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build();

        // CANCEL_AND_REENQUEUE is the standard way to force-update a periodic work schedule
        // if its configuration (like delay or interval) has changed.
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                workRequest
        );
    }

    private static long calculateDelayToNext(int targetDayOfWeek, int targetHour, int targetMinute) {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();

        target.set(Calendar.DAY_OF_WEEK, targetDayOfWeek);
        target.set(Calendar.HOUR_OF_DAY, targetHour);
        target.set(Calendar.MINUTE, targetMinute);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        if (target.before(now) || target.equals(now)) {
            target.add(Calendar.WEEK_OF_YEAR, 1);
        }

        return target.getTimeInMillis() - now.getTimeInMillis();
    }
}
