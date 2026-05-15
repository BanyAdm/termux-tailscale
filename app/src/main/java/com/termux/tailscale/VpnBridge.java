package com.termux.tailscale;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.util.Log;

/**
 * VpnBridge
 *
 * Thin Java layer between our receivers/widget/float and the Tailscale
 * Go backend (libtailscale.so).  The Go backend is pulled in as an AAR
 * from the tailscale-android submodule via build.gradle.
 *
 * All public methods are safe to call from any thread.
 */
public class VpnBridge {

    private static final String TAG  = "TS:VpnBridge";
    private static final String PREFS = "ts_prefs";
    private static final String KEY_WANT_RUNNING = "want_running";

    // Singleton
    private static VpnBridge sInstance;
    public static synchronized VpnBridge get(Context ctx) {
        if (sInstance == null) sInstance = new VpnBridge(ctx.getApplicationContext());
        return sInstance;
    }

    private final Context mCtx;
    private final SharedPreferences mPrefs;

    private VpnBridge(Context ctx) {
        mCtx   = ctx;
        mPrefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // ── Public API ──────────────────────────────────────────────────────────

    /** Connect Tailscale. Shows VPN permission dialog if needed. */
    public void connect() {
        Log.i(TAG, "connect()");
        mPrefs.edit().putBoolean(KEY_WANT_RUNNING, true).apply();

        // Check if VPN permission has been granted.
        if (VpnService.prepare(mCtx) != null) {
            // Need user to grant VPN permission — open MainActivity which handles this.
            Log.w(TAG, "VPN not prepared, launching permission activity");
            MainActivity.launchForVpnPermission(mCtx);
            return;
        }

        TailscaleForegroundService.start(mCtx);
    }

    /** Disconnect Tailscale. */
    public void disconnect() {
        Log.i(TAG, "disconnect()");
        mPrefs.edit().putBoolean(KEY_WANT_RUNNING, false).apply();
        TailscaleForegroundService.stop(mCtx);
    }

    /** Toggle: connect if down, disconnect if up. */
    public void toggle() {
        if (isConnected()) {
            disconnect();
        } else {
            connect();
        }
    }

    /**
     * Best-effort connection check.
     * True if the VPN tunnel interface (tun0) is present.
     * For a more accurate check the Go backend exposes ipn.State but
     * that requires JNI — we add that in a follow-up.
     */
    public boolean isConnected() {
        try {
            java.net.NetworkInterface tun = java.net.NetworkInterface.getByName("tun0");
            return tun != null && tun.isUp();
        } catch (Exception e) {
            return false;
        }
    }

    /** Whether the user last asked us to be running (persisted across reboots). */
    public boolean wantsRunning() {
        return mPrefs.getBoolean(KEY_WANT_RUNNING, false);
    }
}
