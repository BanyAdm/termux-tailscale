package com.termux.tailscale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Reconnects Tailscale after device reboot if the user had it running. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (VpnBridge.get(ctx).wantsRunning()) {
            VpnBridge.get(ctx).connect();
        }
    }
}
