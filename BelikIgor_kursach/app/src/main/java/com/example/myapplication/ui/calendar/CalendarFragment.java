package com.example.myapplication.ui.calendar;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.DatabaseHelper;
import com.example.myapplication.data.Task;
import com.example.myapplication.ui.tasks.TaskEditActivity;
import com.example.myapplication.util.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CalendarFragment extends Fragment {

    private DatabaseHelper db;
    private SessionManager session;
    private CalendarDayAdapter calendarAdapter;
    private TextView monthTitle;
    private int displayYear;
    private int displayMonth;

    private final ActivityResultLauncher<Intent> taskEditor =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> refreshMonth());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = DatabaseHelper.getInstance(requireContext());
        session = new SessionManager(requireContext());

        Calendar now = Calendar.getInstance();
        displayYear = now.get(Calendar.YEAR);
        displayMonth = now.get(Calendar.MONTH);

        monthTitle = view.findViewById(R.id.text_month);
        RecyclerView grid = view.findViewById(R.id.recycler_calendar);
        grid.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        calendarAdapter = new CalendarDayAdapter(this::onDayClick);
        grid.setAdapter(calendarAdapter);

        view.findViewById(R.id.btn_prev).setOnClickListener(v -> {
            displayMonth--;
            if (displayMonth < 0) {
                displayMonth = 11;
                displayYear--;
            }
            refreshMonth();
        });
        view.findViewById(R.id.btn_next).setOnClickListener(v -> {
            displayMonth++;
            if (displayMonth > 11) {
                displayMonth = 0;
                displayYear++;
            }
            refreshMonth();
        });

        refreshMonth();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshMonth();
    }

    private long startOfTodayMs() {
        Calendar t = Calendar.getInstance();
        t.set(Calendar.HOUR_OF_DAY, 0);
        t.set(Calendar.MINUTE, 0);
        t.set(Calendar.SECOND, 0);
        t.set(Calendar.MILLISECOND, 0);
        return t.getTimeInMillis();
    }

    private void refreshMonth() {
        SimpleDateFormat fmt = new SimpleDateFormat("LLLL yyyy", new Locale("ru"));
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, displayYear);
        cal.set(Calendar.MONTH, displayMonth);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        monthTitle.setText(fmt.format(cal.getTime()));

        long uid = session.getUserId();
        Set<Integer> days = db.getDaysWithTasksInMonth(uid, displayYear, displayMonth);
        calendarAdapter.setMonth(displayYear, displayMonth, days, startOfTodayMs());
    }

    private void onDayClick(CalendarDayAdapter.DayCell cell) {
        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(cell.dayStartMs);
        end.add(Calendar.DAY_OF_MONTH, 1);
        long startMs = cell.dayStartMs;
        long endMs = end.getTimeInMillis();

        long uid = session.getUserId();
        List<Task> dayTasks = db.getTasksForUserOnDay(uid, startMs, endMs);

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_day_tasks, null, false);
        RecyclerView rv = dialogView.findViewById(R.id.recycler_day_tasks);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        AlertDialog dlg = new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.tasks_for_day))
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, null)
                .create();

        DayTasksAdapter ad = new DayTasksAdapter(task -> {
            dlg.dismiss();
            Intent i = new Intent(requireContext(), TaskEditActivity.class);
            i.putExtra(TaskEditActivity.EXTRA_TASK_ID, task.getId());
            taskEditor.launch(i);
        });
        rv.setAdapter(ad);
        ad.setTasks(dayTasks);

        MaterialButton addBtn = dialogView.findViewById(R.id.btn_add_task);
        addBtn.setOnClickListener(v -> {
            dlg.dismiss();
            Intent i = new Intent(requireContext(), TaskEditActivity.class);
            i.putExtra(TaskEditActivity.EXTRA_TASK_ID, -1L);
            i.putExtra(TaskEditActivity.EXTRA_DEFAULT_DUE_MS, cell.dayStartMs);
            taskEditor.launch(i);
        });

        dlg.show();
    }
}
