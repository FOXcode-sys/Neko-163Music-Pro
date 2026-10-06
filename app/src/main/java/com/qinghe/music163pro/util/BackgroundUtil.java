package com.qinghe.music163pro.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Background support for the main player and lyrics screens.
 *
 * Modes:
 * - MODE_COVER: background follows the playing (or current) song's album cover.
 *   The cover is kept only in memory (never saved to disk) and is always
 *   (re)loaded when the screen is shown, so it never stays blank.
 * - MODE_CUSTOM: a user-picked static image saved to internal storage.
 * - MODE_DEFAULT: plain dark background.
 */
public final class BackgroundUtil {

    public static final String MODE_COVER = "cover";
    public static final String MODE_CUSTOM = "custom";
    public static final String MODE_DEFAULT = "default";

    private static final String PREFS_NAME = "music163_settings";
    private static final String KEY_MODE = "background_mode";
    private static final String FILE_NAME = "custom_background.jpg";
    private static final int DEFAULT_BG_COLOR = 0xFF121212;
    private static final int SCRIM_ALPHA = 150; // 0..255

    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    // Album-cover background, kept in memory only.
    private static Bitmap sCoverBg;
    private static String sCoverBgUrl;

    private BackgroundUtil() {
    }

    public static String getMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_MODE, MODE_COVER);
    }

    public static void setMode(Context context, String mode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_MODE, mode).apply();
    }

    public static File getBackgroundFile(Context context) {
        return new File(context.getFilesDir(), FILE_NAME);
    }

    public static boolean hasCustomBackground(Context context) {
        return getBackgroundFile(context).exists();
    }

    public static void saveBackground(Context context, Uri uri) {
        if (context == null || uri == null) return;
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            if (is == null) return;
            FileOutputStream fos = new FileOutputStream(getBackgroundFile(context));
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) != -1) fos.write(buf, 0, n);
            fos.close();
            is.close();
        } catch (Exception ignored) {
        }
    }

    public static void clearBackground(Context context) {
        getBackgroundFile(context).delete();
    }

    // ==================== Album cover background (in memory) ====================

    public static Bitmap getCoverBackground() {
        return sCoverBg;
    }

    public static boolean hasCoverBackground(String url) {
        return url != null && !url.isEmpty() && url.equals(sCoverBgUrl) && sCoverBg != null;
    }

    public static void clearCoverBackground() {
        sCoverBg = null;
        sCoverBgUrl = null;
    }

    /**
     * Download the cover and keep a darkened copy in memory (no disk write).
     * onReady runs on the main thread whether it succeeded or failed.
     */
    public static void loadCoverBackground(final Context context, final String url,
                                           final Runnable onReady) {
        if (url == null || url.isEmpty()) {
            clearCoverBackground();
            if (onReady != null) MAIN.post(onReady);
            return;
        }
        if (hasCoverBackground(url)) {
            if (onReady != null) MAIN.post(onReady);
            return;
        }
        final String targetUrl = url;
        final Context appContext = context.getApplicationContext();
        new Thread(() -> {
            Bitmap raw = downloadBitmap(appContext, targetUrl);
            final Bitmap dimmed = raw != null ? dimBitmap(raw, appContext) : null;
            MAIN.post(() -> {
                if (dimmed != null) {
                    sCoverBg = dimmed;
                    sCoverBgUrl = targetUrl;
                } else {
                    clearCoverBackground();
                }
                if (onReady != null) onReady.run();
            });
        }).start();
    }

    private static Bitmap downloadBitmap(Context context, String urlStr) {
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.connect();
            is = conn.getInputStream();
            byte[] data = readAll(is);
            if (data.length == 0) return null;
            int reqW = context.getResources().getDisplayMetrics().widthPixels;
            int reqH = context.getResources().getDisplayMetrics().heightPixels;
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, bounds);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = calculateSampleSize(bounds, reqW, reqH);
            return BitmapFactory.decodeByteArray(data, 0, data.length, opts);
        } catch (Exception ignored) {
            return null;
        } finally {
            try {
                if (is != null) is.close();
            } catch (Exception ignored) {
            }
            if (conn != null) conn.disconnect();
        }
    }

    private static byte[] readAll(InputStream is) throws Exception {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) != -1) out.write(buf, 0, n);
        return out.toByteArray();
    }

    private static Bitmap dimBitmap(Bitmap src, Context context) {
        if (src == null) return null;
        Bitmap dimmed = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(dimmed);
        canvas.drawBitmap(src, 0, 0, null);
        Paint scrim = new Paint();
        scrim.setColor(Color.argb(SCRIM_ALPHA, 0, 0, 0));
        canvas.drawRect(0, 0, dimmed.getWidth(), dimmed.getHeight(), scrim);
        src.recycle();
        return dimmed;
    }

    // ==================== Apply ====================

    public static void applyBackground(Context context, View root) {
        if (root == null) return;
        String mode = getMode(context);
        if (MODE_DEFAULT.equals(mode)) {
            root.setBackgroundColor(DEFAULT_BG_COLOR);
            return;
        }
        if (MODE_COVER.equals(mode)) {
            if (sCoverBg != null) {
                Bitmap cropped = cropToScreenAspect(context, root, sCoverBg);
                root.setBackground(new BitmapDrawable(context.getResources(), cropped));
            } else {
                root.setBackgroundColor(DEFAULT_BG_COLOR);
            }
            return;
        }
        // MODE_CUSTOM
        if (!hasCustomBackground(context)) {
            root.setBackgroundColor(DEFAULT_BG_COLOR);
            return;
        }
        try {
            int reqW = context.getResources().getDisplayMetrics().widthPixels;
            int reqH = context.getResources().getDisplayMetrics().heightPixels;
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(getBackgroundFile(context).getAbsolutePath(), opts);
            opts.inSampleSize = calculateSampleSize(opts, reqW, reqH);
            opts.inJustDecodeBounds = false;
            Bitmap src = BitmapFactory.decodeFile(getBackgroundFile(context).getAbsolutePath(), opts);
            if (src == null) {
                root.setBackgroundColor(DEFAULT_BG_COLOR);
                return;
            }
            Bitmap dimmed = dimBitmap(src, context);
            Bitmap cropped = cropToScreenAspect(context, root, dimmed);
            root.setBackground(new BitmapDrawable(context.getResources(), cropped));
        } catch (Exception ignored) {
            root.setBackgroundColor(DEFAULT_BG_COLOR);
        }
    }

    /**
     * Center-crop a bitmap (typically a square album cover) to match the
     * screen's aspect ratio, so it fills the player/lyrics background exactly
     * like CENTER_CROP would — instead of the stretch/squash that
     * {@link BitmapDrawable} does by default when a mismatched-ratio bitmap
     * is set directly as a View's background. This "locks" the image's
     * visual proportions: content is cropped, never distorted.
     *
     * Prefers the real (already-measured) view size and falls back to the
     * device's screen size, since applyBackground() may run before the view
     * has been laid out.
     */
    private static Bitmap cropToScreenAspect(Context context, View root, Bitmap src) {
        if (src == null || src.getWidth() <= 0 || src.getHeight() <= 0) return src;

        int targetW = root != null ? root.getWidth() : 0;
        int targetH = root != null ? root.getHeight() : 0;
        if (targetW <= 0 || targetH <= 0) {
            targetW = context.getResources().getDisplayMetrics().widthPixels;
            targetH = context.getResources().getDisplayMetrics().heightPixels;
        }
        if (targetW <= 0 || targetH <= 0) return src;

        float targetRatio = (float) targetW / targetH;
        float srcRatio = (float) src.getWidth() / src.getHeight();

        int cropW = src.getWidth();
        int cropH = src.getHeight();
        if (srcRatio > targetRatio) {
            // Source is relatively wider than the screen: crop left/right.
            cropW = Math.round(src.getHeight() * targetRatio);
        } else if (srcRatio < targetRatio) {
            // Source is relatively taller than the screen: crop top/bottom.
            cropH = Math.round(src.getWidth() / targetRatio);
        } else {
            return src; // already matches, nothing to crop
        }
        cropW = Math.max(1, Math.min(cropW, src.getWidth()));
        cropH = Math.max(1, Math.min(cropH, src.getHeight()));
        int x = (src.getWidth() - cropW) / 2;
        int y = (src.getHeight() - cropH) / 2;
        try {
            return Bitmap.createBitmap(src, x, y, cropW, cropH);
        } catch (Exception e) {
            return src;
        }
    }

    private static int calculateSampleSize(BitmapFactory.Options opts, int reqW, int reqH) {
        int sampleSize = 1;
        while (opts.outWidth / sampleSize > reqW * 2 || opts.outHeight / sampleSize > reqH * 2) {
            sampleSize *= 2;
        }
        return Math.max(1, sampleSize);
    }
}
