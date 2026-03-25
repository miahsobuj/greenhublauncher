package com.greenhub.launcher.models;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private String name;
    private String packageName;
    private String activityName;
    private Drawable icon;

    public AppInfo(String name, String packageName, String activityName, Drawable icon) {
        this.name = name;
        this.packageName = packageName;
        this.activityName = activityName;
        this.icon = icon;
    }

    public String getName() { return name; }
    public String getPackageName() { return packageName; }
    public String getActivityName() { return activityName; }
    public Drawable getIcon() { return icon; }
}
