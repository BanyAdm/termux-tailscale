package com.termux.tailscale;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * FloatService
 *
 * Draws a small draggable floating button (requires SYSTEM_ALERT_WINDOW).
 * Tap  → toggle VPN
 * Drag → reposition anywhere on screen
 *
 * Start via:  am startservice -n com.termux.tailscale/.FloatService
 * or from MainActivity settings toggle.
 */
public class FloatService extends Service {

    private WindowManager mWm;
    private View          mFloat;
    private WindowManager.LayoutParams mParams;

    // Drag tracking
    private int mInitX, mInitY, mTouchX, mTouchY;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mFloat != null) return START_STICKY; // already showing
        show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        hide();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    // ── Floating view ────────────────────────────────────────────────────────

    private void show() {
        mWm = (WindowManager) getSystemService(WINDOW_SERVICE);

        ImageView iv = new ImageView(this);
        iv.setImageResource(R.drawable.ic_float_btn);
        iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        iv.setPadding(8, 8, 8, 8);
        updateIcon(iv);

        mParams = new WindowManager.LayoutParams(
            dpToPx(56), dpToPx(56),
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);
        mParams.gravity = Gravity.TOP | Gravity.START;
        mParams.x = 0;
        mParams.y = 200;

        iv.setOnTouchListener(new View.OnTouchListener() {
            boolean dragged;

            @Override
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dragged  = false;
                        mInitX   = mParams.x;
                        mInitY   = mParams.y;
                        mTouchX  = (int) e.getRawX();
                        mTouchY  = (int) e.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) e.getRawX() - mTouchX;
                        int dy = (int) e.getRawY() - mTouchY;
                        if (Math.abs(dx) > 5 || Math.abs(dy) > 5) dragged = true;
                        mParams.x = mInitX + dx;
                        mParams.y = mInitY + dy;
                        mWm.updateViewLayout(mFloat, mParams);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!dragged) {
                            // Tap → toggle
                            VpnBridge bridge = VpnBridge.get(FloatService.this);
                            bridge.toggle();
                            updateIcon((ImageView) v);
                        }
                        break;
                }
                return true;
            }
        });

        mFloat = iv;
        mWm.addView(mFloat, mParams);
    }

    private void hide() {
        if (mFloat != null && mWm != null) {
            mWm.removeView(mFloat);
            mFloat = null;
        }
    }

    private void updateIcon(ImageView iv) {
        boolean on = VpnBridge.get(this).isConnected();
        iv.setImageResource(on ? R.drawable.ic_ts_on : R.drawable.ic_ts_off);
        iv.setAlpha(on ? 1.0f : 0.6f);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    // ── Static helpers ───────────────────────────────────────────────────────

    public static void start(Context ctx) {
        ctx.startService(new Intent(ctx, FloatService.class));
    }

    public static void stop(Context ctx) {
        ctx.stopService(new Intent(ctx, FloatService.class));
    }
}
