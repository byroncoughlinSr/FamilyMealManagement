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

        long initialDelay = calculateDelayToNextFridayMidnight();

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

    private static long calculateDelayToNextFridayMidnight() {
        Calendar now = Calendar.getInstance();
        Calendar nextFridayMidnight = Calendar.getInstance();
        
        // Friday night midnight is technically Saturday at 00:00
        nextFridayMidnight.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        nextFridayMidnight.set(Calendar.HOUR_OF_DAY, 0);
        nextFridayMidnight.set(Calendar.MINUTE, 0);
        nextFridayMidnight.set(Calendar.SECOND, 0);
        nextFridayMidnight.set(Calendar.MILLISECOND, 0);

        if (nextFridayMidnight.before(now) || nextFridayMidnight.equals(now)) {
            nextFridayMidnight.add(Calendar.WEEK_OF_YEAR, 1);
        }

        return nextFridayMidnight.getTimeInMillis() - now.getTimeInMillis();
    }
}
