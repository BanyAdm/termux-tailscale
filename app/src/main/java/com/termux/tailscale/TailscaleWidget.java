package com.termux.tailscale;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * TailscaleWidget
 *
 * A 1×1 homescreen widget showing the connection state as a toggle button.
 * Tapping it fires com.termux.tailscale.TOGGLE → VpnBridge.toggle().
 */
public class TailscaleWidget extends AppWidgetProvider {

    static final String ACTION_WIDGET_TOGGLE = "com.termux.tailscale.WIDGET_TOGGLE";

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        boolean connected = VpnBridge.get(ctx).isConnected();
        for (int id : ids) updateWidget(ctx, mgr, id, connected);
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        super.onReceive(ctx, intent);
        if (ACTION_WIDGET_TOGGLE.equals(intent.getAction())) {
            VpnBridge.get(ctx).toggle();
            // The service will call update() after state changes
        }
    }

    /** Call this whenever the VPN state changes to refresh all widget instances. */
    public static void update(Context ctx, boolean connected) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(ctx, TailscaleWidget.class));
        for (int id : ids) updateWidget(ctx, mgr, id, connected);
    }

    private static void updateWidget(Context ctx, AppWidgetManager mgr, int id, boolean connected) {
        RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.widget_layout);

        // Toggle the icon/label based on state
        views.setImageViewResource(R.id.widget_icon,
            connected ? R.drawable.ic_ts_on : R.drawable.ic_ts_off);
        views.setTextViewText(R.id.widget_label,
            connected ? "ON" : "OFF");

        // Wire up the tap action
        Intent toggleIntent = new Intent(ctx, TailscaleWidget.class);
        toggleIntent.setAction(ACTION_WIDGET_TOGGLE);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, 0, toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, pi);

        mgr.updateAppWidget(id, views);
    }
}
