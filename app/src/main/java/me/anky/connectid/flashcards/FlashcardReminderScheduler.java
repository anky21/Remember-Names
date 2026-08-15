package me.anky.connectid.flashcards;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

public final class FlashcardReminderScheduler {
    static final String ACTION_REMIND = "me.anky.connectid.action.FLASHCARD_REMINDER";
    static final String KEY_ENABLED = "flashcards_reminder_enabled";
    static final String KEY_HOUR = "flashcards_reminder_hour";
    static final String KEY_MINUTE = "flashcards_reminder_minute";
    static final int DEFAULT_HOUR = 19;
    static final int DEFAULT_MINUTE = 0;
    private static final int REQUEST_CODE = 7102;

    private FlashcardReminderScheduler() {
    }

    public static void setReminder(Context context, SharedPreferences preferences,
                                   boolean enabled, int hour, int minute) {
        preferences.edit()
                .putBoolean(KEY_ENABLED, enabled)
                .putInt(KEY_HOUR, hour)
                .putInt(KEY_MINUTE, minute)
                .apply();
        if (enabled) {
            schedule(context, hour, minute);
        } else {
            cancel(context);
        }
    }

    public static void restore(Context context, SharedPreferences preferences) {
        if (!preferences.getBoolean(KEY_ENABLED, false)) {
            return;
        }
        schedule(context,
                preferences.getInt(KEY_HOUR, DEFAULT_HOUR),
                preferences.getInt(KEY_MINUTE, DEFAULT_MINUTE));
    }

    private static void schedule(Context context, int hour, int minute) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Calendar trigger = Calendar.getInstance();
        trigger.set(Calendar.HOUR_OF_DAY, hour);
        trigger.set(Calendar.MINUTE, minute);
        trigger.set(Calendar.SECOND, 0);
        trigger.set(Calendar.MILLISECOND, 0);
        if (trigger.getTimeInMillis() <= System.currentTimeMillis()) {
            trigger.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                trigger.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                getPendingIntent(context));
    }

    private static void cancel(Context context) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(getPendingIntent(context));
        }
    }

    private static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, FlashcardReminderReceiver.class)
                .setAction(ACTION_REMIND);
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
