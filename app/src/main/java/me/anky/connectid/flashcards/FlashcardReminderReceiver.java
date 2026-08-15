package me.anky.connectid.flashcards;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import me.anky.connectid.R;

public class FlashcardReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "flashcard_practice_reminders";
    private static final int NOTIFICATION_ID = 7103;

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = context.getSharedPreferences(
                "shared-prefs", Context.MODE_PRIVATE);
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            FlashcardReminderScheduler.restore(context, preferences);
            return;
        }
        if (!preferences.getBoolean(FlashcardReminderScheduler.KEY_ENABLED, false)) {
            return;
        }
        FlashcardProgressStore.Snapshot progress =
                new FlashcardProgressStore(preferences).getSnapshot();
        if (progress.getReviewsToday() >= progress.getDailyGoal()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) {
            return;
        }
        createChannel(manager);

        Intent openIntent = new Intent(context, FlashcardSetupActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, 7104, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.flashcards_reminder_title))
                .setContentText(context.getString(R.string.flashcards_reminder_message))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        manager.notify(NOTIFICATION_ID, notification.build());
    }

    private void createChannel(NotificationManager manager) {
        if (manager.getNotificationChannel(CHANNEL_ID) != null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Flashcard practice",
                NotificationManager.IMPORTANCE_DEFAULT);
        manager.createNotificationChannel(channel);
    }
}
