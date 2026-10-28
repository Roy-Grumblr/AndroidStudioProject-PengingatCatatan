package com.uts.pengingatcatatan;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class NoteAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "NoteAlarmReceiver";
    private static final String CHANNEL_ID = "daily_note_channel";
    private static final String EXTRA_NOTE_TEXT = "note_text";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        String noteText = intent.getStringExtra(EXTRA_NOTE_TEXT);
        if (noteText == null || noteText.trim().isEmpty()) {
            Log.w(TAG, "Alarm received without a note.");
            return;
        }

        createNotificationChannel(context);

        Intent dialogIntent = new Intent(context, DialogActivity.class);
        dialogIntent.putExtra(EXTRA_NOTE_TEXT, noteText);
        dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent dialogPendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                dialogIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Pengingat Catatan Harian")
                .setContentText(noteText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(noteText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(dialogPendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Notification permission is not granted.");
            return;
        }
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Pengingat Catatan Harian",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifikasi untuk catatan harian yang dijadwalkan.");
            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
