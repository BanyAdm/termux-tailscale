package com.termux.tailscale;

import android.content.Intent;
import android.util.Log;

/**
 * VpnService
 *
 * Extends Android's VpnService and delegates to the Tailscale Go backend.
 *
 * In the full build this extends com.tailscale.ipn.GoBackend (from the
 * tailscale-android submodule AAR). The Go backend handles:
 *   - WireGuard tunnel setup
 *   - Key exchange with Tailscale control plane
 *   - DNS and routing
 *
 * For now this is a stub — replace "extends android.net.VpnService" with
 * "extends com.tailscale.ipn.GoBackend" once the submodule is wired in.
 */
public class VpnService extends android.net.VpnService {

    private static final String TAG = "TS:VpnService";

    static final String ACTION_START = "com.termux.tailscale.VPN_START";
    static final String ACTION_STOP  = "com.termux.tailscale.VPN_STOP";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;

        switch (intent.getAction() != null ? intent.getAction() : "") {
            case ACTION_START:
                Log.i(TAG, "Starting Go backend");
                // TODO: call GoBackend.start() from tailscale-android submodule
                // GoBackend.getInstance().connect(this);
                break;
            case ACTION_STOP:
                Log.i(TAG, "Stopping Go backend");
                // TODO: call GoBackend.stop()
                stopSelf();
                break;
        }
        return START_STICKY;
    }

    @Override
    public void onRevoke() {
        Log.i(TAG, "VPN revoked by system");
        VpnBridge.get(this).disconnect();
        super.onRevoke();
    }
}
