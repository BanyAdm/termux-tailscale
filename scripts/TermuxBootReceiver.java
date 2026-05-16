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
        if (VpnService.prepare(ctx) != null) { Log.i(TAG, "VPN not prepared, skipping"); return; }
        UninitializedApp.get().startVPN();
    }
}
