// Copyright (c) Tailscale Inc & AUTHORS
// SPDX-License-Identifier: BSD-3-Clause

package com.tailscale.ipn;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class StatusVPNWorker extends Worker {

    public static final String STATUS_RESULT_ACTION = "com.tailscale.ipn.TERMUX_STATUS_RESULT";

    public StatusVPNWorker(Context appContext, WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        UninitializedApp app = UninitializedApp.get();
        boolean connected = app.isAbleToStartVPN();
        String status = connected ? "connected" : "disconnected";

        File f = new File(app.getFilesDir(), "termux_status");
        try (FileWriter w = new FileWriter(f)) {
            w.write(status + "\n");
        } catch (IOException e) {
        }

        Intent result = new Intent(STATUS_RESULT_ACTION);
        result.putExtra("connected", connected);
        result.putExtra("status", status);
        app.sendBroadcast(result);

        return Result.success();
    }
}
