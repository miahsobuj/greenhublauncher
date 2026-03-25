package com.greenhub.launcher.utils;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtils {
    
    public static Intent getOpenFileIntent(File file, Context context) {
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        String mimeType = getMimeType(file);
        
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        return intent;
    }
    
    public static Intent getShareFileIntent(File file, Context context) {
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(getMimeType(file));
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return intent;
    }
    
    public static String getMimeType(File file) {
        String extension = getExtension(file.getName());
        if (extension.isEmpty()) return "*/*";
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
    }
    
    public static String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return (lastDot > 0) ? filename.substring(lastDot + 1) : "";
    }
    
    public static boolean copyFile(File source, File dest) {
        try (FileChannel sourceChannel = new FileInputStream(source).getChannel();
             FileChannel destChannel = new FileOutputStream(dest).getChannel()) {
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static String getFileDetails(File file) {
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(file.getName()).append("\n");
        details.append("Path: ").append(file.getParent()).append("\n");
        details.append("Size: ").append(formatFileSize(file.length())).append("\n");
        details.append("Type: ").append(file.isDirectory() ? "Folder" : "File").append("\n");
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        details.append("Modified: ").append(sdf.format(new Date(file.lastModified()))).append("\n");
        
        if (!file.isDirectory()) {
            String ext = getExtension(file.getName());
            details.append("Extension: ").append(ext.isEmpty() ? "None" : ext);
        }
        
        return details.toString();
    }
    
    public static String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        int exp = (int) (Math.log(size) / Math.log(1024));
        String units = "KMGTPE".charAt(exp - 1) + "B";
        return String.format(Locale.getDefault(), "%.1f %s", size / Math.pow(1024, exp), units);
    }
    
    public static int getFileIcon(String filename) {
        String ext = getExtension(filename).toLowerCase();
        switch (ext) {
            case "jpg": case "jpeg": case "png": case "gif": case "bmp":
                return android.R.drawable.ic_menu_gallery;
            case "mp4": case "avi": case "mkv": case "mov":
                return android.R.drawable.ic_media_play;
            case "mp3": case "wav": case "flac": case "aac":
                return android.R.drawable.ic_media_ff;
            case "pdf":
                return android.R.drawable.ic_menu_view;
            case "doc": case "docx": case "txt":
                return android.R.drawable.ic_menu_edit;
            case "apk":
                return android.R.drawable.ic_menu_preferences;
            default:
                return android.R.drawable.ic_menu_info_details;
        }
    }
}
