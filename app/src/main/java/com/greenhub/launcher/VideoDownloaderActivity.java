package com.greenhub.launcher;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Patterns;
import android.view.View;
import android.webkit.*;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoDownloaderActivity extends AppCompatActivity {

    private EditText urlInput;
    private Button pasteButton, downloadButton;
    private ProgressBar progressBar;
    private TextView statusText;
    private WebView webView;
    private Spinner qualitySpinner;
    
    private static final String[] SUPPORTED_SITES = {
        "youtube.com", "youtu.be", "instagram.com", "facebook.com",
        "fb.watch", "twitter.com", "x.com", "tiktok.com", "reddit.com"
    };

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_downloader);
        
        initViews();
        setupWebView();
        
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
    
    private void initViews() {
        urlInput = findViewById(R.id.url_input);
        pasteButton = findViewById(R.id.btn_paste);
        downloadButton = findViewById(R.id.btn_download);
        progressBar = findViewById(R.id.progress_bar);
        statusText = findViewById(R.id.status_text);
        webView = findViewById(R.id.webview);
        qualitySpinner = findViewById(R.id.quality_spinner);
        
        pasteButton.setOnClickListener(v -> pasteUrl());
        downloadButton.setOnClickListener(v -> startDownload());
    }
    
    private void pasteUrl() {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip()) {
            android.content.ClipData clip = clipboard.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                CharSequence url = clip.getItemAt(0).getText();
                if (url != null) {
                    urlInput.setText(url.toString());
                }
            }
        }
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }
        });
    }
    
    private void startDownload() {
        String url = urlInput.getText().toString().trim();
        
        if (url.isEmpty()) {
            Toast.makeText(this, "Please enter a URL", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!isValidUrl(url)) {
            Toast.makeText(this, "Please enter a valid URL", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!isSupportedSite(url)) {
            Toast.makeText(this, "Site not supported. Try YouTube, Instagram, Facebook, Twitter, TikTok", Toast.LENGTH_LONG).show();
            return;
        }
        
        String quality = qualitySpinner.getSelectedItem().toString();
        String downloadUrl = convertToDownloadUrl(url, quality);
        
        if (downloadUrl != null) {
            downloadFile(downloadUrl, getFileNameFromUrl(url));
        } else {
            // For sites that don't support direct download, open web view
            openForWebDownload(url);
        }
    }
    
    private boolean isValidUrl(String url) {
        return Patterns.WEB_URL.matcher(url).matches();
    }
    
    private boolean isSupportedSite(String url) {
        String lowerUrl = url.toLowerCase();
        for (String site : SUPPORTED_SITES) {
            if (lowerUrl.contains(site)) {
                return true;
            }
        }
        return false;
    }
    
    private String convertToDownloadUrl(String originalUrl, String quality) {
        // Convert YouTube URL to download link
        if (originalUrl.contains("youtube.com") || originalUrl.contains("youtu.be")) {
            // Note: This is a simplified example. Real implementation would
            // use a YouTube DL library or API
            return null; // Would require yt-dlp integration
        }
        return null;
    }
    
    private void openForWebDownload(String url) {
        statusText.setText("Opening in browser for download...");
        progressBar.setVisibility(View.VISIBLE);
        
        webView.setDownloadListener((downloadUrl, userAgent, contentDisposition, mimeType, contentLength) -> {
            String fileName = URLUtil.guessFileName(downloadUrl, contentDisposition, mimeType);
            downloadFile(downloadUrl, fileName);
        });
        
        webView.loadUrl(url);
        
        new android.os.Handler().postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
        }, 5000);
    }
    
    private void downloadFile(String url, String fileName) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(fileName);
        request.setDescription("Downloading video...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        request.allowScanningByMediaScanner();
        
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        try {
            long downloadId = downloadManager.enqueue(request);
            statusText.setText("Download started...");
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Download started: " + fileName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            statusText.setText("Download failed");
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private String getFileNameFromUrl(String url) {
        String name = "video_" + System.currentTimeMillis() + ".mp4";
        try {
            Uri uri = Uri.parse(url);
            String path = uri.getLastPathSegment();
            if (path != null && !path.isEmpty()) {
                name = path;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }
}