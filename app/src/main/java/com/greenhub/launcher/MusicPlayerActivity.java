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
}
