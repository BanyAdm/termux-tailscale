// Copyright (c) Tailscale Inc & AUTHORS
// SPDX-License-Identifier: BSD-3-Clause

package com.tailscale.ipn;

import android.content.Context;
import android.net.VpnService;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public final class ToggleVPNWorker extends Worker {

    public ToggleVPNWorker(Context appContext, WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        UninitializedApp app = UninitializedApp.get();
        if (VpnService.prepare(app) != null) {
            return Result.failure();
        }
        if (app.isAbleToStartVPN()) {
            app.stopVPN();
        } else {
            app.startVPN();
        }
        return Result.success();
    }
}
