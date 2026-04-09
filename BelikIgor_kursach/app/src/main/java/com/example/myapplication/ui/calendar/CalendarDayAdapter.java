package com.example.myapplication.ui.calendar;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.Holder> {

    public static class DayCell {
        public final int displayDay;
        public final boolean inCurrentMonth;
        public final long dayStartMs;
        public final boolean hasTask;
        public final boolean isToday;

        public DayCell(int displayDay, boolean inCurrentMonth, long dayStartMs,
                       boolean hasTask, boolean isToday) {
            this.displayDay = displayDay;
            this.inCurrentMonth = inCurrentMonth;
            this.dayStartMs = dayStartMs;
            this.hasTask = hasTask;
            this.isToday = isToday;
        }
    }

    public interface OnDayClickListener {
        void onDayClick(DayCell cell);
    }

    private final List<DayCell> cells = new ArrayList<>();
    private final OnDayClickListener listener;

    public CalendarDayAdapter(OnDayClickListener listener) {
        this.listener = listener;
    }

    public void setMonth(int year, int monthZeroBased, Set<Integer> daysWithTasks, long todayStartMs) {
        cells.clear();

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.YEAR, year);
        cal.set(java.util.Calendar.MONTH, monthZeroBased);
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);

        int firstDow = cal.get(java.util.Calendar.DAY_OF_WEEK);
        int firstOfWeek = cal.getFirstDayOfWeek();
        int offset = (firstDow - firstOfWeek + 7) % 7;
        cal.add(java.util.Calendar.DAY_OF_MONTH, -offset);

        java.util.Calendar end = (java.util.Calendar) cal.clone();
        end.add(java.util.Calendar.DAY_OF_MONTH, 41);

        java.util.Calendar iter = (java.util.Calendar) cal.clone();
        while (!iter.after(end)) {
            boolean inMonth = iter.get(java.util.Calendar.MONTH) == monthZeroBased;
            int day = iter.get(java.util.Calendar.DAY_OF_MONTH);
            long start = iter.getTimeInMillis();
            boolean has = inMonth && daysWithTasks != null && daysWithTasks.contains(day);
            boolean today = sameDay(start, todayStartMs);
            cells.add(new DayCell(day, inMonth, start, has, today));
            iter.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }
        notifyDataSetChanged();
    }

    private static boolean sameDay(long aMs, long bMs) {
        java.util.Calendar ca = java.util.Calendar.getInstance();
        ca.setTimeInMillis(aMs);
        java.util.Calendar cb = java.util.Calendar.getInstance();
        cb.setTimeInMillis(bMs);
        return ca.get(java.util.Calendar.YEAR) == cb.get(java.util.Calendar.YEAR)
                && ca.get(java.util.Calendar.DAY_OF_YEAR) == cb.get(java.util.Calendar.DAY_OF_YEAR);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_cell, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        DayCell c = cells.get(position);
        h.day.setText(String.valueOf(c.displayDay));
        float alpha = c.inCurrentMonth ? 1f : 0.35f;
        h.day.setAlpha(alpha);
        h.dot.setVisibility(c.hasTask ? View.VISIBLE : View.GONE);
        h.dot.setAlpha(alpha);

        int bg = android.R.color.transparent;
        h.itemView.setBackgroundColor(ContextCompat.getColor(h.itemView.getContext(), bg));
        if (c.isToday) {
            h.itemView.setBackgroundResource(R.drawable.calendar_today_bg);
        }
        h.day.setTypeface(null, c.isToday ? Typeface.BOLD : Typeface.NORMAL);

        h.itemView.setOnClickListener(v -> listener.onDayClick(c));
    }

    @Override
    public int getItemCount() {
        return cells.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView day;
        final View dot;

        Holder(@NonNull View itemView) {
            super(itemView);
            day = itemView.findViewById(R.id.text_day);
            dot = itemView.findViewById(R.id.dot);
        }
    }
}
