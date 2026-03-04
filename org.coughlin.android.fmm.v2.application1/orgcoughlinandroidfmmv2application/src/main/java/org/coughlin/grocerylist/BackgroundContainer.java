package org.coughlin.grocerylist;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.core.content.res.ResourcesCompat;

public class BackgroundContainer extends FrameLayout {
    boolean mShowing = false;
    Drawable mShadowedBackground;
    int mOpenAreaTop, mOpenAreaHeight;
    boolean mUpdateBounds = false;

    public BackgroundContainer(Context context) {
        super(context);
        init();
    }

    public BackgroundContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BackgroundContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mShadowedBackground = ResourcesCompat.getDrawable(
                getContext().getResources(), R.drawable.shadowed_background, null
        );
    }

    public void showBackground(int top, int height) {
        setWillNotDraw(false);
        mOpenAreaTop = top;
        mOpenAreaHeight = height;
        mShowing = true;
        mUpdateBounds = true;
        invalidate();  // Trigger a redraw to show the background immediately
    }

    public void hideBackground() {
        setWillNotDraw(true);
        mShowing = false;
        invalidate();  // Clear the background by triggering a redraw
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mShowing && mShadowedBackground != null) {
            if (mUpdateBounds) {
                mShadowedBackground.setBounds(0, 0, getWidth(), mOpenAreaHeight);
                mUpdateBounds = false;  // Bounds only need to be set once when they change
            }
            canvas.save();
            canvas.translate(0, mOpenAreaTop);
            mShadowedBackground.draw(canvas);
            canvas.restore();
        }
    }
}
