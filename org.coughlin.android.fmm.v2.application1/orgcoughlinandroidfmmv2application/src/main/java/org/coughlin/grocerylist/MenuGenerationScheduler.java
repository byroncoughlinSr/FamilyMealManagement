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

        long initialDelay = calculateDelayToNextSunday();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                MenuGenerationWorker.class, 7, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
        );
    }

    private static long calculateDelayToNextSunday() {
        Calendar now = Calendar.getInstance();
        Calendar nextSunday = Calendar.getInstance();
        nextSunday.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        nextSunday.set(Calendar.HOUR_OF_DAY, 8);
        nextSunday.set(Calendar.MINUTE, 0);
        nextSunday.set(Calendar.SECOND, 0);
        nextSunday.set(Calendar.MILLISECOND, 0);

        if (nextSunday.before(now) || nextSunday.equals(now)) {
            nextSunday.add(Calendar.WEEK_OF_YEAR, 1);
        }

        return nextSunday.getTimeInMillis() - now.getTimeInMillis();
    }
}
