package com.example.myapplication.ui.tasks;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;
import com.example.myapplication.data.DatabaseHelper;
import com.example.myapplication.data.Task;
import com.example.myapplication.notification.NotificationHelper;
import com.example.myapplication.notification.ReminderScheduler;
import com.example.myapplication.util.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class TaskEditActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_DEFAULT_DUE_MS = "default_due_ms";

    private static final int[] REMINDER_MINUTES = {-1, 5, 15, 30, 60};

    private long taskId = -1;
    private final Calendar dueCal = Calendar.getInstance();
    private TextInputEditText inputTitle;
    private TextInputEditText inputDescription;
    private TextView textDueSummary;
    private Spinner spinnerReminder;
    private MaterialCheckBox checkImportant;
    private SessionManager session;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_task_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        NotificationHelper.ensureChannels(this);
        session = new SessionManager(this);
        db = DatabaseHelper.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        taskId = getIntent().getLongExtra(EXTRA_TASK_ID, -1);
        long defaultDue = getIntent().getLongExtra(EXTRA_DEFAULT_DUE_MS, 0L);
        if (taskId >= 0) {
            toolbar.setTitle(R.string.edit_task);
        } else {
            toolbar.setTitle(R.string.add_task);
        }
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        inputTitle = findViewById(R.id.input_title);
        inputDescription = findViewById(R.id.input_description);
        textDueSummary = findViewById(R.id.text_due_summary);
        spinnerReminder = findViewById(R.id.spinner_reminder);
        checkImportant = findViewById(R.id.check_important);

        String[] labels = new String[]{
                getString(R.string.reminder_none),
                getString(R.string.reminder_5),
                getString(R.string.reminder_15),
                getString(R.string.reminder_30),
                getString(R.string.reminder_60)
        };
        spinnerReminder.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, labels));

        if (taskId >= 0) {
            Task t = db.getTask(taskId, session.getUserId());
            if (t == null) {
                finish();
                return;
            }
            inputTitle.setText(t.getTitle());
            inputDescription.setText(t.getDescription());
            dueCal.setTimeInMillis(t.getDueDateTime());
            int idx = indexOfReminder(t.getReminderMinutes());
            spinnerReminder.setSelection(idx);
            checkImportant.setChecked(t.isImportant());
        } else if (defaultDue > 0) {
            dueCal.setTimeInMillis(defaultDue);
        }

        updateDueSummary();

        MaterialButton btnPick = findViewById(R.id.btn_pick_datetime);
        btnPick.setOnClickListener(v -> showDateThenTime());

        MaterialButton save = findViewById(R.id.btn_save);
        save.setOnClickListener(v -> saveTask());

        if (taskId >= 0) {
            toolbar.inflateMenu(R.menu.task_edit_menu);
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_delete_task) {
                    confirmDeleteTask();
                    return true;
                }
                return false;
            });
        }
    }

    private static int indexOfReminder(int minutes) {
        for (int i = 0; i < REMINDER_MINUTES.length; i++) {
            if (REMINDER_MINUTES[i] == minutes) return i;
        }
        return 0;
    }

    private void showDateThenTime() {
        DatePickerDialog dp = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            dueCal.set(Calendar.YEAR, year);
            dueCal.set(Calendar.MONTH, month);
            dueCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            new TimePickerDialog(TaskEditActivity.this, (view1, hourOfDay, minute) -> {
                dueCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                dueCal.set(Calendar.MINUTE, minute);
                dueCal.set(Calendar.SECOND, 0);
                dueCal.set(Calendar.MILLISECOND, 0);
                updateDueSummary();
            }, dueCal.get(Calendar.HOUR_OF_DAY), dueCal.get(Calendar.MINUTE), true).show();
        }, dueCal.get(Calendar.YEAR), dueCal.get(Calendar.MONTH), dueCal.get(Calendar.DAY_OF_MONTH));
        dp.show();
    }

    private void updateDueSummary() {
        java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance(
                java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT);
        textDueSummary.setText(df.format(dueCal.getTime()));
    }

    private void saveTask() {
        CharSequence t = inputTitle.getText();
        String title = t != null ? t.toString().trim() : "";
        if (title.isEmpty()) {
            Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        CharSequence d = inputDescription.getText();
        String desc = d != null ? d.toString().trim() : "";
        long uid = session.getUserId();
        long dueMs = dueCal.getTimeInMillis();
        int reminder = REMINDER_MINUTES[spinnerReminder.getSelectedItemPosition()];
        boolean important = checkImportant.isChecked();

        if (taskId >= 0) {
            Task old = db.getTask(taskId, uid);
            boolean completed = old != null && old.isCompleted();
            ReminderScheduler.cancel(this, taskId, uid);
            db.updateTask(taskId, uid, title, desc, dueMs, reminder, completed, important);
            Task updated = db.getTask(taskId, uid);
            if (updated != null) ReminderScheduler.schedule(this, updated);
        } else {
            long newId = db.insertTask(uid, title, desc, dueMs, reminder, important);
            Task created = db.getTask(newId, uid);
            if (created != null) ReminderScheduler.schedule(this, created);
        }
        setResult(RESULT_OK);
        finish();
    }

    private void confirmDeleteTask() {
        long uid = session.getUserId();
        new AlertDialog.Builder(this)
                .setMessage(R.string.delete_task_confirm)
                .setPositiveButton(R.string.delete, (d, w) -> {
                    ReminderScheduler.cancel(this, taskId, uid);
                    db.deleteTask(taskId, uid);
                    setResult(RESULT_OK);
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
