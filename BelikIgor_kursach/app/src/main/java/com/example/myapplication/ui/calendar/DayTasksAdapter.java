package com.example.myapplication.ui.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DayTasksAdapter extends RecyclerView.Adapter<DayTasksAdapter.Holder> {

    public interface Listener {
        void onTaskClick(Task task);
    }

    private final List<Task> tasks = new ArrayList<>();
    private final Listener listener;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public DayTasksAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setTasks(List<Task> list) {
        tasks.clear();
        if (list != null) tasks.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day_task, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Task t = tasks.get(position);
        h.title.setText(t.getTitle());
        h.due.setText(timeFormat.format(new Date(t.getDueDateTime())));
        h.itemView.setOnClickListener(v -> listener.onTaskClick(t));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView due;

        Holder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_title);
            due = itemView.findViewById(R.id.text_due);
        }
    }
}
