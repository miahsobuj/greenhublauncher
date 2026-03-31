package com.greenhub.launcher;

import android.Manifest;
import android.content.Context;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.greenhub.launcher.adapters.FileAdapter;
import com.greenhub.launcher.models.FileItem;
import com.greenhub.launcher.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileManagerActivity extends AppCompatActivity implements FileAdapter.OnFileClickListener {

    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private LinearLayout emptyView;
    private TextView currentPathText;
    private ImageView backButton;
    private TextView storageText;
    private LinearLayout storageLayout;
    
    private List<FileItem> fileList = new ArrayList<>();
    private File currentDirectory;
    private File copiedFile;
    private File cutFile;
    private boolean isCopyMode = false;
    
    private static final int STORAGE_PERMISSION_CODE = 100;
    private static final String ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_manager);
        
        initViews();
        checkStoragePermission();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recycler_files);
        emptyView = findViewById(R.id.empty_view);
        currentPathText = findViewById(R.id.current_path);
        backButton = findViewById(R.id.btn_back);
        storageText = findViewById(R.id.storage_text);
        storageLayout = findViewById(R.id.storage_layout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        backButton.setOnClickListener(v -> goBack());

        FloatingActionButton fab = findViewById(R.id.fab_new_folder);
        fab.setOnClickListener(v -> showNewFolderDialog());
    }
    
    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                loadFiles(ROOT_PATH);
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, STORAGE_PERMISSION_CODE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            } else {
                loadFiles(ROOT_PATH);
            }
        }
    }
    
    private void loadFiles(String path) {
        currentDirectory = new File(path);
        currentPathText.setText(getDisplayPath(path));

        fileList.clear();
        File[] files = currentDirectory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (!file.isHidden()) {
                    fileList.add(new FileItem(file));
                }
            }
        }

        Collections.sort(fileList, (a, b) -> {
            if (a.isDirectory() != b.isDirectory()) {
                return a.isDirectory() ? -1 : 1;
            }
            return a.getName().compareToIgnoreCase(b.getName());
        });

        fileAdapter = new FileAdapter(this, fileList, this);
        recyclerView.setAdapter(fileAdapter);
        updateEmptyView();
        updateStorageInfo();
    }
    
    private String getDisplayPath(String path) {
        if (path.equals(ROOT_PATH)) {
            return "Internal Storage";
        }
        return path.replace(ROOT_PATH, "Internal Storage");
    }

    private void updateStorageInfo() {
        if (currentDirectory != null) {
            long totalSpace = currentDirectory.getTotalSpace();
            long freeSpace = currentDirectory.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;

            String storageInfo = String.format(
                "Storage: %s used of %s",
                formatFileSize(usedSpace),
                formatFileSize(totalSpace)
            );

            storageText.setText(storageInfo);

            // Show storage usage as a percentage (avoid division by zero)
            if (totalSpace > 0) {
                int usagePercent = (int) ((usedSpace * 100) / totalSpace);
                if (usagePercent > 90) {
                    storageText.setTextColor(ContextCompat.getColor(this, R.color.error_red));
                } else if (usagePercent > 75) {
                    storageText.setTextColor(ContextCompat.getColor(this, R.color.warning_orange));
                } else {
                    storageText.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                }
            } else {
                // If we can't get total space, use default text color
                storageText.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            }
        }
    }
    
    private void updateEmptyView() {
        if (fileList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    private void goBack() {
        if (currentDirectory != null && !currentDirectory.getAbsolutePath().equals(ROOT_PATH)) {
            loadFiles(currentDirectory.getParent());
        } else {
            finish();
        }
    }
    
    @Override
    public void onFileClick(FileItem fileItem) {
        if (fileItem.isDirectory()) {
            loadFiles(fileItem.getFile().getAbsolutePath());
        } else {
            openFile(fileItem.getFile());
        }
    }
    
    @Override
    public void onFileLongClick(FileItem fileItem, View view) {
        showFileOptions(fileItem, view);
    }
    
    private void openFile(File file) {
        Intent intent = FileUtils.getOpenFileIntent(file, this);
        if (intent != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Cannot open this file type", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showFileOptions(FileItem fileItem, View anchorView) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.getMenuInflater().inflate(R.menu.file_options, popup.getMenu());
        
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_open) {
                onFileClick(fileItem);
            } else if (id == R.id.action_copy) {
                copyFile(fileItem.getFile());
            } else if (id == R.id.action_cut) {
                cutFile(fileItem.getFile());
            } else if (id == R.id.action_rename) {
                showRenameDialog(fileItem.getFile());
            } else if (id == R.id.action_delete) {
                showDeleteDialog(fileItem.getFile());
            } else if (id == R.id.action_share) {
                shareFile(fileItem.getFile());
            } else if (id == R.id.action_details) {
                showFileDetails(fileItem);
            }
            return true;
        });
        popup.show();
    }
    
    private void showNewFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.GreenDialog);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_input, null);
        EditText input = view.findViewById(R.id.input_text);
        input.setHint("Folder name");
        
        builder.setView(view)
                .setTitle("Create New Folder")
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        createFolder(name);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void createFolder(String name) {
        File newFolder = new File(currentDirectory, name);
        if (newFolder.mkdir()) {
            Toast.makeText(this, "Folder created", Toast.LENGTH_SHORT).show();
            loadFiles(currentDirectory.getAbsolutePath());
        } else {
            Toast.makeText(this, "Failed to create folder", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void copyFile(File file) {
        copiedFile = file;
        cutFile = null;
        isCopyMode = true;
        Toast.makeText(this, "File copied", Toast.LENGTH_SHORT).show();
    }
    
    private void cutFile(File file) {
        cutFile = file;
        copiedFile = null;
        isCopyMode = true;
        Toast.makeText(this, "File cut", Toast.LENGTH_SHORT).show();
    }
    
    private void pasteFile() {
        if (copiedFile != null) {
            // Copy logic
            File dest = new File(currentDirectory, copiedFile.getName());
            if (FileUtils.copyFile(copiedFile, dest)) {
                Toast.makeText(this, "File pasted", Toast.LENGTH_SHORT).show();
                loadFiles(currentDirectory.getAbsolutePath());
            }
        } else if (cutFile != null) {
            // Move logic
            File dest = new File(currentDirectory, cutFile.getName());
            if (cutFile.renameTo(dest)) {
                Toast.makeText(this, "File moved", Toast.LENGTH_SHORT).show();
                loadFiles(currentDirectory.getAbsolutePath());
            }
        }
        copiedFile = null;
        cutFile = null;
        isCopyMode = false;
    }
    
    private void showRenameDialog(File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.GreenDialog);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_input, null);
        EditText input = view.findViewById(R.id.input_text);
        input.setText(file.getName());
        
        builder.setView(view)
                .setTitle("Rename")
                .setPositiveButton("Rename", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        File newFile = new File(file.getParent(), name);
                        if (file.renameTo(newFile)) {
                            Toast.makeText(this, "Renamed", Toast.LENGTH_SHORT).show();
                            loadFiles(currentDirectory.getAbsolutePath());
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void showDeleteDialog(File file) {
        new AlertDialog.Builder(this, R.style.GreenDialog)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete \"" + file.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (file.delete()) {
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                        loadFiles(currentDirectory.getAbsolutePath());
                    } else {
                        Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void shareFile(File file) {
        Intent intent = FileUtils.getShareFileIntent(file, this);
        startActivity(Intent.createChooser(intent, "Share via"));
    }
    
    private void showFileDetails(FileItem fileItem) {
        String details = FileUtils.getFileDetails(fileItem.getFile());
        new AlertDialog.Builder(this, R.style.GreenDialog)
                .setTitle("File Details")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }
    
    @Override
    public void onBackPressed() {
        goBack();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadFiles(ROOT_PATH);
            } else {
                Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}