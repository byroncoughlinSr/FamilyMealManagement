package org.coughlin.grocerylist;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Minimal async loader for locally-stored images. Decodes a bitmap off the
 * main thread and applies it to an ImageView, guarding against view recycling
 * (e.g. inside a ViewPager2) by tagging the target with the requested path.
 */
public class LocalImageLoader {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final int TARGET_SIZE = 1024;

    /**
     * Loads {@code filePath} into {@code target}. If the path is empty or the
     * file is missing, the view is hidden.
     */
    static void load(ImageView target, String filePath) {
        if (filePath == null || filePath.isEmpty() || !new File(filePath).exists()) {
            target.setTag(null);
            target.setImageDrawable(null);
            target.setVisibility(View.GONE);
            return;
        }

        target.setTag(filePath);
        EXECUTOR.execute(() -> {
            final Bitmap bitmap = decodeSampled(filePath);
            MAIN.post(() -> {
                // Only apply if this view still wants this image (not recycled).
                if (!filePath.equals(target.getTag())) {
                    return;
                }
                if (bitmap != null) {
                    target.setImageBitmap(bitmap);
                    target.setVisibility(View.VISIBLE);
                } else {
                    target.setImageDrawable(null);
                    target.setVisibility(View.GONE);
                }
            });
        });
    }

    private static Bitmap decodeSampled(String filePath) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, bounds);

            int sample = 1;
            int largest = Math.max(bounds.outWidth, bounds.outHeight);
            while (largest / sample > TARGET_SIZE) {
                sample *= 2;
            }

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sample;
            return BitmapFactory.decodeFile(filePath, opts);
        } catch (Exception e) {
            return null;
        }
    }
}
