package com.example.myapplication.ui.notes;

import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.DatabaseHelper;
import com.example.myapplication.data.Note;
import com.example.myapplication.ui.profile.NoteAdapter;
import com.example.myapplication.util.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

public class NotesFragment extends Fragment {

    private NoteAdapter adapter;
    private DatabaseHelper db;
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = DatabaseHelper.getInstance(requireContext());
        session = new SessionManager(requireContext());

        RecyclerView rv = view.findViewById(R.id.recycler_notes);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NoteAdapter(new NoteAdapter.Listener() {
            @Override
            public void onNoteClick(Note note) {
                showNoteDialog(note.getId(), note.getTitle(), note.getContent());
            }

            @Override
            public void onNoteLongClick(Note note, View anchor) {
                PopupMenu pm = new PopupMenu(requireContext(), anchor);
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

        FloatingActionButton fab = view.findViewById(R.id.fab_add_note);
        fab.setOnClickListener(v -> showNoteDialog(-1, "", ""));

        load();
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        if (db == null || session == null) return;
        long uid = session.getUserId();
        List<Note> list = db.getNotesForUser(uid);
        if (adapter != null) adapter.setNotes(list);
    }

    private void showNoteDialog(long noteId, String title, String content) {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad / 2, pad, 0);

        EditText t = new EditText(requireContext());
        t.setHint(R.string.note_title);
        t.setText(title);
        layout.addView(t);

        EditText c = new EditText(requireContext());
        c.setHint(R.string.note_content);
        c.setMinLines(4);
        c.setText(content);
        layout.addView(c);

        com.google.android.material.checkbox.MaterialCheckBox important =
                new com.google.android.material.checkbox.MaterialCheckBox(requireContext());
        important.setText("★ " + getString(R.string.important));
        if (noteId >= 0) {
            List<Note> existing = db.getNotesForUser(session.getUserId());
            for (Note n : existing) {
                if (n.getId() == noteId) {
                    important.setChecked(n.isImportant());
                    break;
                }
            }
        }
        layout.addView(important);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle(noteId < 0 ? R.string.add_note : R.string.edit)
                .setView(layout)
                .setPositiveButton(R.string.save, (d, w) -> {
                    String nt = t.getText() != null ? t.getText().toString().trim() : "";
                    if (nt.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String nc = c.getText() != null ? c.getText().toString().trim() : "";
                    long uid = session.getUserId();
                    if (noteId < 0) {
                        db.insertNote(uid, nt, nc, important.isChecked());
                    } else {
                        db.updateNote(noteId, uid, nt, nc, important.isChecked());
                    }
                    load();
                })
                .setNegativeButton(R.string.cancel, null);
        if (noteId >= 0) {
            builder.setNeutralButton(R.string.delete, (d, w) -> {
                db.deleteNote(noteId, session.getUserId());
                load();
            });
        }
        builder.show();
    }
}
