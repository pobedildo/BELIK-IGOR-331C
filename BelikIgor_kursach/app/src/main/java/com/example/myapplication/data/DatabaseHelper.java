package com.example.myapplication.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "organizer.db";
    private static final int DB_VERSION = 3;

    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD_HASH = "password_hash";
    public static final String COL_DISPLAY_NAME = "display_name";
    public static final String COL_AVATAR_URI = "avatar_uri";
    public static final String COL_AVATAR_PRESET = "avatar_preset";
    public static final String COL_NOTIFICATIONS = "notifications_enabled";
    public static final String COL_THEME_DARK = "theme_dark";
    public static final String COL_CREATED_AT = "created_at";

    public static final String TABLE_TASKS = "tasks";
    public static final String COL_TASK_ID = "id";
    public static final String COL_TASK_USER = "user_id";
    public static final String COL_TITLE = "title";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_DUE = "due_datetime";
    public static final String COL_REMINDER = "reminder_minutes";
    public static final String COL_COMPLETED = "completed";
    public static final String COL_TASK_IMPORTANT = "is_important";
    public static final String COL_PARENT_TASK_ID = "parentTaskId";
    public static final String COL_REPEAT_RULE = "repeatRule";

    public static final String TABLE_NOTES = "notes";
    public static final String COL_NOTE_ID = "id";
    public static final String COL_NOTE_USER = "user_id";
    public static final String COL_NOTE_TITLE = "title";
    public static final String COL_NOTE_CONTENT = "content";
    public static final String COL_NOTE_UPDATED = "updated_at";
    public static final String COL_NOTE_IMPORTANT = "is_important";

    public static final String TABLE_TAGS = "tags";
    public static final String COL_TAG_ID = "id";
    public static final String COL_TAG_NAME = "name";
    public static final String COL_TAG_COLOR = "color";

    public static final String TABLE_TASK_TAGS = "task_tags";
    public static final String COL_TASK_TAG_TASK_ID = "taskId";
    public static final String COL_TASK_TAG_TAG_ID = "tagId";

    public static final String TABLE_SUBTASKS = "subtasks";
    public static final String COL_SUBTASK_ID = "id";
    public static final String COL_SUBTASK_TASK_ID = "taskId";
    public static final String COL_SUBTASK_TITLE = "title";
    public static final String COL_SUBTASK_COMPLETED = "isCompleted";

    public static final String TABLE_TASK_IMAGES = "task_images";
    public static final String COL_TASK_IMAGE_ID = "id";
    public static final String COL_TASK_IMAGE_NOTE_ID = "noteId";
    public static final String COL_TASK_IMAGE_PATH = "imagePath";

    public static final String TABLE_BACKUPS = "backups";
    public static final String COL_BACKUP_ID = "id";
    public static final String COL_BACKUP_DATE = "backupDate";
    public static final String COL_BACKUP_FILE_PATH = "filePath";
    public static final String COL_BACKUP_SIZE = "size";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_EMAIL + " TEXT NOT NULL UNIQUE,"
                + COL_PASSWORD_HASH + " TEXT NOT NULL,"
                + COL_DISPLAY_NAME + " TEXT,"
                + COL_AVATAR_URI + " TEXT,"
                + COL_AVATAR_PRESET + " INTEGER NOT NULL DEFAULT 0,"
                + COL_NOTIFICATIONS + " INTEGER NOT NULL DEFAULT 1,"
                + COL_THEME_DARK + " INTEGER NOT NULL DEFAULT 0,"
                + COL_CREATED_AT + " INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE " + TABLE_TASKS + " ("
                + COL_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_TASK_USER + " INTEGER NOT NULL,"
                + COL_TITLE + " TEXT NOT NULL,"
                + COL_DESCRIPTION + " TEXT,"
                + COL_DUE + " INTEGER NOT NULL,"
                + COL_REMINDER + " INTEGER NOT NULL DEFAULT -1,"
                + COL_COMPLETED + " INTEGER NOT NULL DEFAULT 0,"
                + COL_TASK_IMPORTANT + " INTEGER NOT NULL DEFAULT 0,"
                + COL_PARENT_TASK_ID + " INTEGER,"
                + COL_REPEAT_RULE + " TEXT,"
                + "FOREIGN KEY(" + COL_PARENT_TASK_ID + ") REFERENCES " + TABLE_TASKS + "(" + COL_TASK_ID + "),"
                + "FOREIGN KEY(" + COL_TASK_USER + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "))");
        db.execSQL("CREATE TABLE " + TABLE_NOTES + " ("
                + COL_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_NOTE_USER + " INTEGER NOT NULL,"
                + COL_NOTE_TITLE + " TEXT NOT NULL,"
                + COL_NOTE_CONTENT + " TEXT,"
                + COL_NOTE_UPDATED + " INTEGER NOT NULL,"
                + COL_NOTE_IMPORTANT + " INTEGER NOT NULL DEFAULT 0,"
                + "FOREIGN KEY(" + COL_NOTE_USER + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "))");
        db.execSQL("CREATE TABLE " + TABLE_TAGS + " ("
                + COL_TAG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_TAG_NAME + " TEXT NOT NULL UNIQUE,"
                + COL_TAG_COLOR + " TEXT NOT NULL)");
        db.execSQL("CREATE TABLE " + TABLE_TASK_TAGS + " ("
                + COL_TASK_TAG_TASK_ID + " INTEGER NOT NULL,"
                + COL_TASK_TAG_TAG_ID + " INTEGER NOT NULL,"
                + "PRIMARY KEY(" + COL_TASK_TAG_TASK_ID + ", " + COL_TASK_TAG_TAG_ID + "),"
                + "FOREIGN KEY(" + COL_TASK_TAG_TASK_ID + ") REFERENCES " + TABLE_TASKS + "(" + COL_TASK_ID + ") ON DELETE CASCADE,"
                + "FOREIGN KEY(" + COL_TASK_TAG_TAG_ID + ") REFERENCES " + TABLE_TAGS + "(" + COL_TAG_ID + ") ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE " + TABLE_SUBTASKS + " ("
                + COL_SUBTASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_SUBTASK_TASK_ID + " INTEGER NOT NULL,"
                + COL_SUBTASK_TITLE + " TEXT NOT NULL,"
                + COL_SUBTASK_COMPLETED + " INTEGER NOT NULL DEFAULT 0,"
                + "FOREIGN KEY(" + COL_SUBTASK_TASK_ID + ") REFERENCES " + TABLE_TASKS + "(" + COL_TASK_ID + ") ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE " + TABLE_TASK_IMAGES + " ("
                + COL_TASK_IMAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_TASK_IMAGE_NOTE_ID + " INTEGER NOT NULL,"
                + COL_TASK_IMAGE_PATH + " TEXT NOT NULL,"
                + "FOREIGN KEY(" + COL_TASK_IMAGE_NOTE_ID + ") REFERENCES " + TABLE_NOTES + "(" + COL_NOTE_ID + ") ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE " + TABLE_BACKUPS + " ("
                + COL_BACKUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_BACKUP_DATE + " INTEGER NOT NULL,"
                + COL_BACKUP_FILE_PATH + " TEXT NOT NULL,"
                + COL_BACKUP_SIZE + " INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN "
                    + COL_TASK_IMPORTANT + " INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_NOTES + " ADD COLUMN "
                    + COL_NOTE_IMPORTANT + " INTEGER NOT NULL DEFAULT 0");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN "
                    + COL_PARENT_TASK_ID + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN "
                    + COL_REPEAT_RULE + " TEXT");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TAGS + " ("
                    + COL_TAG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COL_TAG_NAME + " TEXT NOT NULL UNIQUE,"
                    + COL_TAG_COLOR + " TEXT NOT NULL)");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TASK_TAGS + " ("
                    + COL_TASK_TAG_TASK_ID + " INTEGER NOT NULL,"
                    + COL_TASK_TAG_TAG_ID + " INTEGER NOT NULL,"
                    + "PRIMARY KEY(" + COL_TASK_TAG_TASK_ID + ", " + COL_TASK_TAG_TAG_ID + "),"
                    + "FOREIGN KEY(" + COL_TASK_TAG_TASK_ID + ") REFERENCES " + TABLE_TASKS + "(" + COL_TASK_ID + ") ON DELETE CASCADE,"
                    + "FOREIGN KEY(" + COL_TASK_TAG_TAG_ID + ") REFERENCES " + TABLE_TAGS + "(" + COL_TAG_ID + ") ON DELETE CASCADE)");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SUBTASKS + " ("
                    + COL_SUBTASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COL_SUBTASK_TASK_ID + " INTEGER NOT NULL,"
                    + COL_SUBTASK_TITLE + " TEXT NOT NULL,"
                    + COL_SUBTASK_COMPLETED + " INTEGER NOT NULL DEFAULT 0,"
                    + "FOREIGN KEY(" + COL_SUBTASK_TASK_ID + ") REFERENCES " + TABLE_TASKS + "(" + COL_TASK_ID + ") ON DELETE CASCADE)");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TASK_IMAGES + " ("
                    + COL_TASK_IMAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COL_TASK_IMAGE_NOTE_ID + " INTEGER NOT NULL,"
                    + COL_TASK_IMAGE_PATH + " TEXT NOT NULL,"
                    + "FOREIGN KEY(" + COL_TASK_IMAGE_NOTE_ID + ") REFERENCES " + TABLE_NOTES + "(" + COL_NOTE_ID + ") ON DELETE CASCADE)");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_BACKUPS + " ("
                    + COL_BACKUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COL_BACKUP_DATE + " INTEGER NOT NULL,"
                    + COL_BACKUP_FILE_PATH + " TEXT NOT NULL,"
                    + COL_BACKUP_SIZE + " INTEGER NOT NULL)");
        }
    }

    public long registerUser(String email, String passwordHash, String displayName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_EMAIL, email.trim().toLowerCase(Locale.ROOT));
        cv.put(COL_PASSWORD_HASH, passwordHash);
        cv.put(COL_DISPLAY_NAME, displayName != null ? displayName : "");
        cv.put(COL_AVATAR_PRESET, 0);
        cv.put(COL_NOTIFICATIONS, 1);
        cv.put(COL_THEME_DARK, 0);
        cv.put(COL_CREATED_AT, System.currentTimeMillis());
        long id = db.insert(TABLE_USERS, null, cv);
        return id;
    }

    public boolean emailExists(String email) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                COL_EMAIL + "=?", new String[]{email.trim().toLowerCase(Locale.ROOT)},
                null, null, null)) {
            return c.moveToFirst();
        }
    }

    public long login(String email, String passwordHash) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                COL_EMAIL + "=? AND " + COL_PASSWORD_HASH + "=?",
                new String[]{email.trim().toLowerCase(Locale.ROOT), passwordHash},
                null, null, null)) {
            if (c.moveToFirst()) {
                return c.getLong(0);
            }
        }
        return -1;
    }

    @Nullable
    public User getUser(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_USERS, null, COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null, null)) {
            if (!c.moveToFirst()) return null;
            return cursorToUser(c);
        }
    }

    private User cursorToUser(Cursor c) {
        return new User(
                c.getLong(c.getColumnIndexOrThrow(COL_USER_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_EMAIL)),
                nullableString(c, COL_DISPLAY_NAME),
                nullableString(c, COL_AVATAR_URI),
                c.getInt(c.getColumnIndexOrThrow(COL_AVATAR_PRESET)),
                c.getInt(c.getColumnIndexOrThrow(COL_NOTIFICATIONS)) == 1,
                c.getInt(c.getColumnIndexOrThrow(COL_THEME_DARK)) == 1,
                c.getLong(c.getColumnIndexOrThrow(COL_CREATED_AT))
        );
    }

    private static String nullableString(Cursor c, String col) {
        int i = c.getColumnIndex(col);
        if (i < 0) return null;
        if (c.isNull(i)) return null;
        return c.getString(i);
    }

    public void updateUserProfile(long userId, String displayName, String avatarUri, int avatarPreset,
                                  boolean notificationsEnabled, boolean darkTheme) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_DISPLAY_NAME, displayName);
        if (avatarUri != null) {
            cv.put(COL_AVATAR_URI, avatarUri);
        } else {
            cv.putNull(COL_AVATAR_URI);
        }
        cv.put(COL_AVATAR_PRESET, avatarPreset);
        cv.put(COL_NOTIFICATIONS, notificationsEnabled ? 1 : 0);
        cv.put(COL_THEME_DARK, darkTheme ? 1 : 0);
        db.update(TABLE_USERS, cv, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
    }

    public void setAvatarPresetOnly(long userId, int preset) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.putNull(COL_AVATAR_URI);
        cv.put(COL_AVATAR_PRESET, preset);
        db.update(TABLE_USERS, cv, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
    }

    public void setAvatarUri(long userId, String uri) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_AVATAR_URI, uri);
        cv.put(COL_AVATAR_PRESET, -1);
        db.update(TABLE_USERS, cv, COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
    }

    public long insertTask(long userId, String title, String description, long dueMs, int reminderMinutes,
                           boolean important) {
        return insertTask(userId, title, description, dueMs, reminderMinutes, important, null, null);
    }

    public long insertTask(long userId, String title, String description, long dueMs, int reminderMinutes,
                           boolean important, @Nullable Long parentTaskId, @Nullable String repeatRule) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_USER, userId);
        cv.put(COL_TITLE, title);
        cv.put(COL_DESCRIPTION, description);
        cv.put(COL_DUE, dueMs);
        cv.put(COL_REMINDER, reminderMinutes);
        cv.put(COL_COMPLETED, 0);
        cv.put(COL_TASK_IMPORTANT, important ? 1 : 0);
        if (parentTaskId != null) {
            cv.put(COL_PARENT_TASK_ID, parentTaskId);
        } else {
            cv.putNull(COL_PARENT_TASK_ID);
        }
        if (repeatRule != null) {
            cv.put(COL_REPEAT_RULE, repeatRule);
        } else {
            cv.putNull(COL_REPEAT_RULE);
        }
        return db.insert(TABLE_TASKS, null, cv);
    }

    public void updateTask(long taskId, long userId, String title, String description,
                           long dueMs, int reminderMinutes, boolean completed, boolean important) {
        updateTask(taskId, userId, title, description, dueMs, reminderMinutes, completed, important, null, null);
    }

    public void updateTask(long taskId, long userId, String title, String description,
                           long dueMs, int reminderMinutes, boolean completed, boolean important,
                           @Nullable Long parentTaskId, @Nullable String repeatRule) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TITLE, title);
        cv.put(COL_DESCRIPTION, description);
        cv.put(COL_DUE, dueMs);
        cv.put(COL_REMINDER, reminderMinutes);
        cv.put(COL_COMPLETED, completed ? 1 : 0);
        cv.put(COL_TASK_IMPORTANT, important ? 1 : 0);
        if (parentTaskId != null) {
            cv.put(COL_PARENT_TASK_ID, parentTaskId);
        } else {
            cv.putNull(COL_PARENT_TASK_ID);
        }
        if (repeatRule != null) {
            cv.put(COL_REPEAT_RULE, repeatRule);
        } else {
            cv.putNull(COL_REPEAT_RULE);
        }
        db.update(TABLE_TASKS, cv, COL_TASK_ID + "=? AND " + COL_TASK_USER + "=?",
                new String[]{String.valueOf(taskId), String.valueOf(userId)});
    }

    public void setTaskCompleted(long taskId, long userId, boolean completed) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_COMPLETED, completed ? 1 : 0);
        db.update(TABLE_TASKS, cv, COL_TASK_ID + "=? AND " + COL_TASK_USER + "=?",
                new String[]{String.valueOf(taskId), String.valueOf(userId)});
    }

    public void deleteTask(long taskId, long userId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_TASKS, COL_TASK_ID + "=? AND " + COL_TASK_USER + "=?",
                new String[]{String.valueOf(taskId), String.valueOf(userId)});
    }

    @Nullable
    public Task getTask(long taskId, long userId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_TASKS, null,
                COL_TASK_ID + "=? AND " + COL_TASK_USER + "=?",
                new String[]{String.valueOf(taskId), String.valueOf(userId)},
                null, null, null)) {
            if (!c.moveToFirst()) return null;
            return cursorToTask(c);
        }
    }

    public List<Task> getTasksForUser(long userId) {
        return getTasksForUser(userId, null);
    }

    public List<Task> getTasksForUser(long userId, @Nullable List<Long> filterTagIds) {
        SQLiteDatabase db = getReadableDatabase();
        List<Task> list = new ArrayList<>();
        Cursor c;
        if (filterTagIds == null || filterTagIds.isEmpty()) {
            c = db.query(TABLE_TASKS, null, COL_TASK_USER + "=?",
                    new String[]{String.valueOf(userId)}, null, null, null);
        } else {
            StringBuilder inClause = new StringBuilder();
            String[] args = new String[1 + filterTagIds.size()];
            args[0] = String.valueOf(userId);
            for (int i = 0; i < filterTagIds.size(); i++) {
                if (i > 0) inClause.append(',');
                inClause.append('?');
                args[i + 1] = String.valueOf(filterTagIds.get(i));
            }
            String sql = "SELECT DISTINCT t.* FROM " + TABLE_TASKS + " t "
                    + "JOIN " + TABLE_TASK_TAGS + " tt ON tt." + COL_TASK_TAG_TASK_ID + " = t." + COL_TASK_ID + " "
                    + "WHERE t." + COL_TASK_USER + "=? AND tt." + COL_TASK_TAG_TAG_ID + " IN (" + inClause + ")";
            c = db.rawQuery(sql, args);
        }
        try (Cursor ignored = c) {
            while (c.moveToNext()) {
                list.add(cursorToTask(c));
            }
        }
        return list;
    }

    public List<Task> getTasksForUserOnDay(long userId, long dayStartMs, long dayEndExclusiveMs) {
        SQLiteDatabase db = getReadableDatabase();
        List<Task> list = new ArrayList<>();
        String sel = COL_TASK_USER + "=? AND " + COL_DUE + ">=? AND " + COL_DUE + "<?";
        String[] args = new String[]{
                String.valueOf(userId),
                String.valueOf(dayStartMs),
                String.valueOf(dayEndExclusiveMs)
        };
        try (Cursor c = db.query(TABLE_TASKS, null, sel, args, null, null, COL_DUE + " ASC")) {
            while (c.moveToNext()) {
                list.add(cursorToTask(c));
            }
        }
        return list;
    }

    public Set<Integer> getDaysWithTasksInMonth(long userId, int year, int monthZeroBased) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, monthZeroBased);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, 1);
        long end = cal.getTimeInMillis();

        SQLiteDatabase db = getReadableDatabase();
        Set<Integer> days = new HashSet<>();
        String sel = COL_TASK_USER + "=? AND " + COL_DUE + ">=? AND " + COL_DUE + "<?";
        String[] args = new String[]{String.valueOf(userId), String.valueOf(start), String.valueOf(end)};
        try (Cursor c = db.query(TABLE_TASKS, new String[]{COL_DUE}, sel, args, null, null, null)) {
            cal.setTimeInMillis(start);
            while (c.moveToNext()) {
                long due = c.getLong(0);
                cal.setTimeInMillis(due);
                days.add(cal.get(Calendar.DAY_OF_MONTH));
            }
        }
        return days;
    }

    private Task cursorToTask(Cursor c) {
        Long parentTaskId = null;
        int parentIdx = c.getColumnIndex(COL_PARENT_TASK_ID);
        if (parentIdx >= 0 && !c.isNull(parentIdx)) {
            parentTaskId = c.getLong(parentIdx);
        }
        return new Task(
                c.getLong(c.getColumnIndexOrThrow(COL_TASK_ID)),
                c.getLong(c.getColumnIndexOrThrow(COL_TASK_USER)),
                c.getString(c.getColumnIndexOrThrow(COL_TITLE)),
                nullableString(c, COL_DESCRIPTION) != null ? nullableString(c, COL_DESCRIPTION) : "",
                c.getLong(c.getColumnIndexOrThrow(COL_DUE)),
                c.getInt(c.getColumnIndexOrThrow(COL_REMINDER)),
                c.getInt(c.getColumnIndexOrThrow(COL_COMPLETED)) == 1,
                c.getInt(c.getColumnIndexOrThrow(COL_TASK_IMPORTANT)) == 1,
                parentTaskId,
                nullableString(c, COL_REPEAT_RULE)
        );
    }

    public void clearTasksForUser(long userId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_TASKS, COL_TASK_USER + "=?", new String[]{String.valueOf(userId)});
    }

    public void clearNotesForUser(long userId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NOTES, COL_NOTE_USER + "=?", new String[]{String.valueOf(userId)});
    }

    public long insertNote(long userId, String title, String content, boolean important) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NOTE_USER, userId);
        cv.put(COL_NOTE_TITLE, title);
        cv.put(COL_NOTE_CONTENT, content);
        cv.put(COL_NOTE_UPDATED, System.currentTimeMillis());
        cv.put(COL_NOTE_IMPORTANT, important ? 1 : 0);
        return db.insert(TABLE_NOTES, null, cv);
    }

    public void updateNote(long noteId, long userId, String title, String content, boolean important) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NOTE_TITLE, title);
        cv.put(COL_NOTE_CONTENT, content);
        cv.put(COL_NOTE_UPDATED, System.currentTimeMillis());
        cv.put(COL_NOTE_IMPORTANT, important ? 1 : 0);
        db.update(TABLE_NOTES, cv, COL_NOTE_ID + "=? AND " + COL_NOTE_USER + "=?",
                new String[]{String.valueOf(noteId), String.valueOf(userId)});
    }

    public void deleteNote(long noteId, long userId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NOTES, COL_NOTE_ID + "=? AND " + COL_NOTE_USER + "=?",
                new String[]{String.valueOf(noteId), String.valueOf(userId)});
    }

    public List<Note> getNotesForUser(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        List<Note> list = new ArrayList<>();
        try (Cursor c = db.query(TABLE_NOTES, null, COL_NOTE_USER + "=?",
                new String[]{String.valueOf(userId)}, null, null,
                COL_NOTE_IMPORTANT + " DESC, " + COL_NOTE_UPDATED + " DESC")) {
            while (c.moveToNext()) {
                list.add(new Note(
                        c.getLong(c.getColumnIndexOrThrow(COL_NOTE_ID)),
                        c.getLong(c.getColumnIndexOrThrow(COL_NOTE_USER)),
                        c.getString(c.getColumnIndexOrThrow(COL_NOTE_TITLE)),
                        nullableString(c, COL_NOTE_CONTENT) != null ? nullableString(c, COL_NOTE_CONTENT) : "",
                        c.getLong(c.getColumnIndexOrThrow(COL_NOTE_UPDATED)),
                        c.getInt(c.getColumnIndexOrThrow(COL_NOTE_IMPORTANT)) == 1
                ));
            }
        }
        return list;
    }

    public List<Task> getIncompleteTasksWithReminders(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        List<Task> list = new ArrayList<>();
        String sel = COL_TASK_USER + "=? AND " + COL_COMPLETED + "=0 AND " + COL_REMINDER + ">=0";
        try (Cursor c = db.query(TABLE_TASKS, null, sel, new String[]{String.valueOf(userId)}, null, null, null)) {
            while (c.moveToNext()) {
                list.add(cursorToTask(c));
            }
        }
        return list;
    }

    /** После перезагрузки устройства — восстановить напоминания для всех пользователей. */
    public List<Task> getAllIncompleteTasksWithReminders() {
        SQLiteDatabase db = getReadableDatabase();
        List<Task> list = new ArrayList<>();
        String sel = COL_COMPLETED + "=0 AND " + COL_REMINDER + ">=0";
        try (Cursor c = db.query(TABLE_TASKS, null, sel, null, null, null, null)) {
            while (c.moveToNext()) {
                list.add(cursorToTask(c));
            }
        }
        return list;
    }

    public List<Tag> getAllTags() {
        SQLiteDatabase db = getReadableDatabase();
        List<Tag> tags = new ArrayList<>();
        try (Cursor c = db.query(TABLE_TAGS, null, null, null, null, null, COL_TAG_NAME + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                tags.add(new Tag(
                        c.getLong(c.getColumnIndexOrThrow(COL_TAG_ID)),
                        c.getString(c.getColumnIndexOrThrow(COL_TAG_NAME)),
                        c.getString(c.getColumnIndexOrThrow(COL_TAG_COLOR))
                ));
            }
        }
        return tags;
    }

    public long upsertTag(String name, String color) {
        SQLiteDatabase db = getWritableDatabase();
        String normName = name.trim();
        try (Cursor c = db.query(TABLE_TAGS, new String[]{COL_TAG_ID},
                "lower(" + COL_TAG_NAME + ")=lower(?)", new String[]{normName}, null, null, null)) {
            if (c.moveToFirst()) return c.getLong(0);
        }
        ContentValues cv = new ContentValues();
        cv.put(COL_TAG_NAME, normName);
        cv.put(COL_TAG_COLOR, color);
        return db.insert(TABLE_TAGS, null, cv);
    }

    public List<Tag> getTaskTags(long taskId) {
        SQLiteDatabase db = getReadableDatabase();
        List<Tag> tags = new ArrayList<>();
        String sql = "SELECT tg." + COL_TAG_ID + ", tg." + COL_TAG_NAME + ", tg." + COL_TAG_COLOR
                + " FROM " + TABLE_TAGS + " tg JOIN " + TABLE_TASK_TAGS + " tt ON tt." + COL_TASK_TAG_TAG_ID + "=tg." + COL_TAG_ID
                + " WHERE tt." + COL_TASK_TAG_TASK_ID + "=? ORDER BY tg." + COL_TAG_NAME + " COLLATE NOCASE ASC";
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(taskId)})) {
            while (c.moveToNext()) {
                tags.add(new Tag(c.getLong(0), c.getString(1), c.getString(2)));
            }
        }
        return tags;
    }

    public void setTaskTags(long taskId, List<Long> tagIds) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_TASK_TAGS, COL_TASK_TAG_TASK_ID + "=?", new String[]{String.valueOf(taskId)});
            for (Long tagId : tagIds) {
                ContentValues cv = new ContentValues();
                cv.put(COL_TASK_TAG_TASK_ID, taskId);
                cv.put(COL_TASK_TAG_TAG_ID, tagId);
                db.insert(TABLE_TASK_TAGS, null, cv);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<TagCount> getTagCountsForUser(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        List<TagCount> list = new ArrayList<>();
        String sql = "SELECT tg." + COL_TAG_ID + ", tg." + COL_TAG_NAME + ", tg." + COL_TAG_COLOR + ", "
                + "COUNT(DISTINCT tt." + COL_TASK_TAG_TASK_ID + ") as cnt "
                + "FROM " + TABLE_TAGS + " tg "
                + "LEFT JOIN " + TABLE_TASK_TAGS + " tt ON tt." + COL_TASK_TAG_TAG_ID + "=tg." + COL_TAG_ID + " "
                + "LEFT JOIN " + TABLE_TASKS + " t ON t." + COL_TASK_ID + "=tt." + COL_TASK_TAG_TASK_ID + " AND t." + COL_TASK_USER + "=? "
                + "GROUP BY tg." + COL_TAG_ID + ", tg." + COL_TAG_NAME + ", tg." + COL_TAG_COLOR + " "
                + "ORDER BY tg." + COL_TAG_NAME + " COLLATE NOCASE ASC";
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(userId)})) {
            while (c.moveToNext()) {
                Tag tag = new Tag(c.getLong(0), c.getString(1), c.getString(2));
                list.add(new TagCount(tag, c.getInt(3)));
            }
        }
        return list;
    }

    public void replaceNoteImages(long noteId, List<String> imagePaths) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_TASK_IMAGES, COL_TASK_IMAGE_NOTE_ID + "=?", new String[]{String.valueOf(noteId)});
            for (String path : imagePaths) {
                ContentValues cv = new ContentValues();
                cv.put(COL_TASK_IMAGE_NOTE_ID, noteId);
                cv.put(COL_TASK_IMAGE_PATH, path);
                db.insert(TABLE_TASK_IMAGES, null, cv);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<NoteImage> getNoteImages(long noteId) {
        SQLiteDatabase db = getReadableDatabase();
        List<NoteImage> images = new ArrayList<>();
        try (Cursor c = db.query(TABLE_TASK_IMAGES, null, COL_TASK_IMAGE_NOTE_ID + "=?",
                new String[]{String.valueOf(noteId)}, null, null, COL_TASK_IMAGE_ID + " ASC")) {
            while (c.moveToNext()) {
                images.add(new NoteImage(
                        c.getLong(c.getColumnIndexOrThrow(COL_TASK_IMAGE_ID)),
                        c.getLong(c.getColumnIndexOrThrow(COL_TASK_IMAGE_NOTE_ID)),
                        c.getString(c.getColumnIndexOrThrow(COL_TASK_IMAGE_PATH))
                ));
            }
        }
        return images;
    }
}
