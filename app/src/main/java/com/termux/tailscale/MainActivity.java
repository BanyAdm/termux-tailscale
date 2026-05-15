package com.termux.tailscale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

    private static final int REQ_VPN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Simple programmatic layout
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("Termux:Tailscale");
        title.setTextSize(24);
        root.addView(title);

        TextView statusView = new TextView(this);
        root.addView(statusView);

        Switch vpnSwitch = new Switch(this);
        vpnSwitch.setText("Tailscale VPN");
        root.addView(vpnSwitch);

        Switch floatSwitch = new Switch(this);
        floatSwitch.setText("Floating button");
        root.addView(floatSwitch);

        Button permBtn = new Button(this);
        permBtn.setText("Grant overlay permission");
        root.addView(permBtn);

        setContentView(root);

        // ── Wire up ──────────────────────────────────────────────────────────
        VpnBridge bridge = VpnBridge.get(this);

        statusView.setText("Status: " + (bridge.isConnected() ? "Connected" : "Disconnected"));
        vpnSwitch.setChecked(bridge.isConnected());

        vpnSwitch.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) bridge.connect(); else bridge.disconnect();
        });

        floatSwitch.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) FloatService.start(this); else FloatService.stop(this);
        });

        permBtn.setOnClickListener(v -> {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                startActivity(new Intent(
                    android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
            }
        });
    }

    /** Called by VpnBridge when VPN permission dialog is needed. */
    public static void launchForVpnPermission(Context ctx) {
        Intent i = new Intent(ctx, MainActivity.class);
        i.putExtra("requestVpn", true);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If we were launched to request VPN permission, do it now
        if (getIntent().getBooleanExtra("requestVpn", false)) {
            Intent i = VpnService.prepare(this);
            if (i != null) startActivityForResult(i, REQ_VPN);
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        if (req == REQ_VPN && res == RESULT_OK) {
            VpnBridge.get(this).connect();
        }
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
