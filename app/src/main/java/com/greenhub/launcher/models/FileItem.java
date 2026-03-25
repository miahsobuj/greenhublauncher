package com.greenhub.launcher.models;

import java.io.File;

public class FileItem {
    private File file;
    private String name;
    private boolean isDirectory;
    private long size;
    private long lastModified;

    public FileItem(File file) {
        this.file = file;
        this.name = file.getName();
        this.isDirectory = file.isDirectory();
        this.size = file.length();
        this.lastModified = file.lastModified();
    }

    public File getFile() { return file; }
    public String getName() { return name; }
    public boolean isDirectory() { return isDirectory; }
    public long getSize() { return size; }
    public long getLastModified() { return lastModified; }
    public String getExtension() {
        if (isDirectory) return "";
        int dotIndex = name.lastIndexOf('.');
        return (dotIndex > 0) ? name.substring(dotIndex + 1) : "";
    }
}
