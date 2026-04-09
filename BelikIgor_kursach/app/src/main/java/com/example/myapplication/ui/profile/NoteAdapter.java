package com.example.myapplication.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.Note;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.Holder> {

    public interface Listener {
        void onNoteClick(Note note);

        void onNoteLongClick(Note note, View anchor);
    }

    private final List<Note> notes = new ArrayList<>();
    private final Listener listener;

    public NoteAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setNotes(List<Note> list) {
        notes.clear();
        if (list != null) notes.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Note n = notes.get(position);
        h.title.setText(n.isImportant() ? "★ " + n.getTitle() : n.getTitle());
        h.content.setText(n.getContent());
        h.itemView.setOnClickListener(v -> listener.onNoteClick(n));
        h.itemView.setOnLongClickListener(v -> {
            listener.onNoteLongClick(n, v);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView content;

        Holder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_title);
            content = itemView.findViewById(R.id.text_content);
        }
    }
}
