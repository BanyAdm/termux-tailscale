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

public class TermuxPluginReceiver extends BroadcastReceiver {

    private static final String TAG = "TS:TermuxPlugin";

    private static final String WORK_CONNECT       = "termux-connect-vpn";
    private static final String WORK_DISCONNECT    = "termux-disconnect-vpn";
    private static final String WORK_USE_EXIT_NODE = "termux-use-exit-node";
    private static final String WORK_TOGGLE        = "termux-toggle-vpn";
    private static final String WORK_STATUS        = "termux-status-vpn";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        Log.i(TAG, "onReceive: " + intent.getAction());

        final WorkManager wm = WorkManager.getInstance(context);
        final String action = intent.getAction();
        final String pkg = context.getPackageName();

        if (Objects.equals(action, pkg + ".CONNECT_VPN")) {
            OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(StartVPNWorker.class)
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .build();
            wm.enqueueUniqueWork(WORK_CONNECT, ExistingWorkPolicy.REPLACE, req);

        } else if (Objects.equals(action, pkg + ".DISCONNECT_VPN")) {
            OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(StopVPNWorker.class)
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .build();
            wm.enqueueUniqueWork(WORK_DISCONNECT, ExistingWorkPolicy.REPLACE, req);

        } else if (Objects.equals(action, pkg + ".USE_EXIT_NODE")) {
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

        } else if (Objects.equals(action, pkg + ".TERMUX_TOGGLE")) {
            OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(ToggleVPNWorker.class)
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .build();
            wm.enqueueUniqueWork(WORK_TOGGLE, ExistingWorkPolicy.REPLACE, req);

        } else if (Objects.equals(action, pkg + ".TERMUX_STATUS")) {
            OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(StatusVPNWorker.class)
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .build();
            wm.enqueueUniqueWork(WORK_STATUS, ExistingWorkPolicy.REPLACE, req);
        }
    }
}
