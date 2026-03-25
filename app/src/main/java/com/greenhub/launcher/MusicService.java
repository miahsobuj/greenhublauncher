package com.greenhub.launcher;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicService extends Service {
    
    public static final String ACTION_PLAY = "com.greenhub.launcher.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.greenhub.launcher.ACTION_PAUSE";
    public static final String ACTION_NEXT = "com.greenhub.launcher.ACTION_NEXT";
    public static final String ACTION_PREV = "com.greenhub.launcher.ACTION_PREV";
    public static final String ACTION_STOP = "com.greenhub.launcher.ACTION_STOP";
    
    public static final String CHANNEL_ID = "MusicServiceChannel";
    public static final int NOTIFICATION_ID = 1001;
    
    private final IBinder binder = new MusicBinder();
    private MediaPlayer mediaPlayer;
    private List<Song> playlist = new ArrayList<>();
    private int currentIndex = -1;
    private boolean isPlaying = false;
    private boolean isShuffle = false;
    private int repeatMode = 0; // 0: none, 1: all, 2: one
    
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private MediaSessionCompat mediaSession;
    
    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
    
    public static class Song {
        public long id;
        public String title;
        public String artist;
        public String path;
        public long duration;
        
        public Song(long id, String title, String artist, String path, long duration) {
            this.id = id;
            this.title = title;
            this.artist = artist;
            this.path = path;
            this.duration = duration;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        initMediaPlayer();
        initMediaSession();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Player",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("GreenHub Music Player controls");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    
    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        
        mediaPlayer.setOnCompletionListener(mp -> {
            if (repeatMode == 2) {
                playSong(currentIndex);
            } else {
                playNext();
            }
        });
        
        mediaPlayer.setOnPreparedListener(mp -> {
            mp.start();
            isPlaying = true;
            updateNotification();
            sendBroadcastState();
        });
        
        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            isPlaying = false;
            sendBroadcastState();
            return true;
        });
    }
    
    private void initMediaSession() {
        mediaSession = new MediaSessionCompat(this, "GreenHubMusic");
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                playSong(currentIndex);
            }
            
            @Override
            public void onPause() {
                pause();
            }
            
            @Override
            public void onSkipToNext() {
                playNext();
            }
            
            @Override
            public void onSkipToPrevious() {
                playPrevious();
            }
            
            @Override
            public void onStop() {
                stopPlayback();
            }
        });
        mediaSession.setActive(true);
    }
    
    private boolean requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                            new AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .build()
                    )
                    .setOnAudioFocusChangeListener(focusChange -> {
                        switch (focusChange) {
                            case AudioManager.AUDIOFOCUS_LOSS:
                            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                                pause();
                                break;
                            case AudioManager.AUDIOFOCUS_GAIN:
                                resume();
                                break;
                        }
                    })
                    .build();
            return audioManager.requestAudioFocus(audioFocusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        } else {
            return audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, 
                    AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;
        
        String action = intent.getAction();
        if (action == null) return START_STICKY;
        
        switch (action) {
            case ACTION_PLAY:
                if (currentIndex >= 0) {
                    resume();
                }
                break;
            case ACTION_PAUSE:
                pause();
                break;
            case ACTION_NEXT:
                playNext();
                break;
            case ACTION_PREV:
                playPrevious();
                break;
            case ACTION_STOP:
                stopPlayback();
                stopForeground(true);
                stopSelf();
                break;
        }
        
        return START_STICKY;
    }
    
    public void setPlaylist(List<Song> songs) {
        this.playlist.clear();
        this.playlist.addAll(songs);
        if (currentIndex == -1 && !playlist.isEmpty()) {
            currentIndex = 0;
        }
    }
    
    public void playSong(int index) {
        if (playlist.isEmpty() || index < 0 || index >= playlist.size()) return;
        
        currentIndex = index;
        Song song = playlist.get(currentIndex);
        
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(song.path);
            mediaPlayer.prepareAsync();
            
            // Become foreground service
            startForeground(NOTIFICATION_ID, buildNotification());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            updateNotification();
            sendBroadcastState();
            stopForeground(false);
        }
    }
    
    public void resume() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            if (requestAudioFocus()) {
                mediaPlayer.start();
                isPlaying = true;
                startForeground(NOTIFICATION_ID, buildNotification());
                sendBroadcastState();
            }
        }
    }
    
    public void playPause() {
        if (isPlaying) {
            pause();
        } else {
            if (currentIndex == -1 && !playlist.isEmpty()) {
                playSong(0);
            } else {
                resume();
            }
        }
    }
    
    public void playNext() {
        if (playlist.isEmpty()) return;
        
        if (isShuffle) {
            currentIndex = new Random().nextInt(playlist.size());
        } else {
            currentIndex = (currentIndex + 1) % playlist.size();
        }
        playSong(currentIndex);
    }
    
    public void playPrevious() {
        if (playlist.isEmpty()) return;
        
        currentIndex = (currentIndex - 1 + playlist.size()) % playlist.size();
        playSong(currentIndex);
    }
    
    public void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isPlaying = false;
        }
        if (mediaSession != null) {
            mediaSession.setActive(false);
        }
        sendBroadcastState();
    }
    
    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }
    
    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }
    
    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }
    
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }
    
    public Song getCurrentSong() {
        if (currentIndex >= 0 && currentIndex < playlist.size()) {
            return playlist.get(currentIndex);
        }
        return null;
    }
    
    public void setShuffle(boolean shuffle) {
        isShuffle = shuffle;
    }
    
    public void setRepeatMode(int mode) {
        repeatMode = mode;
    }
    
    public boolean isShuffle() {
        return isShuffle;
    }
    
    public int getRepeatMode() {
        return repeatMode;
    }
    
    private Notification buildNotification() {
        Song song = getCurrentSong();
        String title = song != null ? song.title : "Unknown";
        String artist = song != null ? song.artist : "Unknown Artist";
        
        Intent previousIntent = new Intent(this, MusicService.class);
        previousIntent.setAction(ACTION_PREV);
        PendingIntent previousPendingIntent = PendingIntent.getService(this, 0, previousIntent, 
                PendingIntent.FLAG_IMMUTABLE);
        
        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction(isPlaying ? ACTION_PAUSE : ACTION_PLAY);
        PendingIntent playPendingIntent = PendingIntent.getService(this, 1, playIntent, 
                PendingIntent.FLAG_IMMUTABLE);
        
        Intent nextIntent = new Intent(this, MusicService.class);
        nextIntent.setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 2, nextIntent, 
                PendingIntent.FLAG_IMMUTABLE);
        
        Intent stopIntent = new Intent(this, MusicService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 3, stopIntent, 
                PendingIntent.FLAG_IMMUTABLE);
        
        Intent openIntent = new Intent(this, MusicPlayerActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openPendingIntent = PendingIntent.getActivity(this, 0, openIntent, 
                PendingIntent.FLAG_IMMUTABLE);
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(artist)
                .setSmallIcon(R.drawable.ic_music)
                .setLargeIcon(null) // Would use album art
                .setContentIntent(openPendingIntent)
                .addAction(R.drawable.ic_prev, "Previous", previousPendingIntent)
                .addAction(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play, 
                        isPlaying ? "Pause" : "Play", playPendingIntent)
                .addAction(R.drawable.ic_next, "Next", nextPendingIntent)
                .addAction(R.drawable.ic_close, "Stop", stopPendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }
    
    private void updateNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, buildNotification());
    }
    
    private void sendBroadcastState() {
        Song song = getCurrentSong();
        Intent intent = new Intent("MUSIC_STATE_CHANGED");
        intent.putExtra("isPlaying", isPlaying);
        intent.putExtra("currentPosition", getCurrentPosition());
        intent.putExtra("duration", getDuration());
        if (song != null) {
            intent.putExtra("title", song.title);
            intent.putExtra("artist", song.artist);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (mediaSession != null) {
            mediaSession.release();
        }
    }
}