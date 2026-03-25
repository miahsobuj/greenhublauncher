package com.greenhub.launcher;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MusicPlayerActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private TextView titleText, artistText, currentTime, totalTime;
    private ImageButton btnPlay, btnPrev, btnNext, btnShuffle, btnRepeat;
    private SeekBar seekBar, volumeSeekBar;
    private ProgressBar loadingProgress;
    
    private List<Song> songList = new ArrayList<>();
    private static final int PERMISSION_CODE = 100;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        
        initViews();
        checkPermissions();
        
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_equalizer).setOnClickListener(v -> showEqualizer());
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recycler_songs);
        titleText = findViewById(R.id.song_title);
        artistText = findViewById(R.id.song_artist);
        currentTime = findViewById(R.id.current_time);
        totalTime = findViewById(R.id.total_time);
        btnPlay = findViewById(R.id.btn_play);
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);
        btnShuffle = findViewById(R.id.btn_shuffle);
        btnRepeat = findViewById(R.id.btn_repeat);
        seekBar = findViewById(R.id.seekbar_progress);
        volumeSeekBar = findViewById(R.id.seekbar_volume);
        loadingProgress = findViewById(R.id.loading_progress);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_AUDIO}, PERMISSION_CODE);
        } else {
            loadSongs();
        }
    }
    
    private void loadSongs() {
        new Thread(() -> {
            ContentResolver contentResolver = getContentResolver();
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String[] projection = {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATA
            };
            
            Cursor cursor = contentResolver.query(uri, projection, null, null,
                    MediaStore.Audio.Media.TITLE + " ASC");
            
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    String title = cursor.getString(1);
                    String artist = cursor.getString(2);
                    long duration = cursor.getLong(3);
                    String path = cursor.getString(4);
                    
                    songList.add(new Song(id, title, artist, duration, path));
                }
                cursor.close();
            }
            
            runOnUiThread(() -> {
                // Update UI with songs
                if (songList.isEmpty()) {
                    Toast.makeText(this, "No songs found", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
    
    private void showEqualizer() {
        // Open equalizer dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.GreenDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_equalizer, null);
        
        builder.setView(view)
                .setTitle("Equalizer")
                .setPositiveButton("Apply", null)
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadSongs();
        }
    }
    
    // Song model
    public static class Song {
        long id;
        String title, artist, path;
        long duration;
        
        Song(long id, String title, String artist, long duration, String path) {
            this.id = id;
            this.title = title;
            this.artist = artist;
            this.duration = duration;
            this.path = path;
        }
    }
}

// Phone Activity
package com.greenhub.launcher;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PhoneActivity extends AppCompatActivity {
    
    private TextView numberDisplay;
    private StringBuilder currentNumber = new StringBuilder();
    private static final int PERMISSION_CODE = 100;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        
        numberDisplay = findViewById(R.id.number_display);
        
        setupKeypad();
        setupCallButtons();
        
        // If called with a number
        String phoneNumber = getIntent().getStringExtra("phone_number");
        if (phoneNumber != null) {
            currentNumber.append(phoneNumber);
            updateDisplay();
        }
    }
    
    private void setupKeypad() {
        int[] buttonIds = new int[] {
            R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
            R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9,
            R.id.btn_star, R.id.btn_hash
        };
        
        for (int id : buttonIds) {
            findViewById(id).setOnClickListener(this::onDigitClicked);
        }
        
        findViewById(R.id.btn_delete).setOnClickListener(v -> {
            if (currentNumber.length() > 0) {
                currentNumber.deleteCharAt(currentNumber.length() - 1);
                updateDisplay();
            }
        });
        
        findViewById(R.id.btn_clear).setOnClickListener(v -> {
            currentNumber.setLength(0);
            updateDisplay();
        });
    }
    
    private void onDigitClicked(View view) {
        Button button = (Button) view;
        currentNumber.append(button.getText());
        updateDisplay();
    }
    
    private void updateDisplay() {
        numberDisplay.setText(currentNumber.toString());
    }
    
    private void setupCallButtons() {
        findViewById(R.id.btn_call).setOnClickListener(v -> makeCall());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
    
    private void makeCall() {
        String number = currentNumber.toString();
        if (number.isEmpty()) {
            Toast.makeText(this, "Enter a number", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CODE);
        } else {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + number));
            startActivity(intent);
        }
    }
}
