package com.greenhub.launcher;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.greenhub.launcher.adapters.AppAdapter;
import com.greenhub.launcher.adapters.ToolsPagerAdapter;
import com.greenhub.launcher.models.AppInfo;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AppAdapter.OnAppClickListener {
    
    private RecyclerView recyclerView;
    private AppAdapter appAdapter;
    private List<AppInfo> appList = new ArrayList<>();
    private List<AppInfo> filteredList = new ArrayList<>();
    private EditText searchBox;
    private ViewPager2 toolsPager;
    private LinearLayout dotsContainer;
    private PackageManager pm;
    
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.INTERNET,
            Manifest.permission.CALL_PHONE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        pm = getPackageManager();
        initViews();
        checkPermissions();
        loadApps();
        setupToolsPager();
        setupSearch();
        setupDateTime();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recycler_apps);
        searchBox = findViewById(R.id.search_box);
        toolsPager = findViewById(R.id.tools_pager);
        dotsContainer = findViewById(R.id.dots_container);
        
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
    }
    
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        } else {
            List<String> permissionsToRequest = new ArrayList<>();
            for (String permission : REQUIRED_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(permission);
                }
            }
            if (!permissionsToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(this, 
                        permissionsToRequest.toArray(new String[0]), PERMISSION_REQUEST_CODE);
            }
        }
    }
    
    private void loadApps() {
        new Thread(() -> {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            
            List<ResolveInfo> availableActivities = pm.queryIntentActivities(intent, 0);
            appList.clear();
            
            for (ResolveInfo ri : availableActivities) {
                String appName = ri.loadLabel(pm).toString();
                String packageName = ri.activityInfo.packageName;
                
                // Don't show this launcher in the app list
                if (!packageName.equals(getPackageName())) {
                    AppInfo appInfo = new AppInfo(appName, packageName, ri.activityInfo.name, ri.loadIcon(pm));
                    appList.add(appInfo);
                }
            }
            
            Collections.sort(appList, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
            filteredList.clear();
            filteredList.addAll(appList);
            
            new Handler(Looper.getMainLooper()).post(() -> {
                appAdapter = new AppAdapter(MainActivity.this, filteredList, MainActivity.this);
                recyclerView.setAdapter(appAdapter);
            });
        }).start();
    }
    
    private void setupToolsPager() {
        ToolsPagerAdapter pagerAdapter = new ToolsPagerAdapter(this);
        toolsPager.setAdapter(pagerAdapter);
        
        toolsPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
            }
        });
        
        setupPagerDots(pagerAdapter.getItemCount());
    }
    
    private void setupPagerDots(int count) {
        dotsContainer.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(12, 12);
            params.setMargins(4, 0, 4, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(i == 0 ? R.drawable.dot_active : R.drawable.dot_inactive);
            dotsContainer.addView(dot);
        }
    }
    
    private void updateDots(int position) {
        for (int i = 0; i < dotsContainer.getChildCount(); i++) {
            View dot = dotsContainer.getChildAt(i);
            dot.setBackgroundResource(i == position ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }
    
    private void setupSearch() {
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @n            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterApps(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void filterApps(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(appList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (AppInfo app : appList) {
                if (app.getName().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(app);
                }
            }
        }
        if (appAdapter != null) {
            appAdapter.notifyDataSetChanged();
        }
    }
    
    private void setupDateTime() {
        TextView dateText = findViewById(R.id.date_text);
        TextView timeText = findViewById(R.id.time_text);
        
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("EEEE, MMMM d", java.util.Locale.getDefault());
        java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        
        Handler timeHandler = new Handler(Looper.getMainLooper());
        Runnable timeUpdater = new Runnable() {
            @Override
            public void run() {
                dateText.setText(dateFormat.format(new java.util.Date()));
                timeText.setText(timeFormat.format(new java.util.Date()));
                timeHandler.postDelayed(this, 1000);
            }
        };
        timeHandler.post(timeUpdater);
    }
    
    @Override
    public void onAppClick(AppInfo app) {
        Intent intent = pm.getLaunchIntentForPackage(app.getPackageName());
        if (intent != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Cannot open app", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onAppLongClick(AppInfo app, View view) {
        showAppOptions(app, view);
    }
    
    private void showAppOptions(AppInfo app, View anchorView) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.GreenDialog);
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_app_options, null);
        
        sheetView.findViewById(R.id.option_open).setOnClickListener(v -> {
            onAppClick(app);
            dialog.dismiss();
        });
        
        sheetView.findViewById(R.id.option_info).setOnClickListener(v -> {
            showAppInfo(app);
            dialog.dismiss();
        });
        
        sheetView.findViewById(R.id.option_uninstall).setOnClickListener(v -> {
            uninstallApp(app);
            dialog.dismiss();
        });
        
        dialog.setContentView(sheetView);
        dialog.show();
    }
    
    private void showAppInfo(AppInfo app) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + app.getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Could not open app info", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void uninstallApp(AppInfo app) {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + app.getPackageName()));
        startActivity(intent);
    }
    
    @Override
    public void onBackPressed() {
        // Stay on home screen - don't exit
        super.onBackPressed();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadApps(); // Refresh app list
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    // Permission denied
                }
            }
        }
    }
}