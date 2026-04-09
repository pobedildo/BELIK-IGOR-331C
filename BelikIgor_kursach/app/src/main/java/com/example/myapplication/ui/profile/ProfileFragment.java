package com.example.myapplication.ui.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.auth.LoginActivity;
import com.example.myapplication.data.DatabaseHelper;
import com.example.myapplication.data.User;
import com.example.myapplication.notification.ReminderScheduler;
import com.example.myapplication.util.SessionManager;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileFragment extends Fragment {

    private static final int[] AVATAR_DRAWABLES = {
            R.drawable.avatar_circle_1,
            R.drawable.avatar_circle_2,
            R.drawable.avatar_circle_3,
            R.drawable.avatar_circle_4
    };

    private SessionManager session;
    private DatabaseHelper db;
    private ShapeableImageView avatar;
    private TextInputEditText inputName;
    private MaterialSwitch switchNotifications;
    private MaterialSwitch switchDark;

    private final ActivityResultLauncher<Intent> pickImage =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() == null || result.getData().getData() == null) return;
                Uri uri = result.getData().getData();
                try {
                    requireContext().getContentResolver().takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException ignored) {
                }
                long uid = new SessionManager(requireContext()).getUserId();
                DatabaseHelper dh = DatabaseHelper.getInstance(requireContext());
                dh.setAvatarUri(uid, uri.toString());
                User u = dh.getUser(uid);
                if (avatar != null) applyAvatar(u);
            });

    private final ActivityResultLauncher<String> requestReadImages =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) pickImage.launch(new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        session = new SessionManager(requireContext());
        db = DatabaseHelper.getInstance(requireContext());

        avatar = view.findViewById(R.id.image_avatar);
        inputName = view.findViewById(R.id.input_name);
        switchNotifications = view.findViewById(R.id.switch_notifications);
        switchDark = view.findViewById(R.id.switch_dark);

        view.findViewById(R.id.btn_change_avatar).setOnClickListener(v -> showAvatarDialog());
        view.findViewById(R.id.btn_save_profile).setOnClickListener(v -> saveProfileFields());

        loadUserIntoForm();

        view.findViewById(R.id.btn_my_data).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), MyDataActivity.class)));

        view.findViewById(R.id.btn_clear_data).setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setMessage(R.string.clear_all_confirm)
                        .setPositiveButton(R.string.yes, (d, w) -> clearUserData())
                        .setNegativeButton(R.string.cancel, null)
                        .show());

        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            long uid = session.getUserId();
            ReminderScheduler.cancelAllForUser(requireContext(), uid);
            session.clear();
            Intent i = new Intent(requireContext(), LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            if (getActivity() != null) getActivity().finish();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserIntoForm();
    }

    private void bindSwitchListeners() {
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            long uid = session.getUserId();
            User u = db.getUser(uid);
            if (u == null) return;
            db.updateUserProfile(uid,
                    textOrEmpty(inputName, u.getDisplayName()),
                    u.getAvatarUri(),
                    u.getAvatarPreset(),
                    isChecked,
                    switchDark.isChecked());
            if (isChecked) {
                ReminderScheduler.rescheduleAllForUser(requireContext(), uid);
            } else {
                ReminderScheduler.cancelAllForUser(requireContext(), uid);
            }
        });

        switchDark.setOnCheckedChangeListener((buttonView, isChecked) -> {
            long uid = session.getUserId();
            User u = db.getUser(uid);
            if (u == null) return;
            db.updateUserProfile(uid,
                    textOrEmpty(inputName, u.getDisplayName()),
                    u.getAvatarUri(),
                    u.getAvatarPreset(),
                    switchNotifications.isChecked(),
                    isChecked);
            AppCompatDelegate.setDefaultNightMode(isChecked
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO);
            if (getActivity() != null) {
                getActivity().recreate();
            }
        });
    }

    private void loadUserIntoForm() {
        User u = db.getUser(session.getUserId());
        if (u == null) return;
        switchNotifications.setOnCheckedChangeListener(null);
        switchDark.setOnCheckedChangeListener(null);
        inputName.setText(u.getDisplayName());
        switchNotifications.setChecked(u.isNotificationsEnabled());
        switchDark.setChecked(u.isDarkTheme());
        applyAvatar(u);
        bindSwitchListeners();
    }

    private void applyAvatar(User u) {
        if (u.getAvatarPreset() >= 0 && u.getAvatarPreset() < AVATAR_DRAWABLES.length) {
            avatar.setImageResource(AVATAR_DRAWABLES[u.getAvatarPreset()]);
            return;
        }
        if (u.getAvatarUri() != null && !u.getAvatarUri().isEmpty()) {
            avatar.setImageURI(Uri.parse(u.getAvatarUri()));
            return;
        }
        avatar.setImageResource(AVATAR_DRAWABLES[0]);
    }

    private void showAvatarDialog() {
        CharSequence[] items = new CharSequence[]{
                getString(R.string.avatar_gallery),
                getString(R.string.avatar_presets) + " 1",
                getString(R.string.avatar_presets) + " 2",
                getString(R.string.avatar_presets) + " 3",
                getString(R.string.avatar_presets) + " 4"
        };
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.avatar_choose)
                .setItems(items, (d, which) -> {
                    if (which == 0) {
                        openGallery();
                    } else {
                        int preset = which - 1;
                        long uid = session.getUserId();
                        db.setAvatarPresetOnly(uid, preset);
                        User u = db.getUser(uid);
                        applyAvatar(u);
                    }
                })
                .show();
    }

    private void openGallery() {
        String perm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(requireContext(), perm) != PackageManager.PERMISSION_GRANTED) {
            requestReadImages.launch(perm);
        } else {
            pickImage.launch(new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
        }
    }

    private void saveProfileFields() {
        long uid = session.getUserId();
        User u = db.getUser(uid);
        if (u == null) return;
        String name = textOrEmpty(inputName, u.getDisplayName());
        db.updateUserProfile(uid, name, u.getAvatarUri(), u.getAvatarPreset(),
                switchNotifications.isChecked(), switchDark.isChecked());
        Toast.makeText(requireContext(), R.string.save, Toast.LENGTH_SHORT).show();
    }

    private static String textOrEmpty(TextInputEditText e, String fallback) {
        if (e.getText() == null) return fallback != null ? fallback : "";
        String s = e.getText().toString().trim();
        return s.isEmpty() ? (fallback != null ? fallback : "") : s;
    }

    private void clearUserData() {
        long uid = session.getUserId();
        ReminderScheduler.cancelAllForUser(requireContext(), uid);
        db.clearTasksForUser(uid);
        db.clearNotesForUser(uid);
        Toast.makeText(requireContext(), R.string.clear_all_data, Toast.LENGTH_SHORT).show();
    }
}
