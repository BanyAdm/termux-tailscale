package com.tailscale.ipn;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.util.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
public class TermuxPluginReceiver extends BroadcastReceiver {
    private static final String TAG = "TS:TermuxPlugin";
    public static final String ACTION_UP = "com.tailscale.ipn.TERMUX_UP";
    public static final String ACTION_DOWN = "com.tailscale.ipn.TERMUX_DOWN";
    public static final String ACTION_TOGGLE = "com.tailscale.ipn.TERMUX_TOGGLE";
    public static final String ACTION_STATUS = "com.tailscale.ipn.TERMUX_STATUS";
    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        Log.i(TAG, "onReceive: " + intent.getAction());
        UninitializedApp app = UninitializedApp.get();
        switch (intent.getAction()) {
            case ACTION_UP:
                if (VpnService.prepare(ctx) != null) { Log.w(TAG, "VPN not prepared"); return; }
                app.startVPN();
                break;
            case ACTION_DOWN:
                app.stopVPN();
                break;
            case ACTION_TOGGLE:
                if (VpnService.prepare(ctx) != null) { Log.w(TAG, "VPN not prepared"); return; }
                if (isConnected()) { app.stopVPN(); } else { app.startVPN(); }
                break;
            case ACTION_STATUS:
                writeStatus(ctx, isConnected());
                break;
        }
    }
    private boolean isConnected() {
        try {
            java.net.NetworkInterface tun = java.net.NetworkInterface.getByName("tun0");
            return tun != null && tun.isUp();
        } catch (Exception e) { return false; }
    }
    private void writeStatus(Context ctx, boolean connected) {
        String status = connected ? "connected" : "disconnected";
        File f = new File(ctx.getFilesDir(), "termux_status");
        try (FileWriter w = new FileWriter(f)) { w.write(status + "\n"); } catch (IOException e) { Log.e(TAG, "status write failed", e); }
        Intent result = new Intent("com.tailscale.ipn.TERMUX_STATUS_RESULT");
        result.putExtra("connected", connected);
        result.putExtra("status", status);
        ctx.sendBroadcast(result);
    }
}
