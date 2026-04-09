package com.example.myapplication.ui.tasks;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.Tag;
import com.example.myapplication.data.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.Holder> {

    public interface Listener {
        void onCheckedChanged(Task task, boolean done);

        void onItemClick(Task task);

        void onItemLongClick(Task task, View anchor);

        void onDeleteClick(Task task);
    }

    private final List<Task> tasks = new ArrayList<>();
    private final Map<Long, List<Tag>> taskTags = new HashMap<>();
    private final Listener listener;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("d MMM yyyy, HH:mm", new Locale("ru"));

    public TaskAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setTasks(List<Task> list) {
        tasks.clear();
        if (list != null) tasks.addAll(list);
        Collections.sort(tasks, (a, b) -> {
            if (a.isImportant() != b.isImportant()) {
                return Boolean.compare(b.isImportant(), a.isImportant());
            }
            if (a.isCompleted() != b.isCompleted()) {
                return Boolean.compare(a.isCompleted(), b.isCompleted());
            }
            return Long.compare(a.getDueDateTime(), b.getDueDateTime());
        });
        notifyDataSetChanged();
    }

    public Task getTaskAt(int position) {
        return tasks.get(position);
    }

    public void setTaskTags(Map<Long, List<Tag>> tagsByTaskId) {
        taskTags.clear();
        if (tagsByTaskId != null) taskTags.putAll(tagsByTaskId);
        notifyDataSetChanged();
    }

    public void removeTask(long taskId) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId() == taskId) {
                tasks.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Task t = tasks.get(position);
        h.title.setText(t.isImportant() ? "★ " + t.getTitle() : t.getTitle());
        String desc = t.getDescription();
        if (desc == null || desc.isEmpty()) {
            h.description.setVisibility(View.GONE);
        } else {
            h.description.setVisibility(View.VISIBLE);
            h.description.setText(desc);
        }
        h.due.setText(dateFormat.format(new Date(t.getDueDateTime())));
        bindTags(h.chipsTags, taskTags.get(t.getId()));

        h.check.setOnCheckedChangeListener(null);
        h.check.setChecked(t.isCompleted());
        applyStrike(h.title, h.description, t.isCompleted());

        h.check.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applyStrike(h.title, h.description, isChecked);
            listener.onCheckedChanged(t, isChecked);
        });

        h.deleteDone.setVisibility(t.isCompleted() ? View.VISIBLE : View.GONE);
        h.deleteDone.setOnClickListener(v -> listener.onDeleteClick(t));

        h.itemView.setOnClickListener(v -> listener.onItemClick(t));
        h.itemView.setOnLongClickListener(v -> {
            listener.onItemLongClick(t, v);
            return true;
        });
    }

    private void bindTags(ChipGroup group, List<Tag> tags) {
        group.removeAllViews();
        if (tags == null || tags.isEmpty()) {
            group.setVisibility(View.GONE);
            return;
        }
        group.setVisibility(View.VISIBLE);
        for (Tag tag : tags) {
            Chip chip = new Chip(group.getContext());
            chip.setText(tag.getName());
            chip.setCheckable(false);
            int color = parseColor(tag.getColor());
            chip.setChipBackgroundColorResource(android.R.color.transparent);
            chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(ColorUtils.setAlphaComponent(color, 48)));
            chip.setTextColor(color);
            group.addView(chip);
        }
    }

    private static int parseColor(String hex) {
        try {
            return android.graphics.Color.parseColor(hex);
        } catch (Exception e) {
            return android.graphics.Color.parseColor("#3F51B5");
        }
    }

    private static void applyStrike(TextView title, TextView description, boolean done) {
        int f = title.getPaintFlags();
        if (done) {
            title.setPaintFlags(f | Paint.STRIKE_THRU_TEXT_FLAG);
            description.setPaintFlags(description.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            title.setAlpha(0.6f);
            description.setAlpha(0.6f);
        } else {
            title.setPaintFlags(f & ~Paint.STRIKE_THRU_TEXT_FLAG);
            description.setPaintFlags(description.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            title.setAlpha(1f);
            description.setAlpha(1f);
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final CheckBox check;
        final TextView title;
        final TextView description;
        final TextView due;
        final ChipGroup chipsTags;
        final ImageButton deleteDone;

        Holder(@NonNull View itemView) {
            super(itemView);
            check = itemView.findViewById(R.id.check_done);
            title = itemView.findViewById(R.id.text_title);
            description = itemView.findViewById(R.id.text_description);
            due = itemView.findViewById(R.id.text_due);
            chipsTags = itemView.findViewById(R.id.chips_tags);
            deleteDone = itemView.findViewById(R.id.btn_delete_done);
        }
    }
}
