// Copyright (c) Tailscale Inc & AUTHORS
// SPDX-License-Identifier: BSD-3-Clause

package com.tailscale.ipn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.util.Log;

public class TermuxBootReceiver extends BroadcastReceiver {

    private static final String TAG = "TS:TermuxBoot";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        Log.i(TAG, "Boot received: " + intent.getAction());
        UninitializedApp app = UninitializedApp.get();
        if (VpnService.prepare(ctx) != null) {
            Log.i(TAG, "VPN not prepared, skipping auto-reconnect");
            return;
        }
        if (app.isAbleToStartVPN()) {
            app.startVPN();
        }
    }
}
