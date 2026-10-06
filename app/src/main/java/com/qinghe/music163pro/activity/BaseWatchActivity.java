package com.qinghe.music163pro.activity;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.qinghe.music163pro.R;
import com.qinghe.music163pro.util.RotaryInputHelper;
import com.qinghe.music163pro.util.WatchUiUtils;

/**
 * Base activity for shared watch UI behavior:
 *  - keep-screen-on preference
 *  - right-swipe-to-go-back gesture (matches the pattern already used by
 *    MainActivity/MoreActivity, centralized here so every other page gets it
 *    for free)
 *  - physical crown (rotary encoder) scrolling of whatever list/ScrollView is
 *    on screen, e.g. on an OPPO Watch 3 Pro
 */
public abstract class BaseWatchActivity extends AppCompatActivity {

    private GestureDetector swipeBackDetector;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WatchUiUtils.applyKeepScreenOnPreference(this);
        swipeBackDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null || !isSwipeBackEnabled()) return false;
                float diffX = e2.getX() - e1.getX();
                float diffY = Math.abs(e2.getY() - e1.getY());
                if (diffX > 80 && diffY < 200 && Math.abs(velocityX) > 200) {
                    finish();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Subclasses with their own dispatchTouchEvent-based gesture handling (or
     * that shouldn't be swipe-dismissible, e.g. a root/launcher screen) can
     * override this to return false.
     */
    protected boolean isSwipeBackEnabled() {
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (swipeBackDetector != null) {
            swipeBackDetector.onTouchEvent(event);
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * Physical crown (rotary encoder) support: turns crown rotation into a
     * scroll of whichever list/ScrollView is currently on screen. Subclasses
     * that need to scroll something more specific (e.g. a lyrics view inside
     * an overlay) should override {@link #rotaryScrollTarget()}.
     */
    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        View content = findViewById(android.R.id.content);
        if (RotaryInputHelper.handleRotaryScroll(this, event, content, rotaryScrollTarget())) {
            return true;
        }
        return super.dispatchGenericMotionEvent(event);
    }

    /**
     * Optional explicit scroll target for crown input. Return null (default)
     * to let {@link RotaryInputHelper} auto-detect the first scrollable view
     * on screen.
     */
    protected View rotaryScrollTarget() {
        return null;
    }

    protected final int px(int baseValue) {
        return WatchUiUtils.px(this, baseValue);
    }

    /**
     * Convert dp to pixels using the device's display density.
     * Unlike px() which scales by screen width ratio, this follows
     * Android's standard dp-to-px conversion, ensuring consistency
     * with XML-defined layout dimensions (e.g., login page buttons at 36dp).
     */
    protected final int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    /**
     * Create a MaterialButton styled for watch screens.
     * Uses dp-based height (matching the XML login buttons at 36dp, 13sp text)
     * to ensure text is always fully visible regardless of screen density.
     */
    protected final MaterialButton createWatchButton(String text, boolean outlined) {
        int styleAttr = outlined
                ? com.google.android.material.R.attr.materialButtonOutlinedStyle
                : com.google.android.material.R.attr.materialButtonStyle;
        MaterialButton button = new MaterialButton(this, null, styleAttr);
        button.setText(text);
        button.setTextColor(getResources().getColor(R.color.text_primary));
        button.setTextSize(13);
        button.setAllCaps(false);
        button.setInsetTop(0);
        button.setInsetBottom(0);
        // Use dp-based minHeight (matching XML button layout_height="36dp")
        int minHeightPx = dpToPx(36);
        button.setMinHeight(minHeightPx);
        button.setMinimumHeight(minHeightPx);
        return button;
    }
}
