package com.greenhub.launcher.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.greenhub.launcher.R;
import com.greenhub.launcher.models.FileItem;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private Context context;
    private List<FileItem> files;
    private OnFileClickListener listener;

    public interface OnFileClickListener {
        void onFileClick(FileItem file);
        void onFileLongClick(FileItem file, View view);
    }

    public FileAdapter(Context context, List<FileItem> files, OnFileClickListener listener) {
        this.context = context;
        this.files = files;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileItem file = files.get(position);
        holder.bind(file);
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    class FileViewHolder extends RecyclerView.ViewHolder {
        ImageView fileIcon;
        TextView fileName;
        TextView fileSize;

        FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileIcon = itemView.findViewById(R.id.file_icon);
            fileName = itemView.findViewById(R.id.file_name);
            fileSize = itemView.findViewById(R.id.file_size);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onFileClick(files.get(pos));
                }
            });

            itemView.setOnLongClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onFileLongClick(files.get(pos), v);
                }
                return true;
            });
        }

        void bind(FileItem file) {
            fileName.setText(file.getName());
            fileSize.setText(file.getSize());
            
            // Set appropriate icon based on file type
            if (file.isDirectory()) {
                fileIcon.setImageResource(R.drawable.ic_folder);
            } else {
                fileIcon.setImageResource(R.drawable.ic_note);
            }
        }
    }
}