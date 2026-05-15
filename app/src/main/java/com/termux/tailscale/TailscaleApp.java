package com.termux.tailscale;

import android.app.Application;

public class TailscaleApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // VpnBridge singleton is lazy-initialized on first use
    }
}
