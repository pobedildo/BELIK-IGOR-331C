package com.example.myapplication.ui.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.DatabaseHelper;
import com.example.myapplication.data.Task;
import com.example.myapplication.notification.ReminderScheduler;
import com.example.myapplication.ui.calendar.CalendarActivity;
import com.example.myapplication.util.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class TasksFragment extends Fragment {

    private TaskAdapter adapter;
    private TextView emptyView;
    private TextView hintAdd;
    private RecyclerView recyclerView;
    private SessionManager session;
    private DatabaseHelper db;

    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> loadTasks());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tasks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        session = new SessionManager(requireContext());
        db = DatabaseHelper.getInstance(requireContext());

        recyclerView = view.findViewById(R.id.recycler_tasks);
        emptyView = view.findViewById(R.id.empty_view);
        hintAdd = view.findViewById(R.id.hint_add);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TaskAdapter(new TaskAdapter.Listener() {
            @Override
            public void onCheckedChanged(Task task, boolean done) {
                long uid = session.getUserId();
                db.setTaskCompleted(task.getId(), uid, done);
                ReminderScheduler.cancel(requireContext(), task.getId(), uid);
                if (!done) {
                    Task updated = db.getTask(task.getId(), uid);
                    if (updated != null) ReminderScheduler.schedule(requireContext(), updated);
                }
                loadTasks();
            }

            @Override
            public void onItemClick(Task task) {
                openEdit(task.getId());
            }

            @Override
            public void onItemLongClick(Task task, View anchor) {
                PopupMenu pm = new PopupMenu(requireContext(), anchor);
                pm.getMenu().add(0, 1, 0, R.string.edit);
                pm.getMenu().add(0, 2, 0, R.string.delete);
                pm.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 1) {
                        openEdit(task.getId());
                    } else if (item.getItemId() == 2) {
                        deleteTask(task);
                    }
                    return true;
                });
                pm.show();
            }

            @Override
            public void onDeleteClick(Task task) {
                deleteTask(task);
            }
        });
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                Task t = adapter.getTaskAt(pos);
                deleteTask(t);
            }
        }).attachToRecyclerView(recyclerView);

        emptyView.setOnClickListener(v -> openAddTask());
        recyclerView.setOnClickListener(v -> openAddTask());
        hintAdd.setOnClickListener(v -> openAddTask());

        FloatingActionButton fabCalendar = view.findViewById(R.id.fab_calendar);
        fabCalendar.setOnClickListener(v -> startActivity(new Intent(requireContext(), CalendarActivity.class)));

        loadTasks();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks();
    }

    private void openEdit(long taskId) {
        Intent i = new Intent(requireContext(), TaskEditActivity.class);
        i.putExtra(TaskEditActivity.EXTRA_TASK_ID, taskId);
        editLauncher.launch(i);
    }

    private void deleteTask(Task task) {
        long uid = session.getUserId();
        ReminderScheduler.cancel(requireContext(), task.getId(), uid);
        db.deleteTask(task.getId(), uid);
        loadTasks();
    }

    private void loadTasks() {
        long uid = session.getUserId();
        List<Task> list = db.getTasksForUser(uid);
        adapter.setTasks(list);
        boolean empty = list.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        hintAdd.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void openAddTask() {
        Intent i = new Intent(requireContext(), TaskEditActivity.class);
        i.putExtra(TaskEditActivity.EXTRA_TASK_ID, -1L);
        editLauncher.launch(i);
    }
}
