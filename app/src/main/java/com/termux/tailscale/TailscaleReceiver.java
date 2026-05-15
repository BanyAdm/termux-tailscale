package com.termux.tailscale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * TailscaleReceiver
 *
 * Handles explicit broadcast intents sent from Termux shell scripts:
 *
 *   am broadcast -a com.termux.tailscale.UP
 *   am broadcast -a com.termux.tailscale.DOWN
 *   am broadcast -a com.termux.tailscale.TOGGLE
 *   am broadcast -a com.termux.tailscale.STATUS
 *
 * STATUS writes result to a file so the shell can read it back:
 *   /data/data/com.termux.tailscale/files/status  → "connected" | "disconnected"
 */
public class TailscaleReceiver extends BroadcastReceiver {

    private static final String TAG = "TS:Receiver";

    static final String ACTION_UP     = "com.termux.tailscale.UP";
    static final String ACTION_DOWN   = "com.termux.tailscale.DOWN";
    static final String ACTION_TOGGLE = "com.termux.tailscale.TOGGLE";
    static final String ACTION_STATUS = "com.termux.tailscale.STATUS";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        Log.i(TAG, "onReceive: " + intent.getAction());
        VpnBridge bridge = VpnBridge.get(ctx);

        switch (intent.getAction()) {
            case ACTION_UP:
                bridge.connect();
                break;

            case ACTION_DOWN:
                bridge.disconnect();
                break;

            case ACTION_TOGGLE:
                bridge.toggle();
                break;

            case ACTION_STATUS:
                writeStatus(ctx, bridge.isConnected());
                break;
        }
    }

    private void writeStatus(Context ctx, boolean connected) {
        // Write to a known path so the shell script can cat it
        String status = connected ? "connected" : "disconnected";
        java.io.File dir  = ctx.getFilesDir();
        java.io.File file = new java.io.File(dir, "status");
        try (java.io.FileWriter w = new java.io.FileWriter(file)) {
            w.write(status + "\n");
        } catch (java.io.IOException e) {
            Log.e(TAG, "Failed to write status", e);
        }
        Log.i(TAG, "status=" + status);

        // Also send a result broadcast so scripts can use a listener
        Intent result = new Intent("com.termux.tailscale.STATUS_RESULT");
        result.putExtra("connected", connected);
        result.putExtra("status", status);
        ctx.sendBroadcast(result);
    }
}
