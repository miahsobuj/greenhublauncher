package com.greenhub.launcher;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.greenhub.launcher.adapters.NotesAdapter;
import com.greenhub.launcher.models.Note;
import com.greenhub.launcher.utils.NoteManager;

import java.util.List;

public class NotepadActivity extends AppCompatActivity implements NotesAdapter.OnNoteClickListener {

    private RecyclerView recyclerView;
    private NotesAdapter notesAdapter;
    private View emptyView;
    private NoteManager noteManager;
    private List<Note> noteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notepad);
        
        noteManager = new NoteManager(this);
        initViews();
        loadNotes();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recycler_notes);
        emptyView = findViewById(R.id.empty_view);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        FloatingActionButton fab = findViewById(R.id.fab_add_note);
        fab.setOnClickListener(v -> showNoteEditDialog(null));
        
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
    
    private void loadNotes() {
        noteList = noteManager.getAllNotes();
        notesAdapter = new NotesAdapter(this, noteList, this);
        recyclerView.setAdapter(notesAdapter);
        updateEmptyView();
    }
    
    private void updateEmptyView() {
        if (noteList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public void onNoteClick(Note note) {
        showNoteEditDialog(note);
    }
    
    @Override
    public void onNoteLongClick(Note note, View view) {
        showNoteOptions(note);
    }
    
    private void showNoteEditDialog(Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.GreenDialog);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_note_edit, null);
        
        EditText titleInput = view.findViewById(R.id.note_title);
        EditText contentInput = view.findViewById(R.id.note_content);
        
        if (note != null) {
            titleInput.setText(note.getTitle());
            contentInput.setText(note.getContent());
        }
        
        boolean isNewNote = (note == null);
        String dialogTitle = isNewNote ? "New Note" : "Edit Note";
        
        builder.setView(view)
                .setTitle(dialogTitle)
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = titleInput.getText().toString().trim();
                    String content = contentInput.getText().toString().trim();
                    
                    if (title.isEmpty()) {
                        title = "Untitled";
                    }
                    
                    if (isNewNote) {
                        Note newNote = new Note(title, content, System.currentTimeMillis());
                        noteManager.saveNote(newNote);
                        Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
                    } else {
                        note.setTitle(title);
                        note.setContent(content);
                        note.setTimestamp(System.currentTimeMillis());
                        noteManager.updateNote(note);
                        Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
                    }
                    loadNotes();
                })
                .setNegativeButton("Cancel", null);
        
        if (!isNewNote) {
            builder.setNeutralButton("Delete", (dialog, which) -> {
                confirmDeleteNote(note);
            });
        }
        
        builder.show();
    }
    
    private void showNoteOptions(Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.GreenDialog);
        String[] options = {"Edit", "Delete", "Share"};
        
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    showNoteEditDialog(note);
                    break;
                case 1:
                    confirmDeleteNote(note);
                    break;
                case 2:
                    shareNote(note);
                    break;
            }
        });
        
        builder.show();
    }
    
    private void confirmDeleteNote(Note note) {
        new AlertDialog.Builder(this, R.style.GreenDialog)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete \"" + note.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    noteManager.deleteNote(note.getId());
                    Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
                    loadNotes();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void shareNote(Note note) {
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, note.getTitle());
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, note.getTitle() + "\n\n" + note.getContent());
        startActivity(android.content.Intent.createChooser(shareIntent, "Share via"));
    }
}