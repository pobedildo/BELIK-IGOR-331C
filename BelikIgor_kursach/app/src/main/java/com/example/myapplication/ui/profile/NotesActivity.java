package com.example.myapplication.ui.profile;

import android.os.Bundle;
import android.net.Uri;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.DatabaseHelper;
import com.example.myapplication.data.Note;
import com.example.myapplication.data.NoteImage;
import com.example.myapplication.util.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;

public class NotesActivity extends AppCompatActivity {

    private NoteAdapter adapter;
    private DatabaseHelper db;
    private SessionManager session;
    private Uri pendingCameraUri;
    private String pendingCameraPath;
    private final ArrayList<String> editingImagePaths = new ArrayList<>();
    private NoteImageAdapter editingImageAdapter;
    private Runnable onImagesChanged;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickImagesLauncher =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(10), uris -> {
                for (Uri uri : uris) {
                    String savedPath = copyImageToInternal(uri);
                    if (savedPath != null) editingImagePaths.add(savedPath);
                }
                if (onImagesChanged != null) onImagesChanged.run();
            });

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), ok -> {
                if (ok && pendingCameraPath != null) {
                    editingImagePaths.add(pendingCameraPath);
                    if (onImagesChanged != null) onImagesChanged.run();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        db = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        RecyclerView rv = findViewById(R.id.recycler_notes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoteAdapter(new NoteAdapter.Listener() {
            @Override
            public void onNoteClick(Note note) {
                List<NoteImage> images = db.getNoteImages(note.getId());
                showNoteDialog(note.getId(), note.getTitle(), note.getContent(), toPaths(images));
            }

            @Override
            public void onNoteLongClick(Note note, android.view.View anchor) {
                PopupMenu pm = new PopupMenu(NotesActivity.this, anchor);
                pm.getMenu().add(0, 1, 0, R.string.delete);
                pm.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 1) {
                        db.deleteNote(note.getId(), session.getUserId());
                        load();
                    }
                    return true;
                });
                pm.show();
            }
        });
        rv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_add_note);
        fab.setOnClickListener(v -> showNoteDialog(-1, "", "", new ArrayList<>()));

        load();
    }

    private void load() {
        long uid = session.getUserId();
        List<Note> list = db.getNotesForUser(uid);
        adapter.setNotes(list);
    }

    private void showNoteDialog(long noteId, String title, String content, List<String> imagePaths) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad / 2, pad, 0);

        EditText t = new EditText(this);
        t.setHint(R.string.note_title);
        t.setText(title);
        layout.addView(t);

        EditText c = new EditText(this);
        c.setHint(R.string.note_content);
        c.setMinLines(4);
        c.setText(content);
        layout.addView(c);

        RecyclerView imagesRv = new RecyclerView(this);
        imagesRv.setLayoutManager(new GridLayoutManager(this, 3));
        editingImageAdapter = new NoteImageAdapter(this::showImagePreview);
        imagesRv.setAdapter(editingImageAdapter);
        layout.addView(imagesRv);

        LinearLayout imageButtons = new LinearLayout(this);
        imageButtons.setOrientation(LinearLayout.HORIZONTAL);
        android.widget.Button btnGallery = new android.widget.Button(this);
        btnGallery.setText(R.string.attach_from_gallery);
        btnGallery.setOnClickListener(v -> pickImagesLauncher.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()));
        imageButtons.addView(btnGallery);
        android.widget.Button btnCamera = new android.widget.Button(this);
        btnCamera.setText(R.string.take_photo);
        btnCamera.setOnClickListener(v -> capturePhoto());
        imageButtons.addView(btnCamera);
        layout.addView(imageButtons);

        editingImagePaths.clear();
        editingImagePaths.addAll(imagePaths);
        onImagesChanged = () -> editingImageAdapter.setPaths(editingImagePaths);
        onImagesChanged.run();

        new AlertDialog.Builder(this)
                .setTitle(noteId < 0 ? R.string.add_note : R.string.edit)
                .setView(layout)
                .setPositiveButton(R.string.save, (d, w) -> {
                    String nt = t.getText() != null ? t.getText().toString().trim() : "";
                    if (nt.isEmpty()) {
                        Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String nc = c.getText() != null ? c.getText().toString().trim() : "";
                    long uid = session.getUserId();
                    if (noteId < 0) {
                        long newId = db.insertNote(uid, nt, nc, false);
                        db.replaceNoteImages(newId, editingImagePaths);
                    } else {
                        db.updateNote(noteId, uid, nt, nc, false);
                        db.replaceNoteImages(noteId, editingImagePaths);
                    }
                    load();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private List<String> toPaths(List<NoteImage> images) {
        List<String> paths = new ArrayList<>();
        for (NoteImage image : images) paths.add(image.getImagePath());
        return paths;
    }

    private void capturePhoto() {
        File dir = new File(getFilesDir(), "note_images");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, "cam_" + System.currentTimeMillis() + ".jpg");
        pendingCameraPath = file.getAbsolutePath();
        pendingCameraUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        takePictureLauncher.launch(pendingCameraUri);
    }

    private String copyImageToInternal(Uri uri) {
        try {
            File dir = new File(getFilesDir(), "note_images");
            if (!dir.exists()) dir.mkdirs();
            File out = new File(dir, "img_" + System.currentTimeMillis() + ".jpg");
            try (InputStream in = getContentResolver().openInputStream(uri);
                 FileOutputStream fos = new FileOutputStream(out)) {
                if (in == null) return null;
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) > 0) {
                    fos.write(buffer, 0, read);
                }
            }
            return out.getAbsolutePath();
        } catch (Exception e) {
            Toast.makeText(this, R.string.error_image_attach, Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void showImagePreview(String path) {
        android.widget.ImageView image = new android.widget.ImageView(this);
        image.setAdjustViewBounds(true);
        image.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
        image.setImageURI(Uri.fromFile(new File(path)));
        new AlertDialog.Builder(this)
                .setView(image)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
