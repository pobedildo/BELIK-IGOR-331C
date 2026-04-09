package com.example.myapplication.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.DatabaseHelper;
import com.example.myapplication.data.Note;
import com.example.myapplication.util.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class NotesActivity extends AppCompatActivity {

    private NoteAdapter adapter;
    private DatabaseHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        db = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        RecyclerView rv = findViewById(R.id.recycler_notes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoteAdapter(new NoteAdapter.Listener() {
            @Override
            public void onNoteClick(Note note) {
                showNoteDialog(note.getId(), note.getTitle(), note.getContent());
            }

            @Override
            public void onNoteLongClick(Note note, android.view.View anchor) {
                PopupMenu pm = new PopupMenu(NotesActivity.this, anchor);
                pm.getMenu().add(0, 1, 0, R.string.delete);
                pm.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 1) {
                        db.deleteNote(note.getId(), session.getUserId());
                        load();
                    }
                    return true;
                });
                pm.show();
            }
        });
        rv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_add_note);
        fab.setOnClickListener(v -> showNoteDialog(-1, "", ""));

        load();
    }

    private void load() {
        long uid = session.getUserId();
        List<Note> list = db.getNotesForUser(uid);
        adapter.setNotes(list);
    }

    private void showNoteDialog(long noteId, String title, String content) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad / 2, pad, 0);

        EditText t = new EditText(this);
        t.setHint(R.string.note_title);
        t.setText(title);
        layout.addView(t);

        EditText c = new EditText(this);
        c.setHint(R.string.note_content);
        c.setMinLines(4);
        c.setText(content);
        layout.addView(c);

        new AlertDialog.Builder(this)
                .setTitle(noteId < 0 ? R.string.add_note : R.string.edit)
                .setView(layout)
                .setPositiveButton(R.string.save, (d, w) -> {
                    String nt = t.getText() != null ? t.getText().toString().trim() : "";
                    if (nt.isEmpty()) {
                        Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String nc = c.getText() != null ? c.getText().toString().trim() : "";
                    long uid = session.getUserId();
                    if (noteId < 0) {
                        db.insertNote(uid, nt, nc, false);
                    } else {
                        db.updateNote(noteId, uid, nt, nc, false);
                    }
                    load();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
