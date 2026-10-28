package com.uts.pengingatcatatan;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class DialogActivity extends AppCompatActivity {

    private static final String EXTRA_NOTE_TEXT = "note_text";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String noteText = getIntent().getStringExtra(EXTRA_NOTE_TEXT);
        if (noteText == null) {
            noteText = "";
        }

        new AlertDialog.Builder(this)
                .setMessage("Apakah catatan ini sudah dibaca: " + noteText + "?")
                .setPositiveButton("Ya", (dialog, which) -> finish())
                .setNegativeButton("Belum", (dialog, which) -> finish())
                .setOnCancelListener(dialog -> finish())
                .show();
    }
}
