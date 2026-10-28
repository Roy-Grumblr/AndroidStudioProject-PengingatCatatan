package com.uts.pengingatcatatan;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final String EXTRA_NOTE_TEXT = "note_text";

    private EditText noteInput;
    private TimePicker timePicker;
    private AlarmManager alarmManager;
    private String pendingNote;
    private boolean waitingForExactAlarmPermission;
    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        noteInput = findViewById(R.id.editTextNote);
        timePicker = findViewById(R.id.timePicker);
        Button scheduleButton = findViewById(R.id.buttonSchedule);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        timePicker.setIs24HourView(true);

        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        schedulePendingReminder();
                    } else {
                        pendingNote = null;
                        Toast.makeText(this, "Izin notifikasi diperlukan untuk menampilkan pengingat.", Toast.LENGTH_LONG).show();
                    }
                });

        scheduleButton.setOnClickListener(view -> requestReminder());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (waitingForExactAlarmPermission) {
            waitingForExactAlarmPermission = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    && alarmManager.canScheduleExactAlarms()) {
                schedulePendingReminder();
            } else {
                pendingNote = null;
                Toast.makeText(this, "Izin alarm tepat belum diberikan.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void requestReminder() {
        String note = noteInput.getText().toString().trim();
        if (note.isEmpty()) {
            Toast.makeText(this, "Catatan tidak boleh kosong.", Toast.LENGTH_SHORT).show();
            return;
        }
        pendingNote = note;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            return;
        }
        schedulePendingReminder();
    }

    private void schedulePendingReminder() {
        if (pendingNote == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && !alarmManager.canScheduleExactAlarms()) {
            waitingForExactAlarmPermission = true;
            Intent settingsIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            settingsIntent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(settingsIntent);
            Toast.makeText(this, "Izinkan alarm tepat agar pengingat dapat dijadwalkan.", Toast.LENGTH_LONG).show();
            return;
        }

        Calendar executionTime = Calendar.getInstance();
        executionTime.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
        executionTime.set(Calendar.MINUTE, timePicker.getMinute());
        executionTime.set(Calendar.SECOND, 0);
        executionTime.set(Calendar.MILLISECOND, 0);
        if (!executionTime.after(Calendar.getInstance())) {
            executionTime.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent alarmIntent = new Intent(this, NoteAlarmReceiver.class);
        alarmIntent.putExtra(EXTRA_NOTE_TEXT, pendingNote);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        try {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    executionTime.getTimeInMillis(),
                    pendingIntent);
            Toast.makeText(this, "Pengingat berhasil dijadwalkan.", Toast.LENGTH_SHORT).show();
            noteInput.setText("");
            pendingNote = null;
        } catch (SecurityException exception) {
            pendingNote = null;
            Toast.makeText(this, "Tidak dapat menjadwalkan alarm. Periksa izin alarm tepat.", Toast.LENGTH_LONG).show();
        }
    }
}
