// Copyright (c) Tailscale Inc & AUTHORS
// SPDX-License-Identifier: BSD-3-Clause

package com.tailscale.ipn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.WorkManager;

import java.util.Objects;

/**
 * TermuxPluginReceiver
 *
 * Receives broadcasts from Termux shell scripts to control the Tailscale VPN.
 * Uses WorkManager (same as IPNReceiver) so it works reliably from background.
 *
 * Usage from Termux:
 *   am broadcast -a com.tailscale.ipn.CONNECT_VPN
 *   am broadcast -a com.tailscale.ipn.DISCONNECT_VPN
 *   am broadcast -a com.tailscale.ipn.USE_EXIT_NODE --es exitNode "nodeName" --ez allowLanAccess false
 *   am broadcast -a com.tailscale.ipn.TERMUX_TOGGLE
 *   am broadcast -a com.tailscale.ipn.TERMUX_STATUS
 */
public class TermuxPluginReceiver extends BroadcastReceiver {

    private static final String TAG = "TS:TermuxPlugin";

    public static final String ACTION_CONNECT    = "com.tailscale.ipn.CONNECT_VPN";
    public static final String ACTION_DISCONNECT = "com.tailscale.ipn.DISCONNECT_VPN";
    public static final String ACTION_USE_EXIT_NODE = "com.tailscale.ipn.USE_EXIT_NODE";
    public static final String ACTION_TOGGLE     = "com.tailscale.ipn.TERMUX_TOGGLE";
    public static final String ACTION_STATUS     = "com.tailscale.ipn.TERMUX_STATUS";

    private static final String WORK_CONNECT      = "termux-connect-vpn";
    private static final String WORK_DISCONNECT   = "termux-disconnect-vpn";
    private static final String WORK_USE_EXIT_NODE = "termux-use-exit-node";
    private static final String WORK_TOGGLE       = "termux-toggle-vpn";
    private static final String WORK_STATUS       = "termux-status-vpn";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        Log.i(TAG, "onReceive: " + intent.getAction());

        final WorkManager wm = WorkManager.getInstance(context);
        final String action = intent.getAction();

        switch (action) {
            case ACTION_CONNECT: {
                OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(StartVPNWorker.class)
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        .build();
                wm.enqueueUniqueWork(WORK_CONNECT, ExistingWorkPolicy.REPLACE, req);
                break;
            }
            case ACTION_DISCONNECT: {
                OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(StopVPNWorker.class)
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        .build();
                wm.enqueueUniqueWork(WORK_DISCONNECT, ExistingWorkPolicy.REPLACE, req);
                break;
            }
            case ACTION_USE_EXIT_NODE: {
                String exitNode = intent.getStringExtra("exitNode");
                boolean allowLanAccess = intent.getBooleanExtra("allowLanAccess", false);
                Data input = new Data.Builder()
                        .putString(UseExitNodeWorker.EXIT_NODE_NAME, exitNode)
                        .putBoolean(UseExitNodeWorker.ALLOW_LAN_ACCESS, allowLanAccess)
                        .build();
                OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(UseExitNodeWorker.class)
                        .setInputData(input)
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        .build();
                wm.enqueueUniqueWork(WORK_USE_EXIT_NODE, ExistingWorkPolicy.REPLACE, req);
                break;
            }
            case ACTION_TOGGLE: {
                OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(ToggleVPNWorker.class)
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        .build();
                wm.enqueueUniqueWork(WORK_TOGGLE, ExistingWorkPolicy.REPLACE, req);
                break;
            }
            case ACTION_STATUS: {
                OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(StatusVPNWorker.class)
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        .build();
                wm.enqueueUniqueWork(WORK_STATUS, ExistingWorkPolicy.REPLACE, req);
                break;
            }
            default:
                Log.w(TAG, "Unknown action: " + action);
        }
    }
}
