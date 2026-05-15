package com.termux.tailscale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * TaskerReceiver
 *
 * Locale/Tasker "fire" receiver.
 * The bundle extra "action" can be: "up", "down", "toggle"
 *
 * In Tasker: Plugin → Termux:Tailscale → pick action
 */
public class TaskerReceiver extends BroadcastReceiver {

    private static final String TAG = "TS:Tasker";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        Bundle bundle = intent.getBundleExtra("com.twofortyfouram.locale.intent.extra.BUNDLE");
        if (bundle == null) return;

        String action = bundle.getString("action", "toggle");
        Log.i(TAG, "Tasker fire: " + action);

        VpnBridge bridge = VpnBridge.get(ctx);
        switch (action) {
            case "up":     bridge.connect();    break;
            case "down":   bridge.disconnect(); break;
            default:       bridge.toggle();     break;
        }
    }
}
