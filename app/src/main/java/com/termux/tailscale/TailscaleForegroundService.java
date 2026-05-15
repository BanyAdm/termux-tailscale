package com.termux.tailscale;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;

/**
 * TailscaleForegroundService
 *
 * Keeps the process alive while the VPN is connected.
 * The actual WireGuard tunnel is set up by VpnService (Tailscale's Go backend).
 * This service provides the persistent notification with quick disconnect action.
 */
public class TailscaleForegroundService extends Service {

    private static final String TAG      = "TS:FgService";
    private static final String CHANNEL  = "tailscale_vpn";
    private static final int    NOTIF_ID = 1;

    static final String ACTION_START = "com.termux.tailscale.START_FG";
    static final String ACTION_STOP  = "com.termux.tailscale.STOP_FG";

    // ── Static helpers ───────────────────────────────────────────────────────

    public static void start(Context ctx) {
        Intent i = new Intent(ctx, TailscaleForegroundService.class);
        i.setAction(ACTION_START);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.startForegroundService(i);
        } else {
            ctx.startService(i);
        }
    }

    public static void stop(Context ctx) {
        Intent i = new Intent(ctx, TailscaleForegroundService.class);
        i.setAction(ACTION_STOP);
        ctx.startService(i);
    }

    // ── Service lifecycle ────────────────────────────────────────────────────

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;

        switch (intent.getAction() != null ? intent.getAction() : "") {
            case ACTION_START:
                Log.i(TAG, "Starting foreground + VPN");
                startForeground(NOTIF_ID, buildNotification(true));
                startVpnBackend();
                break;

            case ACTION_STOP:
                Log.i(TAG, "Stopping");
                stopVpnBackend();
                stopForeground(true);
                stopSelf();
                break;

            default:
                // Restarted by system after kill
                if (VpnBridge.get(this).wantsRunning()) {
                    startForeground(NOTIF_ID, buildNotification(true));
                    startVpnBackend();
                } else {
                    stopSelf();
                }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    // ── VPN backend calls ────────────────────────────────────────────────────

    private void startVpnBackend() {
        // Start the Tailscale VPN service (submodule's IPNService equivalent)
        Intent vpn = new Intent(this, com.termux.tailscale.VpnService.class);
        vpn.setAction(ACTION_START);
        startService(vpn);

        // Update widget and float button
        TailscaleWidget.update(this, true);
    }

    private void stopVpnBackend() {
        Intent vpn = new Intent(this, com.termux.tailscale.VpnService.class);
        vpn.setAction(ACTION_STOP);
        startService(vpn);

        TailscaleWidget.update(this, false);
    }

    // ── Notification ─────────────────────────────────────────────────────────

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                CHANNEL, "Tailscale VPN",
                NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("Tailscale connection status");
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                .createNotificationChannel(ch);
        }
    }

    private Notification buildNotification(boolean connected) {
        // "Disconnect" quick action
        Intent disconnectIntent = new Intent(TailscaleReceiver.ACTION_DOWN);
        disconnectIntent.setClass(this, TailscaleReceiver.class);
        PendingIntent disconnectPi = PendingIntent.getBroadcast(
            this, 0, disconnectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // replace with TS icon
            .setContentTitle(connected ? "Tailscale: Connected" : "Tailscale: Disconnected")
            .setContentText(connected ? "Tap to open · Long-press notif for options" : "")
            .setOngoing(connected)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_delete, "Disconnect", disconnectPi)
            .build();
    }
}
