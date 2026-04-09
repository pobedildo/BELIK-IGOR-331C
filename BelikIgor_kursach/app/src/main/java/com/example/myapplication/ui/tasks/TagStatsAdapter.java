package com.example.myapplication.ui.tasks;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.TagCount;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class TagStatsAdapter extends RecyclerView.Adapter<TagStatsAdapter.Holder> {
    private final List<TagCount> items = new ArrayList<>();

    public void setItems(List<TagCount> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag_stat, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        TagCount tc = items.get(position);
        holder.chip.setText(tc.getTag().getName());
        int color = parseColor(tc.getTag().getColor());
        holder.chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(
                Color.argb(38, Color.red(color), Color.green(color), Color.blue(color))));
        holder.chip.setTextColor(color);
        holder.count.setText(String.valueOf(tc.getTaskCount()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private int parseColor(String hex) {
        try {
            return Color.parseColor(hex);
        } catch (Exception e) {
            return Color.parseColor("#3F51B5");
        }
    }

    static class Holder extends RecyclerView.ViewHolder {
        final Chip chip;
        final TextView count;

        Holder(@NonNull View itemView) {
            super(itemView);
            chip = itemView.findViewById(R.id.chip_tag_name);
            count = itemView.findViewById(R.id.text_count);
        }
    }
}
