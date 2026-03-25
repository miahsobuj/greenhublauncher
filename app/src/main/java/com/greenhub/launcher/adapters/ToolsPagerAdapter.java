package com.greenhub.launcher.adapters;

import android.content.Intent;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.greenhub.launcher.CalculatorActivity;
import com.greenhub.launcher.FileManagerActivity;
import com.greenhub.launcher.BrowserActivity;
import com.greenhub.launcher.ContactsActivity;
import com.greenhub.launcher.MusicPlayerActivity;
import com.greenhub.launcher.NotepadActivity;
import com.greenhub.launcher.PhoneActivity;
import com.greenhub.launcher.R;
import com.greenhub.launcher.VideoDownloaderActivity;

import java.util.ArrayList;
import java.util.List;

public class ToolsPagerAdapter extends RecyclerView.Adapter<ToolsPagerAdapter.ToolsViewHolder> {

    private Context context;
    private List<ToolItem> tools;

    static class ToolItem {
        String name;
        int iconRes;
        Class<?> activity;

        ToolItem(String name, int iconRes, Class<?> activity) {
            this.name = name;
            this.iconRes = iconRes;
            this.activity = activity;
        }
    }

    public ToolsPagerAdapter(Context context) {
        this.context = context;
        this.tools = new ArrayList<>();
        
        // Initialize built-in tools
        tools.add(new ToolItem("File Manager", R.drawable.ic_folder, FileManagerActivity.class));
        tools.add(new ToolItem("Calculator", R.drawable.ic_calculator, CalculatorActivity.class));
        tools.add(new ToolItem("Browser", R.drawable.ic_browser, BrowserActivity.class));
        tools.add(new ToolItem("Notepad", R.drawable.ic_note, NotepadActivity.class));
        tools.add(new ToolItem("Video Downloader", R.drawable.ic_download, VideoDownloaderActivity.class));
        tools.add(new ToolItem("Music Player", R.drawable.ic_music, MusicPlayerActivity.class));
        tools.add(new ToolItem("Phone", R.drawable.ic_phone, PhoneActivity.class));
        tools.add(new ToolItem("Contacts", R.drawable.ic_contacts, ContactsActivity.class));
    }

    @NonNull
    @Override
    public ToolsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tools_grid, parent, false);
        return new ToolsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToolsViewHolder holder, int position) {
        int itemsPerPage = 6;
        int start = position * itemsPerPage;
        int end = Math.min(start + itemsPerPage, tools.size());
        
        List<ToolItem> pageTools = tools.subList(start, end);
        holder.bind(pageTools);
    }

    @Override
    public int getItemCount() {
        return (int) Math.ceil(tools.size() / 6.0);
    }

    class ToolsViewHolder extends RecyclerView.ViewHolder {
        LinearLayout container;
        List<View> toolViews = new ArrayList<>();

        ToolsViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.tools_container);
        }

        void bind(List<ToolItem> pageTools) {
            container.removeAllViews();
            
            for (int i = 0; i < pageTools.size(); i += 3) {
                LinearLayout row = new LinearLayout(context);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                
                for (int j = i; j < Math.min(i + 3, pageTools.size()); j++) {
                    ToolItem tool = pageTools.get(j);
                    View toolView = createToolView(tool);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                    params.setMargins(16, 8, 16, 8);
                    toolView.setLayoutParams(params);
                    row.addView(toolView);
                }
                
                container.addView(row);
            }
        }

        View createToolView(ToolItem tool) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_tool_card, null);
            ImageView icon = view.findViewById(R.id.tool_icon);
            TextView name = view.findViewById(R.id.tool_name);
            
            icon.setImageResource(tool.iconRes);
            name.setText(tool.name);
            
            view.setOnClickListener(v -> {
                Intent intent = new Intent(context, tool.activity);
                context.startActivity(intent);
            });
            
            return view;
        }
    }
}
