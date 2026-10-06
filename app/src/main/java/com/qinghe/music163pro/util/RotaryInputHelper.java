package com.qinghe.music163pro.util;

import android.content.Context;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Helper for handling physical crown / rotary-encoder input (e.g. the OPPO
 * Watch 3 Pro's rotating crown).
 *
 * Rotary rotation is delivered by the Android input framework as a generic
 * MotionEvent with source {@link InputDevice#SOURCE_ROTARY_ENCODER} and the
 * rotation amount on {@link MotionEvent#AXIS_SCROLL} — this is the standard
 * AOSP mechanism used across rotary-equipped Android watches (adopted by
 * ColorOS Watch, since it is "directly based on Android" rather than a
 * closed platform). We turn that into a scroll on whatever scrollable view
 * is on screen, so every list/ScrollView-based page in the app responds to
 * the crown the same way it responds to a swipe.
 *
 * NOTE: the sign of AXIS_SCROLL (which rotation direction is "positive") can
 * differ slightly by OEM. If crown scrolling feels reversed on a real OPPO
 * Watch 3 Pro, flip {@link #DIRECTION} from 1 to -1.
 */
public final class RotaryInputHelper {

    /** Flip to -1 if crown rotation scrolls the wrong way on-device. */
    private static final int DIRECTION = 1;

    private RotaryInputHelper() {
    }

    /**
     * Call this from an Activity's dispatchGenericMotionEvent(). Returns true
     * if the event was a rotary scroll and was consumed.
     *
     * @param rootView the activity's current content view (e.g. the view
     *                 passed to setContentView, or the decor view), used to
     *                 search for a scrollable target when preferredTarget is
     *                 null or not currently visible/scrollable.
     */
    public static boolean handleRotaryScroll(Context context, MotionEvent event, View rootView) {
        return handleRotaryScroll(context, event, rootView, null);
    }

    /**
     * @param preferredTarget an explicit view to scroll (e.g. the lyrics
     *                        ScrollView while a lyrics overlay is showing),
     *                        tried before falling back to searching rootView.
     */
    public static boolean handleRotaryScroll(Context context, MotionEvent event, View rootView,
                                              View preferredTarget) {
        if (event.getAction() != MotionEvent.ACTION_SCROLL) {
            return false;
        }
        if (!event.isFromSource(InputDevice.SOURCE_ROTARY_ENCODER)) {
            return false;
        }

        float scrollAxis = event.getAxisValue(MotionEvent.AXIS_SCROLL);
        int pixelDelta = (int) (scrollAxis * getScrollFactor(context) * DIRECTION);
        if (pixelDelta == 0) {
            return false;
        }

        View target = (preferredTarget != null && preferredTarget.getVisibility() == View.VISIBLE)
                ? preferredTarget
                : findScrollableView(rootView);

        return applyScroll(target, pixelDelta);
    }

    private static float getScrollFactor(Context context) {
        // getScaledVerticalScrollFactor needs API 26; fall back to a sensible
        // fixed pixel-per-"click" value on older API levels.
        try {
            return ViewConfiguration.get(context).getScaledVerticalScrollFactor();
        } catch (Throwable t) {
            return context.getResources().getDisplayMetrics().density * 24f;
        }
    }

    private static boolean applyScroll(View target, int pixelDelta) {
        if (target == null) {
            return false;
        }
        if (target instanceof RecyclerView) {
            ((RecyclerView) target).scrollBy(0, pixelDelta);
            return true;
        }
        if (target instanceof AbsListView) {
            ((AbsListView) target).smoothScrollBy(pixelDelta, 0);
            return true;
        }
        if (target instanceof NestedScrollView) {
            target.scrollBy(0, pixelDelta);
            return true;
        }
        if (target instanceof ScrollView) {
            target.scrollBy(0, pixelDelta);
            return true;
        }
        if (target instanceof HorizontalScrollView) {
            target.scrollBy(pixelDelta, 0);
            return true;
        }
        return false;
    }

    /**
     * Depth-first search of the view tree for the first visible scrollable
     * container (ScrollView/NestedScrollView/ListView/RecyclerView/...).
     */
    public static View findScrollableView(View view) {
        if (view == null || view.getVisibility() != View.VISIBLE) {
            return null;
        }
        if (view instanceof RecyclerView
                || view instanceof AbsListView
                || view instanceof NestedScrollView
                || view instanceof ScrollView
                || view instanceof HorizontalScrollView) {
            return view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View found = findScrollableView(group.getChildAt(i));
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
