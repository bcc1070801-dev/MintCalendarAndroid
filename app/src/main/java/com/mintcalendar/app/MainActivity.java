package com.mintcalendar.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final String CHANNEL_ID = "calendar_reminders";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createNotificationChannel();

        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    100
            );
        }

        WebView webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(
                new ReminderBridge(this),
                "AndroidReminder"
        );

        webView.loadUrl("file:///android_asset/index.html");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "行程提醒",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription("薄荷奶油行事曆提醒");

            getSystemService(NotificationManager.class)
                    .createNotificationChannel(channel);
        }
    }

    public static class ReminderBridge {

        private final Context context;

        ReminderBridge(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void schedule(
                String date,
                String time,
                String title,
                int minutesBefore
        ) {

            try {
                SimpleDateFormat sdf =
                        new SimpleDateFormat(
                                "yyyy-MM-dd HH:mm",
                                Locale.TAIWAN
                        );

                Date eventDate =
                        sdf.parse(date + " " + time);

                if (eventDate == null) return;

                long triggerAt =
                        eventDate.getTime()
                        - minutesBefore * 60000L;

                if (triggerAt <=
                        System.currentTimeMillis()) return;

                Intent intent =
                        new Intent(
                                context,
                                ReminderReceiver.class
                        );

                intent.putExtra("title", title);

                int requestCode =
                        (date + time + title +
                                minutesBefore).hashCode();

                PendingIntent pendingIntent =
                        PendingIntent.getBroadcast(
                                context,
                                requestCode,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                                        |
                                PendingIntent.FLAG_IMMUTABLE
                        );

                AlarmManager alarmManager =
                        (AlarmManager)
                                context.getSystemService(
                                        Context.ALARM_SERVICE
                                );

                if (Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.S) {

                    if (alarmManager
                            .canScheduleExactAlarms()) {

                        alarmManager
                                .setExactAndAllowWhileIdle(
                                        AlarmManager.RTC_WAKEUP,
                                        triggerAt,
                                        pendingIntent
                                );

                    } else {

                        alarmManager
                                .setAndAllowWhileIdle(
                                        AlarmManager.RTC_WAKEUP,
                                        triggerAt,
                                        pendingIntent
                                );
                    }

                } else {

                    alarmManager
                            .setExactAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    triggerAt,
                                    pendingIntent
                            );
                }

            } catch (Exception ignored) {
            }
        }
    }
}
