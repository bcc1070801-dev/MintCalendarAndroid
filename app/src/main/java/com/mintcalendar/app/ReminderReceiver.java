package com.mintcalendar.app;

import android.app.*;
import android.content.*;

public class ReminderReceiver
        extends BroadcastReceiver {

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {

        String title =
                intent.getStringExtra("title");

        if (title == null ||
                title.isEmpty()) {
            title = "行程提醒";
        }

        Intent openApp =
                context.getPackageManager()
                        .getLaunchIntentForPackage(
                                context.getPackageName()
                        );

        PendingIntent contentIntent = null;

        if (openApp != null) {

            contentIntent =
                    PendingIntent.getActivity(
                            context,
                            0,
                            openApp,
                            PendingIntent.FLAG_UPDATE_CURRENT
                                    |
                            PendingIntent.FLAG_IMMUTABLE
                    );
        }

        Notification.Builder builder =
                new Notification.Builder(
                        context,
                        "calendar_reminders"
                )
                .setSmallIcon(
                        android.R.drawable.ic_popup_reminder
                )
                .setContentTitle(
                        "🍃 薄荷奶油行事曆"
                )
                .setContentText(title)
                .setAutoCancel(true)
                .setPriority(
                        Notification.PRIORITY_HIGH
                );

        if (contentIntent != null) {
            builder.setContentIntent(contentIntent);
        }

        NotificationManager manager =
                (NotificationManager)
                        context.getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        manager.notify(
                (int) System.currentTimeMillis(),
                builder.build()
        );
    }
}
