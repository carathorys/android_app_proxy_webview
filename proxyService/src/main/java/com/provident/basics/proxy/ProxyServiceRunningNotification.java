package com.provident.basics.proxy;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;

import com.provident.basics.MainActivity_;
import com.provident.basics.R;

/**
 * Helper class for showing and canceling proxy service running
 * notifications.
 * <p>
 * This class makes heavy use of the {@link NotificationCompat.Builder} helper
 * class to create notifications in a backward-compatible way.
 */
public class ProxyServiceRunningNotification {
    /**
     * The unique identifier for this type of notification.
     */
    private static final String NOTIFICATION_TAG = "ProxyServiceRunningNotification";

    /**
     * Shows the notification, or updates a previously shown notification of
     * this type, with the given parameters.
     * <p>
     * TODO: Customize this method's arguments to present relevant content in
     * the notification.
     * <p>
     * TODO: Customize the contents of this method to tweak the behavior and
     * presentation of proxy service running notifications. Make
     * sure to follow the
     * <a href="https://developer.android.com/design/patterns/notifications.html">
     * Notification design guidelines</a> when doing so.
     *
     * @see #cancel(Context)
     */
    public static void notify(final Context context, final int count, final long input, final long output) {
        if (context == null)
            return;
        final Resources res = context.getResources();

        final String title = res.getString(
                R.string.proxy_service_running_notification_title_template);

        final Intent resultIntent = new Intent(context, MainActivity_.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "ProxyService")

                // Set appropriate defaults for the notification light, sound,
                // and vibration.
                // .setDefaults(Notification.DEFAULT_ALL)

                // Set required fields, including the small icon, the
                // notification title, and text.
                .setSmallIcon(R.drawable.ic_stat_proxy_service_running)
                .setContentTitle(title)
                .setVibrate(new long[]{0, 50})
                // Use a default priority (recognized on devices running Android
                // 4.1 or later)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                // Set the pending intent to be initiated when the user touches
                // the notification.
                .setContentIntent(resultPendingIntent)
                .setColorized(true).setColor(res.getColor(R.color.colorPrimaryDark))
                .setOngoing(true)
                .setChannelId(NOTIFICATION_TAG)
                // Automatically dismiss the notification when it is touched.
                .setAutoCancel(false);
        if (count > 0) {
            builder.setSubText(res.getString(R.string.proxy_service_requests_count, count));
        }
        if (input > 0 || output > 0) {
            builder.setContentText(res.getString(R.string.proxy_service_requests_size, getType((double) input, 0), getType((double) output, 0)));
        }
        notify(context, builder.build());
    }

    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        CharSequence name = context.getString(R.string.app_name);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel c = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            c = new NotificationChannel(NOTIFICATION_TAG, name, importance);
            nm.createNotificationChannel(c);
        }

        nm.notify(NOTIFICATION_TAG, 0, notification);
    }

    public static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_TAG, 0);
    }

    @SuppressLint("DefaultLocale")
    private static String getType(double in, int level) {
        if ((in / 1024) > 1) {
            return getType(in / 1024, level + 1);
        } else {
            switch (level) {
                case 0:
                    return String.format("%.2f " + (in > 1 ? "bytes" : "byte"), in);
                case 1:
                    return String.format("%.2f Kb", in);
                case 2:
                    return String.format("%.2f Mb", in);
                case 3:
                    return String.format("%.2f Gb", in);
                case 4:
                    return String.format("%.2f Tb", in);
            }
        }
        return "";
    }
}
